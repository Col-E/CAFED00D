package me.coley.cafedude.io;

import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.annotation.AnnotationElementValue;
import me.coley.cafedude.classfile.annotation.ArrayElementValue;
import me.coley.cafedude.classfile.annotation.ClassElementValue;
import me.coley.cafedude.classfile.annotation.ElementValue;
import me.coley.cafedude.classfile.annotation.EnumElementValue;
import me.coley.cafedude.classfile.annotation.PrimitiveElementValue;
import me.coley.cafedude.classfile.annotation.TargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.CatchTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.EmptyTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.FormalParameterTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.LocalVarTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.LocalVarTargetInfo.Variable;
import me.coley.cafedude.classfile.annotation.TargetInfo.OffsetTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.SuperTypeTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.ThrowsTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.TypeArgumentTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.TypeParameterBoundTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.TypeParameterTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfoType;
import me.coley.cafedude.classfile.annotation.TypeAnnotation;
import me.coley.cafedude.classfile.annotation.TypePath;
import me.coley.cafedude.classfile.annotation.TypePathElement;
import me.coley.cafedude.classfile.annotation.TypePathKind;
import me.coley.cafedude.classfile.annotation.Utf8ElementValue;
import me.coley.cafedude.classfile.attribute.AnnotationDefaultAttribute;
import me.coley.cafedude.classfile.attribute.AnnotationsAttribute;
import me.coley.cafedude.classfile.attribute.ParameterAnnotationsAttribute;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Annotation reader for all annotation attributes.
 *
 * @author Matt Coley
 */
public class AnnotationReader {
	private static final Logger logger = LoggerFactory.getLogger(AnnotationReader.class);
	private final ClassFileReader reader;
	private final ConstPool cp;
	private final DataInputStream is;
	private final AttributeContext context;
	private final CpUtf8 name;
	private final int maxCpIndex;
	private final boolean visible;

	/**
	 * Create an annotation reader.
	 *
	 * @param reader
	 * 		Parent class reader.
	 * @param cp
	 * 		The constant pool to use for reference.
	 * @param is
	 * 		Stream to read from.
	 * @param length
	 * 		Expected length of data to read.
	 * @param name
	 * 		Attribute name index.
	 * @param context
	 * 		Location of the annotation.
	 * @param visible
	 * 		Whether the annotation is visible at runtime.
	 *
	 * @throws IOException
	 * 		When the subsection of the given stream for annotation reading cannot be allocated,
	 * 		possible due to out-of-bounds problems. This is an indicator of a malformed class.
	 */
	public AnnotationReader(ClassFileReader reader, ConstPool cp, DataInputStream is, int length,
							CpUtf8 name, AttributeContext context, boolean visible)
			throws IOException {
		this.reader = reader;
		this.cp = cp;
		byte[] data = new byte[length];
		is.readFully(data);
		this.is = new DataInputStream(new ByteArrayInputStream(data));
		this.name = name;
		this.context = context;
		this.maxCpIndex = cp.size();
		this.visible = visible;
	}

	/**
	 * Reads an {@link AnnotationDefaultAttribute} attribute.
	 *
	 * @return The annotation default attribute read. {@code null} when malformed.
	 */
	@Nullable
	public AnnotationDefaultAttribute readAnnotationDefault() {
		try {
			return new AnnotationDefaultAttribute(name, readElementValue());
		} catch (Throwable t) {
			logger.debug("Illegally formatted AnnotationDefault, dropping");
			return null;
		}
	}

	/**
	 * Reads an attribute containing multiple annotations. Used for:
	 * <ul>
	 *     <li>{@code RuntimeInvisibleAnnotations}</li>
	 *     <li>{@code RuntimeVisibleAnnotations}</li>
	 * </ul>
	 *
	 * @return The type annotation attribute read. {@code null} if the annotation was malformed.
	 */
	@Nullable
	public AnnotationsAttribute readAnnotations() {
		try {
			// Skip if obvious junk
			int numAnnotations = is.readUnsignedShort();
			if (numAnnotations == 0) {
				logger.debug("Annotations attribute has 0 items, skipping");
				return null;
			}
			// Read each annotation
			Set<String> usedAnnotationTypes = new HashSet<>();
			List<Annotation> annotations = new ArrayList<>();
			for (int i = 0; i < numAnnotations; i++) {
				Annotation annotation = readAnnotation();
				if (reader.doDropDupeAnnotations()) {
					// Only add if the type hasn't been used before
					String type = annotation.getType().getText();
					if (!usedAnnotationTypes.contains(type)) {
						annotations.add(annotation);
						usedAnnotationTypes.add(type);
					}
				} else {
					// Add unconditionally
					annotations.add(annotation);
				}
			}
			// Didn't throw exception, its valid
			return new AnnotationsAttribute(name, annotations, visible);
		} catch (Throwable t) {
			logger.debug("Illegally formatted Annotations, dropping");
			return null;
		}
	}

