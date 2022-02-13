package me.coley.cafedude.io;

import me.coley.cafedude.Constants.Attributes;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.attribute.AnnotationDefaultAttribute;
import me.coley.cafedude.classfile.attribute.AnnotationsAttribute;
import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.attribute.AttributeVersions;
import me.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute;
import me.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import me.coley.cafedude.classfile.attribute.CodeAttribute;
import me.coley.cafedude.classfile.attribute.CodeAttribute.ExceptionTableEntry;
import me.coley.cafedude.classfile.attribute.ConstantValueAttribute;
import me.coley.cafedude.classfile.attribute.DebugExtensionAttribute;
import me.coley.cafedude.classfile.attribute.DefaultAttribute;
import me.coley.cafedude.classfile.attribute.DeprecatedAttribute;
import me.coley.cafedude.classfile.attribute.EnclosingMethodAttribute;
import me.coley.cafedude.classfile.attribute.ExceptionsAttribute;
import me.coley.cafedude.classfile.attribute.InnerClassesAttribute;
import me.coley.cafedude.classfile.attribute.InnerClassesAttribute.InnerClass;
import me.coley.cafedude.classfile.attribute.LineNumberTableAttribute;
import me.coley.cafedude.classfile.attribute.LineNumberTableAttribute.LineEntry;
import me.coley.cafedude.classfile.attribute.LocalVariableTableAttribute;
import me.coley.cafedude.classfile.attribute.LocalVariableTableAttribute.VarEntry;
import me.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute;
import me.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute.VarTypeEntry;
import me.coley.cafedude.classfile.attribute.ModuleAttribute;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Exports;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Opens;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Provides;
import me.coley.cafedude.classfile.attribute.ModuleAttribute.Requires;
import me.coley.cafedude.classfile.attribute.NestHostAttribute;
import me.coley.cafedude.classfile.attribute.NestMembersAttribute;
import me.coley.cafedude.classfile.attribute.ParameterAnnotationsAttribute;
import me.coley.cafedude.classfile.attribute.PermittedClassesAttribute;
import me.coley.cafedude.classfile.attribute.RecordAttribute;
import me.coley.cafedude.classfile.attribute.RecordAttribute.RecordComponent;
import me.coley.cafedude.classfile.attribute.SignatureAttribute;
import me.coley.cafedude.classfile.attribute.SourceFileAttribute;
import me.coley.cafedude.classfile.attribute.StackMapTableAttribute;
import me.coley.cafedude.classfile.attribute.StackMapTableAttribute.StackMapFrame;
import me.coley.cafedude.classfile.attribute.StackMapTableAttribute.TypeInfo;
import me.coley.cafedude.classfile.attribute.SyntheticAttribute;
import me.coley.cafedude.classfile.constant.CpUtf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static me.coley.cafedude.Constants.Attributes.*;
import static me.coley.cafedude.Constants.StackMapTable.*;

/**
 * Attribute reader for all attributes.
 * <br>
 * Annotations delegate to {@link AnnotationReader} due to complexity.
 *
 * @author Matt Coley
 */
public class AttributeReader {
	private static final Logger logger = LoggerFactory.getLogger(AttributeReader.class);
	private final IndexableByteStream is;
	private final ClassFileReader reader;
	private final ClassBuilder builder;
	// Attribute info
	private final int expectedContentLength;
	private final int nameIndex;

