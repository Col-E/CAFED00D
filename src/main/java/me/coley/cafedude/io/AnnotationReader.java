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
import me.coley.cafedude.classfile.constant.CpUtf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final int nameIndex;
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
	 * @param nameIndex
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
							int nameIndex, AttributeContext context, boolean visible)
			throws IOException {
		this.reader = reader;
		this.cp = cp;
		byte[] data = new byte[length];
		is.readFully(data);
		this.is = new DataInputStream(new ByteArrayInputStream(data));
		this.nameIndex = nameIndex;
		this.context = context;
		this.maxCpIndex = cp.size();
		this.visible = visible;
	}

	/**
	 * Reads an {@link AnnotationDefaultAttribute} attribute.
	 *
	 * @return The annotation default attribute read. {@code null} when malformed.
	 */
	public AnnotationDefaultAttribute readAnnotationDefault() {
		try {
			return new AnnotationDefaultAttribute(nameIndex, readElementValue());
		} catch (Throwable t) {
			logger.debug("Illegally formatted AnnotationDefault", t);
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
					String type = cp.getUtf(annotation.getTypeIndex());
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
			return new AnnotationsAttribute(nameIndex, annotations, visible);
		} catch (Throwable t) {
			logger.debug("Illegally formatted Annotations", t);
			return null;
		}
	}

	/**
	 * Reads a parameter annotation.
	 *
	 * @return The type annotation attribute read. {@code null} if the annotation was malformed.
	 */
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
			return new ParameterAnnotationsAttribute(nameIndex, parameterAnnotations, visible);
		} catch (Throwable t) {
			logger.debug("Illegally formatted ParameterAnnotations", t);
			return null;
		}
	}

	/**
	 * Reads a collection of type annotations <i>(TypeParameterAnnotations)</i>.
	 *
	 * @return The type annotation attribute read. {@code null} if the annotation was malformed.
	 */
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
			return new AnnotationsAttribute(nameIndex, annotations, visible);
		} catch (Throwable t) {
			logger.debug("Illegally formatted TypeAnnotations", t);
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
		if (!cp.isIndexOfType(typeIndex, CpUtf8.class)) {
			logger.warn("Illegally formatted Annotation item, type_index={} != CP_UTF8", typeIndex);
			throw new IllegalArgumentException("Annotation type_index does not point to CP_UTF8!");
		}
		Map<Integer, ElementValue> values = readElementPairs();
		return new Annotation(typeIndex, values);
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
		int typeIndex = is.readUnsignedShort();
		Map<Integer, ElementValue> values = readElementPairs();
		return new TypeAnnotation(typeIndex, values, info, typePath);
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
	 * @return The annotation field pairs <i>({@code NameIndex} --> {@code Value})</i>.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private Map<Integer, ElementValue> readElementPairs() throws IOException {
		int numPairs = is.readUnsignedShort();
		Map<Integer, ElementValue> values = new LinkedHashMap<>();
		while (numPairs > 0) {
			int nameIndex = is.readUnsignedShort();
			ElementValue value = readElementValue();
			if (values.containsKey(nameIndex))
				throw new IllegalArgumentException("Element pairs already has field by name index: " + nameIndex);
			values.put(nameIndex, value);
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
				return new PrimitiveElementValue(tag, index);
			case 's': // String
				int utfIndex = is.readUnsignedShort();
				return new Utf8ElementValue(tag, utfIndex);
			case 'e': // Enum
				int typeNameIndex = is.readUnsignedShort();
				int constNameIndex = is.readUnsignedShort();
				return new EnumElementValue(tag, typeNameIndex, constNameIndex);
			case 'c': // Class
				int classInfoIndex = is.readUnsignedShort();
				return new ClassElementValue(tag, classInfoIndex);
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