	/**
	 * Reads a parameter annotation.
	 *
	 * @return The type annotation attribute read. {@code null} if the annotation was malformed.
	 */
	@Nullable
	public ParameterAnnotationsAttribute readParameterAnnotations() {
		try {
			// Skip if obvious junk
			int numParameters = is.readUnsignedByte();
			if (numParameters == 0) {
				logger.debug("ParameterAnnotations attribute has 0 items, skipping");
				return null;
			}
			// Each parameter has its own number of annotations to parse
			Map<Integer, List<Annotation>> parameterAnnotations = new LinkedHashMap<>();
			for (int p = 0; p < numParameters; p++) {
				List<Annotation> annotations = new ArrayList<>();
				int numAnnotations = is.readUnsignedShort();
				for (int i = 0; i < numAnnotations; i++)
					annotations.add(readAnnotation());
				parameterAnnotations.put(p, annotations);
			}
			// Didn't crash, its valid
			return new ParameterAnnotationsAttribute(name, parameterAnnotations, visible);
		} catch (Throwable t) {
			logger.debug("Illegally formatted ParameterAnnotations, dropping");
			return null;
		}
	}

	/**
	 * Reads a collection of type annotations <i>(TypeParameterAnnotations)</i>.
	 *
	 * @return The type annotation attribute read. {@code null} if the annotation was malformed.
	 */
	@Nullable
	public AnnotationsAttribute readTypeAnnotations() {
		try {
			// Skip if obvious junk
			int numAnnotations = is.readUnsignedShort();
			if (numAnnotations == 0) {
				logger.debug("TypeAnnotations attribute has 0 items, skipping");
				return null;
			}
			// Read each type annotation
			List<Annotation> annotations = new ArrayList<>();
			for (int i = 0; i < numAnnotations; i++)
				annotations.add(readTypeAnnotation());
			// Didn't throw exception, its valid
			return new AnnotationsAttribute(name, annotations, visible);
		} catch (Throwable t) {
			logger.debug("Illegally formatted TypeAnnotations, dropping");
			return null;
		}
	}