	/**
	 * @param reader
	 * 		Parent class reader.
	 * @param builder
	 * 		Class being build/read into.
	 * @param is
	 * 		Parent stream.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	public AttributeReader(ClassFileReader reader, ClassBuilder builder, DataInputStream is) throws IOException {
		this.reader = reader;
		this.builder = builder;
		// Extract name/lengtjh
		this.nameIndex = is.readUnsignedShort();
		this.expectedContentLength = is.readInt();
		// Create local stream
		byte[] subsection = new byte[expectedContentLength];
		is.readFully(subsection);
		this.is = new IndexableByteStream(subsection);
	}

	/**
	 * @param context
	 * 		Where the attribute is applied to.
	 *
	 * @return Attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	public Attribute readAttribute(AttributeContext context) throws IOException {
		try {
			Attribute attribute = read(context);
			if (attribute == null)
				return null;
			int read = is.getIndex();
			if (read != expectedContentLength) {
				String name = ((CpUtf8) builder.getPool().get(nameIndex)).getText();
				logger.debug("Invalid '{}' on {}, claimed to be {} bytes, but was {}",
						name, context.name(), expectedContentLength, read);
				return null;
			}
			return attribute;
		} catch (IOException ex) {
			if (reader.doDropEofAttributes()) {
				String name = ((CpUtf8) builder.getPool().get(nameIndex)).getText();
				logger.debug("Invalid '{}' on {}, EOF thrown when parsing attribute, expected {} bytes",
						name, context.name(), expectedContentLength);
				return null;
			} else
				throw ex;
		}
	}

	private Attribute read(AttributeContext context) throws IOException {
		ConstPool pool = builder.getPool();
		String name = pool.getUtf(nameIndex);
		// Check for illegally inserted attributes from future versions
		if (reader.doDropForwardVersioned()) {
			int introducedAt = AttributeVersions.getIntroducedVersion(name);
			if (introducedAt > builder.getVersionMajor()) {
				logger.debug("Found '{}' on {} in class version {}, min supported is {}",
						name, context.name(), builder.getVersionMajor(), introducedAt);
				return null;
			}
		}
		switch (name) {
			case CODE:
				return readCode();
			case CONSTANT_VALUE:
				return readConstantValue();
			case DEPRECATED:
				return new DeprecatedAttribute(nameIndex);
			case ENCLOSING_METHOD:
				return readEnclosingMethod();
			case EXCEPTIONS:
				return readExceptions();
			case INNER_CLASSES:
				return readInnerClasses();
			case NEST_HOST:
				return readNestHost();
			case NEST_MEMBERS:
				return readNestMembers();
			case SOURCE_DEBUG_EXTENSION:
				return readSourceDebugExtension();
			case RUNTIME_INVISIBLE_ANNOTATIONS:
			case RUNTIME_VISIBLE_ANNOTATIONS:
				return readAnnotations(context);
			case RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				return readParameterAnnotations(context);
			case RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				return readTypeAnnotations(context);
			case ANNOTATION_DEFAULT:
				return readAnnotationDefault(context);
			case SYNTHETIC:
				return readSynthetic();
			case BOOTSTRAP_METHODS:
				return readBoostrapMethods();
			case SIGNATURE:
				return readSignature();
			case SOURCE_FILE:
				return readSourceFile();
			case Attributes.MODULE:
				return readModule();
			case STACK_MAP_TABLE:
				return readStackMapTable();
			case LINE_NUMBER_TABLE:
				return readLineNumbers();
			case LOCAL_VARIABLE_TABLE:
				return readLocalVariables();
			case LOCAL_VARIABLE_TYPE_TABLE:
				return readLocalVariableTypess();
			case PERMITTED_SUBCLASSES:
				return readPermittedClasses();
			case RECORD:
				return readRecord();
			case CHARACTER_RANGE_TABLE:
			case COMPILATION_ID:
			case METHOD_PARAMETERS:
			case MODULE_HASHES:
			case MODULE_MAIN_CLASS:
			case MODULE_PACKAGES:
			case MODULE_RESOLUTION:
			case MODULE_TARGET:
			case SOURCE_ID:
			default:
				break;
		}
		// No known/unhandled attribute length is less than 2.
		// So if that is given, we likely have an intentionally malformed attribute.
		if (expectedContentLength < 2) {
			logger.debug("Invalid attribute, its content length <= 1");
			is.skipBytes(expectedContentLength);
			return null;
		}
		// Default handling, skip remaining bytes
		is.skipBytes(expectedContentLength);
		return new DefaultAttribute(nameIndex, is.getBuffer());
	}

	/**
	 * @return Record attribute indicating the current class is a record, and details components of the record.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private RecordAttribute readRecord() throws IOException {
		List<RecordComponent> components = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int nameIndex = is.readUnsignedShort();
			int descIndex = is.readUnsignedShort();
			int numAttributes = is.readUnsignedShort();
			List<Attribute> attributes = new ArrayList<>();
			for (int x = 0; x < numAttributes; x++) {
				Attribute attr = new AttributeReader(reader, builder, is).readAttribute(AttributeContext.ATTRIBUTE);
				if (attr != null)
					attributes.add(attr);
			}
			components.add(new RecordComponent(nameIndex, descIndex, attributes));
		}
		return new RecordAttribute(nameIndex, components);
	}

	/**
	 * @return Permitted classes authorized to extend/implement the current class.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private PermittedClassesAttribute readPermittedClasses() throws IOException {
		List<Integer> entries = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int index = is.readUnsignedShort();
			entries.add(index);
		}
		return new PermittedClassesAttribute(nameIndex, entries);
	}

	/**
	 * @return Variable type table.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private LocalVariableTypeTableAttribute readLocalVariableTypess() throws IOException {
		List<VarTypeEntry> entries = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int startPc = is.readUnsignedShort();
			int length = is.readUnsignedShort();
			int name = is.readUnsignedShort();
			int sig = is.readUnsignedShort();
			int index = is.readUnsignedShort();
			entries.add(new VarTypeEntry(startPc, length, name, sig, index));
		}
		return new LocalVariableTypeTableAttribute(nameIndex, entries);
	}

	/**
	 * @return Variable table.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private LocalVariableTableAttribute readLocalVariables() throws IOException {
		List<VarEntry> entries = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int startPc = is.readUnsignedShort();
			int length = is.readUnsignedShort();
			int name = is.readUnsignedShort();
			int desc = is.readUnsignedShort();
			int index = is.readUnsignedShort();
			entries.add(new VarEntry(startPc, length, name, desc, index));
		}
		return new LocalVariableTableAttribute(nameIndex, entries);
	}

	/**
	 * @return Line number table.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private LineNumberTableAttribute readLineNumbers() throws IOException {
		List<LineEntry> entries = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int offset = is.readUnsignedShort();
			int line = is.readUnsignedShort();
			entries.add(new LineEntry(offset, line));
		}
		return new LineNumberTableAttribute(nameIndex, entries);
	}

	/**
	 * @return ModuleAttribute attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private ModuleAttribute readModule() throws IOException {
		int moduleIndex = is.readUnsignedShort();
		int flags = is.readUnsignedShort();
		int versionIndex = is.readUnsignedShort();
		List<Requires> requires = new ArrayList<>();
		int count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int reqIndex = is.readUnsignedShort();
			int reqFlags = is.readUnsignedShort();
			int reqVersion = is.readUnsignedShort();
			requires.add(new Requires(reqIndex, reqFlags, reqVersion));
		}
		List<Exports> exports = new ArrayList<>();
		count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int expIndex = is.readUnsignedShort();
			int expFlags = is.readUnsignedShort();
			int expCount = is.readUnsignedShort();
			List<Integer> indices = new ArrayList<>();
			for (int j = 0; j < expCount; j++) {
				indices.add(is.readUnsignedShort());
			}
			exports.add(new Exports(expIndex, expFlags, indices));
		}
		List<Opens> opens = new ArrayList<>();
		count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int openIndex = is.readUnsignedShort();
			int openFlags = is.readUnsignedShort();
			int openCount = is.readUnsignedShort();
			List<Integer> indices = new ArrayList<>();
			for (int j = 0; j < openCount; j++) {
				indices.add(is.readUnsignedShort());
			}
			opens.add(new Opens(openIndex, openFlags, indices));
		}
		List<Integer> uses = new ArrayList<>();
		count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			uses.add(is.readUnsignedShort());
		}
		List<Provides> provides = new ArrayList<>();
		count = is.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			int prvIndex = is.readUnsignedShort();
			int prvCount = is.readUnsignedShort();
			List<Integer> indices = new ArrayList<>();
			for (int j = 0; j < prvCount; j++) {
				indices.add(is.readUnsignedShort());
			}
			provides.add(new Provides(prvIndex, indices));
		}
		return new ModuleAttribute(nameIndex, moduleIndex, flags, versionIndex,
				requires, exports, opens, uses, provides);
	}

	/**
	 * @return Signature attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private SignatureAttribute readSignature() throws IOException {
		int signatureIndex = is.readUnsignedShort();
		return new SignatureAttribute(nameIndex, signatureIndex);
	}

	/**
	 * @return Source file name attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private SourceFileAttribute readSourceFile() throws IOException {
		int sourceFileNameIndex = is.readUnsignedShort();
		return new SourceFileAttribute(nameIndex, sourceFileNameIndex);
	}

	/**
	 * @return Enclosing method attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private EnclosingMethodAttribute readEnclosingMethod() throws IOException {
		int classIndex = is.readUnsignedShort();
		int methodIndex = is.readUnsignedShort();
		return new EnclosingMethodAttribute(nameIndex, classIndex, methodIndex);
	}

	/**
	 * @return Exceptions attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private ExceptionsAttribute readExceptions() throws IOException {
		int numberOfExceptionIndices = is.readUnsignedShort();
		List<Integer> exceptionIndexTable = new ArrayList<>();
		for (int i = 0; i < numberOfExceptionIndices; i++) {
			exceptionIndexTable.add(is.readUnsignedShort());
		}
		return new ExceptionsAttribute(nameIndex, exceptionIndexTable);
	}

	/**
	 * @return Inner classes attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private InnerClassesAttribute readInnerClasses() throws IOException {
		int numberOfInnerClasses = is.readUnsignedShort();
		List<InnerClass> innerClasses = new ArrayList<>();
		for (int i = 0; i < numberOfInnerClasses; i++) {
			int innerClassInfoIndex = is.readUnsignedShort();
			int outerClassInfoIndex = is.readUnsignedShort();
			int innerNameIndex = is.readUnsignedShort();
			int innerClassAccessFlags = is.readUnsignedShort();
			innerClasses.add(new InnerClass(innerClassInfoIndex, outerClassInfoIndex,
					innerNameIndex, innerClassAccessFlags));
		}
		return new InnerClassesAttribute(nameIndex, innerClasses);
	}

	/**
	 * @return Nest host attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private NestHostAttribute readNestHost() throws IOException {
		if (expectedContentLength != 2) {
			logger.debug("Found NestHost with illegal content length: {} != 2", expectedContentLength);
			return null;
		}
		int hostClassIndex = is.readUnsignedShort();
		return new NestHostAttribute(nameIndex, hostClassIndex);
	}

	/**
	 * @return Nest members attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private NestMembersAttribute readNestMembers() throws IOException {
		int count = is.readUnsignedShort();
		List<Integer> memberClassIndices = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int classIndex = is.readUnsignedShort();
			memberClassIndices.add(classIndex);
		}
		return new NestMembersAttribute(nameIndex, memberClassIndices);
	}

	/**
	 * @return Source debug attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private DebugExtensionAttribute readSourceDebugExtension() throws IOException {
		byte[] debugExtension = new byte[expectedContentLength];
		is.readFully(debugExtension);
		// Validate data represents UTF text
		try {
			new DataInputStream(new ByteArrayInputStream(debugExtension)).readUTF();
		} catch (Throwable t) {
			logger.debug("Invalid SourceDebugExtension, not a valid UTF");
			return null;
		}
		return new DebugExtensionAttribute(nameIndex, debugExtension);
	}

	/**
	 * @param context
	 * 		Location the annotation is defined in.
	 *
	 * @return Annotations attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private AnnotationsAttribute readAnnotations(AttributeContext context) throws IOException {
		return new AnnotationReader(reader, builder.getPool(), is, expectedContentLength, nameIndex, context)
				.readAnnotations();
	}

	/**
	 * @param context
	 * 		Location the annotation is defined in.
	 *
	 * @return ParameterAnnotations attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private ParameterAnnotationsAttribute readParameterAnnotations(AttributeContext context) throws IOException {
		return new AnnotationReader(reader, builder.getPool(), is, expectedContentLength, nameIndex, context)
				.readParameterAnnotations();
	}

	/**
	 * @param context
	 * 		Location the annotation is defined in.
	 *
	 * @return TypeAnnotation attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private AnnotationsAttribute readTypeAnnotations(AttributeContext context) throws IOException {
		return new AnnotationReader(reader, builder.getPool(), is, expectedContentLength, nameIndex, context)
				.readTypeAnnotations();
	}

	/**
	 * @param context
	 * 		Location the annotation is defined in.
	 *
	 * @return AnnotationDefault attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private AnnotationDefaultAttribute readAnnotationDefault(AttributeContext context) throws IOException {
		return new AnnotationReader(reader, builder.getPool(), is, expectedContentLength, nameIndex, context)
				.readAnnotationDefault();
	}

	/**
	 * @return Synthetic attribute.
	 */
	private SyntheticAttribute readSynthetic() {
		return new SyntheticAttribute(nameIndex);
	}

