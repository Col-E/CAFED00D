package me.coley.cafedude.io;

import me.coley.cafedude.ConstPool;
import me.coley.cafedude.Constants.Attributes;
import me.coley.cafedude.attribute.AnnotationDefaultAttribute;
import me.coley.cafedude.attribute.AnnotationsAttribute;
import me.coley.cafedude.attribute.Attribute;
import me.coley.cafedude.attribute.AttributeContexts;
import me.coley.cafedude.attribute.AttributeCpAccessValidator;
import me.coley.cafedude.attribute.AttributeVersions;
import me.coley.cafedude.attribute.BootstrapMethodsAttribute;
import me.coley.cafedude.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import me.coley.cafedude.attribute.CodeAttribute;
import me.coley.cafedude.attribute.CodeAttribute.ExceptionTableEntry;
import me.coley.cafedude.attribute.ConstantValueAttribute;
import me.coley.cafedude.attribute.DebugExtensionAttribute;
import me.coley.cafedude.attribute.DefaultAttribute;
import me.coley.cafedude.attribute.DeprecatedAttribute;
import me.coley.cafedude.attribute.EnclosingMethodAttribute;
import me.coley.cafedude.attribute.ExceptionsAttribute;
import me.coley.cafedude.attribute.InnerClassesAttribute;
import me.coley.cafedude.attribute.InnerClassesAttribute.InnerClass;
import me.coley.cafedude.attribute.ModuleAttribute;
import me.coley.cafedude.attribute.ModuleAttribute.Exports;
import me.coley.cafedude.attribute.ModuleAttribute.Opens;
import me.coley.cafedude.attribute.ModuleAttribute.Provides;
import me.coley.cafedude.attribute.ModuleAttribute.Requires;
import me.coley.cafedude.attribute.NestHostAttribute;
import me.coley.cafedude.attribute.NestMembersAttribute;
import me.coley.cafedude.attribute.ParameterAnnotationsAttribute;
import me.coley.cafedude.attribute.SignatureAttribute;
import me.coley.cafedude.attribute.SourceFileAttribute;
import me.coley.cafedude.attribute.StackMapTableAttribute;
import me.coley.cafedude.attribute.SyntheticAttribute;
import me.coley.cafedude.constant.CpUtf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.coley.cafedude.Constants.Attributes.*;

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
	 * @param attribute
	 * 		Attribute to check.
	 *
	 * @return {@code true} when the attribute is valid, containing no illegal CP index references.
	 */
	private boolean shouldAddAttribute(Attribute attribute) {
		if (attribute == null)
			return false;
		return !reader.doDropIllegalCpRefs() || AttributeCpAccessValidator.isValid(builder.getPool(), attribute);
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
				logger.debug("Invalid '{}', claimed to be {} bytes, but was {}", name, expectedContentLength, read);
				return null;
			}
			return attribute;
		} catch (IOException ex) {
			if (reader.doDropEofAttributes()) {
				String name = ((CpUtf8) builder.getPool().get(nameIndex)).getText();
				logger.debug("Invalid '{}', EOF thrown when parsing attribute, expected {} bytes",
						name, expectedContentLength);
				return null;
			} else
				throw ex;
		}
	}

	private Attribute read(AttributeContext context) throws IOException {
		ConstPool pool = builder.getPool();
		String name = ((CpUtf8) pool.get(nameIndex)).getText();
		// Check for illegally inserted attributes from future versions
		if (reader.doDropForwardVersioned()) {
			int introducedAt = AttributeVersions.getIntroducedVersion(name);
			if (introducedAt > builder.getVersionMajor()) {
				logger.debug("Found '{}' in class version {}, min supported is {}",
						name, builder.getVersionMajor(), introducedAt);
				return null;
			}
		}
		// Check for illegal usage contexts
		Collection<AttributeContext> allowedContexts = AttributeContexts.getAllowedContexts(name);
		if (!allowedContexts.contains(context)) {
			logger.debug("Found '{}' declared in illegal context {}, allowed contexts: {}",
					name, context.name(), allowedContexts);
			return null;
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
			case CHARACTER_RANGE_TABLE:
			case COMPILATION_ID:
			case LINE_NUMBER_TABLE:
			case LOCAL_VARIABLE_TABLE:
			case LOCAL_VARIABLE_TYPE_TABLE:
			case METHOD_PARAMETERS:
			case MODULE_HASHES:
			case MODULE_MAIN_CLASS:
			case MODULE_PACKAGES:
			case MODULE_RESOLUTION:
			case MODULE_TARGET:
			case PERMITTED_SUBCLASSES:
			case RECORD:
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
			// The reason for this null check is because illegal attributes return null and are dropped.
			// The second validation check asserts that all CP refs in the attribute point to valid
			// indices and are of the expected types.
			Attribute attr = new AttributeReader(reader, builder, is).readAttribute(AttributeContext.ATTRIBUTE);
			if (shouldAddAttribute(attr))
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
		throw new RuntimeException();
	}
}