	/**
	 * Common annotation structure reading.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private Annotation readAnnotation() throws IOException {
		int typeIndex = is.readUnsignedShort();
		// Validate the type points to an entry in the constant pool that is valid UTF8 item
		if (typeIndex >= maxCpIndex) {
			logger.warn("Illegally formatted Annotation item, out of CP bounds, type_index={} >= {}",
					typeIndex, maxCpIndex);
			throw new IllegalArgumentException("Annotation type_index out of CP bounds!");
		}
		CpUtf8 type = (CpUtf8) cp.get(typeIndex);
		Map<CpUtf8, ElementValue> values = readElementPairs();
		return new Annotation(type, values);
	}

	/**
	 * Common type annotation structure reading.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private TypeAnnotation readTypeAnnotation() throws IOException {
		// Read target type (lets us know where the type annotation is located)
		int targetType = is.readUnsignedByte();
		AttributeContext expectedLocation = AttributeContext.fromAnnotationTargetType(targetType);
		// Skip if context is not expected location.
		if (!context.equals(expectedLocation))
			throw new IllegalArgumentException("Annotation location does not match allowed locations for its type");
		// Parse target info union
		TargetInfoType targetInfoType = TargetInfoType.fromTargetType(targetType);
		TargetInfo info;
		switch (targetInfoType) {
			case TYPE_PARAMETER_TARGET: {
				int typeParameterIndex = is.readUnsignedByte();
				info = new TypeParameterTargetInfo(targetType, typeParameterIndex);
				break;
			}
			case SUPERTYPE_TARGET: {
				int superTypeIndex = is.readUnsignedShort();
				info = new SuperTypeTargetInfo(targetType, superTypeIndex);
				break;
			}
			case TYPE_PARAMETER_BOUND_TARGET: {
				int typeParameterIndex = is.readUnsignedByte();
				int boundIndex = is.readUnsignedByte();
				info = new TypeParameterBoundTargetInfo(targetType, typeParameterIndex, boundIndex);
				break;
			}
			case EMPTY_TARGET: {
				info = new EmptyTargetInfo(targetType);
				break;
			}
			case FORMAL_PARAMETER_TARGET: {
				int formalParameterIndex = is.readUnsignedByte();
				info = new FormalParameterTargetInfo(targetType, formalParameterIndex);
				break;
			}
			case THROWS_TARGET: {
				int throwsTypeIndex = is.readUnsignedShort();
				info = new ThrowsTargetInfo(targetType, throwsTypeIndex);
				break;
			}
			case LOCALVAR_TARGET: {
				List<Variable> variables = new ArrayList<>();
				int tableLength = is.readUnsignedShort();
				for (int i = 0; i < tableLength; i++) {
					int startPc = is.readUnsignedShort();
					int length = is.readUnsignedShort();
					int index = is.readUnsignedShort();
					variables.add(new Variable(startPc, length, index));
				}
				info = new LocalVarTargetInfo(targetType, variables);
				break;
			}
			case CATCH_TARGET: {
				int exceptionTableIndex = is.readUnsignedShort();
				info = new CatchTargetInfo(targetType, exceptionTableIndex);
				break;
			}
			case OFFSET_TARGET: {
				int offset = is.readUnsignedShort();
				info = new OffsetTargetInfo(targetType, offset);
				break;
			}
			case TYPE_ARGUMENT_TARGET: {
				int offset = is.readUnsignedShort();
				int typeArgumentIndex = is.readUnsignedByte();
				info = new TypeArgumentTargetInfo(targetType, offset, typeArgumentIndex);
				break;
			}
			default:
				throw new IllegalArgumentException("Invalid type argument target");
		}
		// Parse type path
		TypePath typePath = readTypePath();
		// Parse the stuff that populates a normal annotation
		CpUtf8 type = (CpUtf8) cp.get(is.readUnsignedShort());
		Map<CpUtf8, ElementValue> values = readElementPairs();
		return new TypeAnnotation(type, values, info, typePath);
	}

	/**
	 * @return Read type path.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private TypePath readTypePath() throws IOException {
		int length = is.readUnsignedByte();
		List<TypePathElement> elements = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			int kind = is.readUnsignedByte();
			int index = is.readUnsignedByte();
			elements.add(new TypePathElement(TypePathKind.fromValue(kind), index));
		}
		return new TypePath(elements);
	}

	/**
	 * @return The annotation field pairs <i>({@code name} --> {@code Value})</i>.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private Map<CpUtf8, ElementValue> readElementPairs() throws IOException {
		int numPairs = is.readUnsignedShort();
		Map<CpUtf8, ElementValue> values = new LinkedHashMap<>();
		while (numPairs > 0) {
			CpUtf8 name = (CpUtf8) cp.get(is.readUnsignedShort());
			ElementValue value = readElementValue();
			if (values.containsKey(name))
				throw new IllegalArgumentException("Element pairs already has field by name index: " + name);
			values.put(name, value);
			numPairs--;
		}
		return values;
	}

	/**
	 * @return The annotation field <i>(Technically method)</i> value.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private ElementValue readElementValue() throws IOException {
		char tag = (char) is.readUnsignedByte();
		switch (tag) {
			case 'B': // byte
			case 'C': // char
			case 'D': // double
			case 'F': // float
			case 'I': // int
			case 'J': // long
			case 'S': // short
			case 'Z': // boolean
				int index = is.readUnsignedShort();
				CpEntry entry = cp.get(index);
				return new PrimitiveElementValue(tag, entry);
			case 's': // String
				int utfIndex = is.readUnsignedShort();
				CpUtf8 utf = (CpUtf8) cp.get(utfIndex);
				return new Utf8ElementValue(tag, utf);
			case 'e': // Enum
				int typename = is.readUnsignedShort();
				int constname = is.readUnsignedShort();
				CpUtf8 type = (CpUtf8) cp.get(typename);
				CpUtf8 constant = (CpUtf8) cp.get(constname);
				return new EnumElementValue(tag, type, constant);
			case 'c': // Class
				int classInfoIndex = is.readUnsignedShort();
				CpUtf8 classInfo = (CpUtf8) cp.get(classInfoIndex);
				return new ClassElementValue(tag, classInfo);
			case '@': // Annotation
				Annotation nestedAnnotation = readAnnotation();
				return new AnnotationElementValue(tag, nestedAnnotation);
			case '[': // Array
				int numElements = is.readUnsignedShort();
				List<ElementValue> arrayValues = new ArrayList<>();
				for (int i = 0; i < numElements; i++)
					arrayValues.add(readElementValue());
				return new ArrayElementValue(tag, arrayValues);
			default:
				logger.debug("Unknown element_value tag: ({}) '{}'", (int) tag, tag);
				break;
		}
		throw new IllegalArgumentException("Unrecognized tag for annotation element value: " + tag);
	}
}
