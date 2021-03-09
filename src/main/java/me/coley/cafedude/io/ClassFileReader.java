package me.coley.cafedude.io;

import me.coley.cafedude.*;
import me.coley.cafedude.attribute.*;
import me.coley.cafedude.constant.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static me.coley.cafedude.Constants.*;
import static me.coley.cafedude.Constants.Attributes.*;
import static me.coley.cafedude.Constants.ConstantPool.*;

/**
 * Class file format parser.
 *
 * @author Matt Coley
 * @see ClassFile Parsed class representation.
 * @see ClassFileWriter Class file format writer.
 */
public class ClassFileReader {
	private static final Logger logger = LoggerFactory.getLogger(ClassFileReader.class);
	private DataInputStream is;
	private ConstPool pool;
	private boolean isOakVersion;
	private boolean isAnnotation;
	private int version;

	/**
	 * @param code
	 * 		Class bytecode to read.
	 *
	 * @return Parsed class file.
	 *
	 * @throws InvalidClassException
	 * 		When some class reading exception occurs.
	 */
	public ClassFile read(byte[] code) throws InvalidClassException {
		pool = new ConstPool();
		try {
			try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(code))) {
				this.is = is;
				// Read magic header
				if (is.readInt() != 0xCAFEBABE)
					throw new InvalidClassException("Does not start with 0xCAFEBABE");
				// Version
				int versionMinor = is.readUnsignedShort();
				int versionMajor = is.readUnsignedShort();
				version = versionMajor;
				isOakVersion = (versionMajor == JAVA1 && versionMinor <= 2) || (versionMajor < JAVA1);
				// Constant pool
				int numConstants = is.readUnsignedShort() - 1;
				while (pool.size() < numConstants)
					pool.add(readPoolEntry());
				// Flags
				int access = is.readUnsignedShort();
				isAnnotation = (access & Constants.ACC_ANNOTATION) != 0;
				// This/super classes
				int classIndex = is.readUnsignedShort();
				int superIndex = is.readUnsignedShort();
				// Interfaces
				int numInterfaces = is.readUnsignedShort();
				List<Integer> interfaces = new ArrayList<>();
				for (int i = 0; i < numInterfaces; i++)
					interfaces.add(is.readUnsignedShort());
				// Fields
				int numFields = is.readUnsignedShort();
				List<Field> fields = new ArrayList<>();
				for (int i = 0; i < numFields; i++)
					fields.add(readField());
				// Methods
				int numMethods = is.readUnsignedShort();
				List<Method> methods = new ArrayList<>();
				for (int i = 0; i < numMethods; i++)
					methods.add(readMethod());
				// Attributes
				int numAttributes = is.readUnsignedShort();
				List<Attribute> attributes = new ArrayList<>();
				for (int i = 0; i < numAttributes; i++) {
					Attribute attr = readAttribute(AttributeContext.CLASS);
					if (attr != null)
						attributes.add(attr);
				}
				return new ClassFile(
						versionMinor, versionMajor,
						pool,
						access,
						classIndex, superIndex,
						interfaces,
						fields,
						methods,
						attributes
				);
			} catch (IOException ex) {
				logger.debug("IO error reading class", ex);
				throw new InvalidClassException(ex);
			}
		} catch (Throwable t) {
			logger.debug("Error reading class", t);
			throw new InvalidClassException(t);
		}
	}

	/**
	 * @return Constant pool entry.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 * @throws InvalidClassException
	 * 		An unknown attribute is present.
	 */
	private ConstPoolEntry readPoolEntry() throws IOException, InvalidClassException {
		int tag = is.readUnsignedByte();
		switch (tag) {
			case UTF8:
				return new CpUtf8(is.readUTF());
			case INTEGER:
				return new CpInt(is.readInt());
			case FLOAT:
				return new CpFloat(is.readFloat());
			case LONG:
				return new CpLong(is.readLong());
			case DOUBLE:
				return new CpDouble(is.readDouble());
			case STRING:
				return new CpString(is.readUnsignedShort());
			case CLASS:
				return new CpClass(is.readUnsignedShort());
			case FIELD_REF:
				return new CpFieldRef(is.readUnsignedShort(), is.readUnsignedShort());
			case METHOD_REF:
				return new CpMethodRef(is.readUnsignedShort(), is.readUnsignedShort());
			case INTERFACE_METHOD_REF:
				return new CpInterfaceMethodRef(is.readUnsignedShort(), is.readUnsignedShort());
			case NAME_TYPE:
				return new CpNameType(is.readUnsignedShort(), is.readUnsignedShort());
			case DYNAMIC:
				return new CpDynamic(is.readUnsignedShort(), is.readUnsignedShort());
			case METHOD_HANDLE:
				return new CpMethodHandle(is.readByte(), is.readUnsignedShort());
			case METHOD_TYPE:
				return new CpMethodType(is.readUnsignedShort());
			case INVOKE_DYNAMIC:
				return new CpInvokeDynamic(is.readUnsignedShort(), is.readUnsignedShort());
			case ConstantPool.MODULE:
				return new CpModule(is.readUnsignedShort());
			case PACKAGE:
				return new CpPackage(is.readUnsignedShort());
			default:
				throw new InvalidClassException("Unknown constant-pool tag: " + tag);
		}
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
	private Attribute readAttribute(AttributeContext context) throws IOException {
		int nameIndex = is.readUnsignedShort();
		int length = is.readInt();
		String name = ((CpUtf8) pool.get(nameIndex)).getText();
		switch (name) {
			case CODE:
				// Check for illegal usage on non-method items
				if (context != AttributeContext.METHOD) {
					logger.info("Found Code declared in non-method context: {}", context.name());
					is.skipBytes(length);
					return null;
				}
				int maxStack = -1;
				int maxLocals = -1;
				int codeLength = -1;
				byte[] code = null;
				List<CodeAttribute.ExceptionTableEntry> exceptions = new ArrayList<>();
				List<Attribute> attributes = new ArrayList<>();
				// Parse depending on class format version
				if (isOakVersion) {
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
					// The reason for this null check is because illegal attributes return null and are dropped
					Attribute attr = readAttribute(AttributeContext.ATTRIBUTE);
					if (attr != null)
						attributes.add(attr);
				}
				return new CodeAttribute(nameIndex, maxStack, maxLocals, code, exceptions, attributes);
			case DEPRECATED:
				return new DeprecatedAttribute(nameIndex);
			case NEST_HOST:
				// Check for:
				//  - Illegal usage of code on non-class items
				//  - Usage in code below java 11
				if (version < Constants.JAVA11) {
					logger.debug("Found NestHost declared class below supported Java version");
					is.skipBytes(length);
					return null;
				}
				if (context != AttributeContext.CLASS) {
					logger.debug("Found NestHost applied in non-class context: {}", context.name());
					is.skipBytes(length);
					return null;
				}
				if (length != 2) {
					logger.debug("Found NestHost with illegal content length: {} != 2", length);
					is.skipBytes(length);
					return null;
				}
				int hostClassIndex = is.readUnsignedShort();
				// Check for illegal out of bounds entries
				if (hostClassIndex >= pool.size()) {
					// We've already read the bytes, so nothing to skip here
					logger.debug("Found NestHost with index out of pool range: {} >= {}", length, pool.size());
					return null;
				}
				return new NestHostAttribute(nameIndex, hostClassIndex);
			case NEST_MEMBERS:
				// Check for illegal usage on non-class items
				if (version < Constants.JAVA11) {
					logger.debug("Found NestMembers declared class below supported Java version");
					is.skipBytes(length);
					return null;
				}
				if (context != AttributeContext.CLASS) {
					logger.debug("Found NestMembers applied in non-class context: {}", context.name());
					is.skipBytes(length);
					return null;
				}
				int count = is.readUnsignedShort();
				List<Integer> memberClassIndices = new ArrayList<>();
				for (int i = 0; i < count; i++) {
					int classIndex = is.readUnsignedShort();
					// Check for illegal out of bounds entries
					if (classIndex >= pool.size()) {
						logger.debug("Found NestHost member with index out of pool range: {} >= {}",
								classIndex, pool.size());
						// count_u2 + count * (u2_classIndex)
						int alreadyRead = (2 + ((i + 1) * 2));
						is.skipBytes(length - alreadyRead);
						return null;
					}
					memberClassIndices.add(classIndex);
				}
				return new NestMembersAttribute(nameIndex, memberClassIndices);
			case SOURCE_DEBUG_EXTENSION:
				byte[] debugExtension = new byte[length];
				is.readFully(debugExtension);
				// Validate data represents UTF text
				try {
					new DataInputStream(new ByteArrayInputStream(debugExtension)).readUTF();
				} catch (Throwable t) {
					logger.debug("Invalid source-debug-extension, not a valid UTF");
					return null;
				}
				return new DebugExtensionAttribute(nameIndex, debugExtension);
			case RUNTIME_INVISIBLE_ANNOTATIONS:
			case RUNTIME_VISIBLE_ANNOTATIONS:
				return new AnnotationReader(is, length, nameIndex, context).readAnnotations();
			case RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				return new AnnotationReader(is, length, nameIndex, context).readParameterAnnotations();
			case RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				return new AnnotationReader(is, length, nameIndex, context).readTypeAnnotations();
			case ANNOTATION_DEFAULT:
				return new AnnotationReader(is, length, nameIndex, context).readAnnotationDefault();
			case SYNTHETIC:
				return new SyntheticAttribute(nameIndex);
			case BOOTSTRAP_METHODS:
			case CHARACTER_RANGE_TABLE:
			case COMPILATION_ID:
			case CONSTANT_VALUE:
			case ENCLOSING_METHOD:
			case EXCEPTIONS:
			case INNER_CLASSES:
			case LINE_NUMBER_TABLE:
			case LOCAL_VARIABLE_TABLE:
			case LOCAL_VARIABLE_TYPE_TABLE:
			case METHOD_PARAMETERS:
			case Attributes.MODULE:
			case MODULE_HASHES:
			case MODULE_MAIN_CLASS:
			case MODULE_PACKAGES:
			case MODULE_RESOLUTION:
			case MODULE_TARGET:
			case PERMITTED_SUBCLASSES:
			case RECORD:
			case SIGNATURE:
			case SOURCE_FILE:
			case SOURCE_ID:
			case STACK_MAP:
			case STACK_MAP_TABLE:
			default:
				// No known/unhandled attribute length is less than 2.
				// So if that is given, we likely have an intentionally malformed attribute.
				if (length < 2) {
					logger.debug("Invalid attribute, its content length <= 1");
					is.skipBytes(length);
					return null;
				}
				byte[] data = new byte[length];
				is.readFully(data);
				return new DefaultAttribute(nameIndex, data);
		}
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
	 * @return Field member.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private Field readField() throws IOException {
		int access = is.readUnsignedShort();
		int nameIndex = is.readUnsignedShort();
		int typeIndex = is.readUnsignedShort();
		int numAttributes = is.readUnsignedShort();
		List<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < numAttributes; i++) {
			Attribute attr = readAttribute(AttributeContext.FIELD);
			if (attr != null)
				attributes.add(attr);
		}
		return new Field(attributes, access, nameIndex, typeIndex);
	}

	/**
	 * @return Method member.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private Method readMethod() throws IOException {
		int access = is.readUnsignedShort();
		int nameIndex = is.readUnsignedShort();
		int typeIndex = is.readUnsignedShort();
		int numAttributes = is.readUnsignedShort();
		List<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < numAttributes; i++) {
			Attribute attr = readAttribute(AttributeContext.METHOD);
			if (attr != null)
				attributes.add(attr);
		}
		return new Method(attributes, access, nameIndex, typeIndex);
	}

}