	/**
	 * @return Bootstrap methods attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private BootstrapMethodsAttribute readBoostrapMethods() throws IOException {
		List<BootstrapMethod> bootstrapMethods = new ArrayList<>();
		int bsmCount = is.readUnsignedShort();
		for (int i = 0; i < bsmCount; i++) {
			int methodRef = is.readUnsignedShort();
			int argCount = is.readUnsignedShort();
			List<Integer> args = new ArrayList<>();
			for (int j = 0; j < argCount; j++) {
				args.add(is.readUnsignedShort());
			}
			bootstrapMethods.add(new BootstrapMethod(methodRef, args));
		}
		return new BootstrapMethodsAttribute(nameIndex, bootstrapMethods);
	}

	/**
	 * @return Code attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private CodeAttribute readCode() throws IOException {
		int maxStack = -1;
		int maxLocals = -1;
		int codeLength = -1;
		byte[] code = null;
		List<ExceptionTableEntry> exceptions = new ArrayList<>();
		List<Attribute> attributes = new ArrayList<>();
		// Parse depending on class format version
		if (builder.isOakVersion()) {
			// Pre-java oak parsing (half-size data types)
			maxStack = is.readUnsignedByte();
			maxLocals = is.readUnsignedByte();
			codeLength = is.readUnsignedShort();
		} else {
			// Modern parsing
			maxStack = is.readUnsignedShort();
			maxLocals = is.readUnsignedShort();
			codeLength = is.readInt();
		}
		// Read instructions
		code = new byte[codeLength];
		is.readFully(code);
		// Read exceptions
		int numExceptions = is.readUnsignedShort();
		for (int i = 0; i < numExceptions; i++)
			exceptions.add(readCodeException());
		// Read attributes
		int numAttributes = is.readUnsignedShort();
		for (int i = 0; i < numAttributes; i++) {
			Attribute attr = new AttributeReader(reader, builder, is).readAttribute(AttributeContext.ATTRIBUTE);
			if (attr != null)
				attributes.add(attr);
		}
		return new CodeAttribute(nameIndex, maxStack, maxLocals, code, exceptions, attributes);
	}

	/**
	 * @return Exception table entry for code attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private CodeAttribute.ExceptionTableEntry readCodeException() throws IOException {
		return new CodeAttribute.ExceptionTableEntry(
				is.readUnsignedShort(),
				is.readUnsignedShort(),
				is.readUnsignedShort(),
				is.readUnsignedShort()
		);
	}

	/**
	 * @return Constant value attribute.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private ConstantValueAttribute readConstantValue() throws IOException {
		int valueIndex = is.readUnsignedShort();
		return new ConstantValueAttribute(nameIndex, valueIndex);
	}

	private StackMapTableAttribute readStackMapTable() throws IOException {
		int numEntries = is.readUnsignedShort();
		List<StackMapFrame> frames = new ArrayList<>(numEntries);
		for (int i = 0; i < numEntries; i++) {
			// u1: frame_type
			int frameType = is.readUnsignedByte();
			if (frameType <= SAME_FRAME_MAX) {
				// same_frame
				// The offset_delta is the frame_type
				frames.add(new StackMapTableAttribute.SameFrame(frameType));
			} else if (frameType <= SAME_LOCALS_ONE_STACK_ITEM_MAX) {
				// same_locals_1_stack_item_frame
				// The offset_delta is frame_type - 64
				// verification_type_info stack
				TypeInfo stack = readVerificationTypeInfo();
				frames.add(new StackMapTableAttribute.SameLocalsOneStackItem(
						frameType - 64,
						stack
				));
			} else if (frameType < SAME_LOCALS_ONE_STACK_ITEM_EXTENDED_MIN) {
				// Tags in the range [128-246] are reserved for future use.
				throw new IllegalArgumentException("Unknown stackframe tag " + frameType);
			} else if (frameType <= SAME_LOCALS_ONE_STACK_ITEM_EXTENDED_MAX) {
				// same_locals_1_stack_item_frame_extended
				// u2: offset_delta
				int offsetDelta = is.readUnsignedShort();
				// verification_type_info stack
				TypeInfo stack = readVerificationTypeInfo();
				frames.add(
						new StackMapTableAttribute.SameLocalsOneStackItemExtended(
								offsetDelta,
								stack
						)
				);
			} else if (frameType <= CHOP_FRAME_MAX) {
				// chop_frame
				// This frame type indicates that the frame has the same local
				// variables as the previous frame except that the last k local
				// variables are absent, and that the operand stack is empty. The
				// value of k is given by the formula 251 - frame_type.
				int k = 251 - frameType;
				// u2: offset_delta
				int offsetDelta = is.readUnsignedShort();
				frames.add(new StackMapTableAttribute.ChopFrame(offsetDelta, k));
			} else if (frameType < 252) {
				// same_frame_extended
				// u2: offset_delta
				int offsetDelta = is.readUnsignedShort();
				frames.add(new StackMapTableAttribute.SameFrameExtended(
						offsetDelta
				));
			} else if (frameType <= APPEND_FRAME_MAX) {
				// append_frame
				// u2: offset_delta
				int offsetDelta = is.readUnsignedShort();
				// verification_type_info locals[frame_type - 251]
				int numLocals = frameType - 251;
				List<TypeInfo> locals = new ArrayList<>(numLocals);
				for (int j = 0; j < numLocals; j++) {
					locals.add(readVerificationTypeInfo());
				}
				frames.add(new StackMapTableAttribute.AppendFrame(
						offsetDelta, locals
				));
			} else if (frameType <= FULL_FRAME_MAX) {
				// full_frame
				// u2: offset_delta
				int offsetDelta = is.readUnsignedShort();
				// verification_type_info locals[u2 number_of_locals]
				int numLocals = is.readUnsignedShort();
				List<TypeInfo> locals = new ArrayList<>(numLocals);
				for (int j = 0; j < numLocals; j++) {
					locals.add(readVerificationTypeInfo());
				}
				// verification_type_info stack[u2 number_of_stack_items]
				int numStackItems = is.readUnsignedShort();
				List<TypeInfo> stack = new ArrayList<>(numStackItems);
				for (int j = 0; j < numStackItems; j++) {
					stack.add(readVerificationTypeInfo());
				}
				frames.add(new StackMapTableAttribute.FullFrame(
						offsetDelta, locals, stack
				));
			} else {
				throw new IllegalArgumentException("Unknown frame type " + frameType);
			}
		}
		return new StackMapTableAttribute(nameIndex, frames);
	}

	private TypeInfo readVerificationTypeInfo() throws IOException {
		// u1 tag
		int tag = is.readUnsignedByte();
		switch (tag) {
			case ITEM_TOP:
				return new StackMapTableAttribute.TopVariableInfo();
			case ITEM_INTEGER:
				return new StackMapTableAttribute.IntegerVariableInfo();
			case ITEM_FLOAT:
				return new StackMapTableAttribute.FloatVariableInfo();
			case ITEM_DOUBLE:
				return new StackMapTableAttribute.DoubleVariableInfo();
			case ITEM_LONG:
				return new StackMapTableAttribute.LongVariableInfo();
			case ITEM_NULL:
				return new StackMapTableAttribute.NullVariableInfo();
			case ITEM_UNINITIALIZED_THIS:
				return new StackMapTableAttribute.UninitializedThisVariableInfo();
			case ITEM_OBJECT:
				// u2 cpool_index
				int cpoolIndex = is.readUnsignedShort();
				return new StackMapTableAttribute.ObjectVariableInfo(cpoolIndex);
			case ITEM_UNINITIALIZED:
				// u2 offset
				int offset = is.readUnsignedShort();
				return new StackMapTableAttribute.UninitializedVariableInfo(offset);
			default:
				throw new IllegalArgumentException("Unknown verification type tag " + tag);
		}
	}
}
