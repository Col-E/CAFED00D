package me.coley.cafedude.io;

import static me.coley.cafedude.Constants.JAVA1;
import static me.coley.cafedude.Constants.Attributes.ANNOTATION_DEFAULT;
import static me.coley.cafedude.Constants.Attributes.BOOTSTRAP_METHODS;
import static me.coley.cafedude.Constants.Attributes.CHARACTER_RANGE_TABLE;
import static me.coley.cafedude.Constants.Attributes.CODE;
import static me.coley.cafedude.Constants.Attributes.COMPILATION_ID;
import static me.coley.cafedude.Constants.Attributes.CONSTANT_VALUE;
import static me.coley.cafedude.Constants.Attributes.DEPRECATED;
import static me.coley.cafedude.Constants.Attributes.ENCLOSING_METHOD;
import static me.coley.cafedude.Constants.Attributes.EXCEPTIONS;
import static me.coley.cafedude.Constants.Attributes.INNER_CLASSES;
import static me.coley.cafedude.Constants.Attributes.LINE_NUMBER_TABLE;
import static me.coley.cafedude.Constants.Attributes.LOCAL_VARIABLE_TABLE;
import static me.coley.cafedude.Constants.Attributes.LOCAL_VARIABLE_TYPE_TABLE;
import static me.coley.cafedude.Constants.Attributes.METHOD_PARAMETERS;
import static me.coley.cafedude.Constants.Attributes.MODULE_HASHES;
import static me.coley.cafedude.Constants.Attributes.MODULE_MAIN_CLASS;
import static me.coley.cafedude.Constants.Attributes.MODULE_PACKAGES;
import static me.coley.cafedude.Constants.Attributes.MODULE_RESOLUTION;
import static me.coley.cafedude.Constants.Attributes.MODULE_TARGET;
import static me.coley.cafedude.Constants.Attributes.NEST_HOST;
import static me.coley.cafedude.Constants.Attributes.NEST_MEMBERS;
import static me.coley.cafedude.Constants.Attributes.PERMITTED_SUBCLASSES;
import static me.coley.cafedude.Constants.Attributes.RECORD;
import static me.coley.cafedude.Constants.Attributes.RUNTIME_INVISIBLE_ANNOTATIONS;
import static me.coley.cafedude.Constants.Attributes.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS;
import static me.coley.cafedude.Constants.Attributes.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS;
import static me.coley.cafedude.Constants.Attributes.RUNTIME_VISIBLE_ANNOTATIONS;
import static me.coley.cafedude.Constants.Attributes.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS;
import static me.coley.cafedude.Constants.Attributes.RUNTIME_VISIBLE_TYPE_ANNOTATIONS;
import static me.coley.cafedude.Constants.Attributes.SIGNATURE;
import static me.coley.cafedude.Constants.Attributes.SOURCE_DEBUG_EXTENSION;
import static me.coley.cafedude.Constants.Attributes.SOURCE_FILE;
import static me.coley.cafedude.Constants.Attributes.SOURCE_ID;
import static me.coley.cafedude.Constants.Attributes.STACK_MAP_TABLE;
import static me.coley.cafedude.Constants.Attributes.SYNTHETIC;
import static me.coley.cafedude.Constants.ConstantPool.CLASS;
import static me.coley.cafedude.Constants.ConstantPool.DOUBLE;
import static me.coley.cafedude.Constants.ConstantPool.DYNAMIC;
import static me.coley.cafedude.Constants.ConstantPool.FIELD_REF;
import static me.coley.cafedude.Constants.ConstantPool.FLOAT;
import static me.coley.cafedude.Constants.ConstantPool.INTEGER;
import static me.coley.cafedude.Constants.ConstantPool.INTERFACE_METHOD_REF;
import static me.coley.cafedude.Constants.ConstantPool.INVOKE_DYNAMIC;
import static me.coley.cafedude.Constants.ConstantPool.LONG;
import static me.coley.cafedude.Constants.ConstantPool.METHOD_HANDLE;
import static me.coley.cafedude.Constants.ConstantPool.METHOD_REF;
import static me.coley.cafedude.Constants.ConstantPool.METHOD_TYPE;
import static me.coley.cafedude.Constants.ConstantPool.NAME_TYPE;
import static me.coley.cafedude.Constants.ConstantPool.PACKAGE;
import static me.coley.cafedude.Constants.ConstantPool.STRING;
import static me.coley.cafedude.Constants.ConstantPool.UTF8;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.coley.cafedude.attribute.AttributeContexts;
import me.coley.cafedude.attribute.AttributeVersions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.coley.cafedude.ClassFile;
import me.coley.cafedude.ConstPool;
import me.coley.cafedude.Constants;
import me.coley.cafedude.Constants.Attributes;
import me.coley.cafedude.Constants.ConstantPool;
import me.coley.cafedude.Field;
import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.Method;
import me.coley.cafedude.attribute.Attribute;
import me.coley.cafedude.attribute.CodeAttribute;
import me.coley.cafedude.attribute.ConstantValueAttribute;
import me.coley.cafedude.attribute.DebugExtensionAttribute;
import me.coley.cafedude.attribute.DefaultAttribute;
import me.coley.cafedude.attribute.DeprecatedAttribute;
import me.coley.cafedude.attribute.EnclosingMethodAttribute;
import me.coley.cafedude.attribute.ExceptionsAttribute;
import me.coley.cafedude.attribute.InnerClassesAttribute;
import me.coley.cafedude.attribute.InnerClassesAttribute.InnerClass;
import me.coley.cafedude.attribute.NestHostAttribute;
import me.coley.cafedude.attribute.NestMembersAttribute;
import me.coley.cafedude.attribute.SyntheticAttribute;
import me.coley.cafedude.constant.ConstPoolEntry;
import me.coley.cafedude.constant.CpClass;
import me.coley.cafedude.constant.CpDouble;
import me.coley.cafedude.constant.CpDynamic;
import me.coley.cafedude.constant.CpFieldRef;
import me.coley.cafedude.constant.CpFloat;
import me.coley.cafedude.constant.CpInt;
import me.coley.cafedude.constant.CpInterfaceMethodRef;
import me.coley.cafedude.constant.CpInvokeDynamic;
import me.coley.cafedude.constant.CpLong;
import me.coley.cafedude.constant.CpMethodHandle;
import me.coley.cafedude.constant.CpMethodRef;
import me.coley.cafedude.constant.CpMethodType;
import me.coley.cafedude.constant.CpModule;
import me.coley.cafedude.constant.CpNameType;
import me.coley.cafedude.constant.CpPackage;
import me.coley.cafedude.constant.CpString;
import me.coley.cafedude.constant.CpUtf8;

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
	// config
	private boolean dropForwardVersioned = true;

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
		// Check for illegally inserted attributes from future versions
		if (dropForwardVersioned) {
			int introducedAt = AttributeVersions.getIntroducedVersion(name);
			if (introducedAt > version) {
				logger.warn("Found '{}' in class version {}, min supported is {}", name, version, introducedAt);
				is.skipBytes(length);
				return null;
			}
		}
		// Check for illegal usage contexts
		Collection<AttributeContext> allowedContexts = AttributeContexts.getAllowedContexts(name);
		if (!allowedContexts.contains(context)) {
			logger.info("Found '{}' declared in illegal context {}, allowed contexts: {}",
					name, context.name(), allowedContexts);
			is.skipBytes(length);
			return null;
		}
		switch (name) {
			case CODE:
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
			case CONSTANT_VALUE:
				return new ConstantValueAttribute(nameIndex, is.readUnsignedShort());
			case DEPRECATED:
				return new DeprecatedAttribute(nameIndex);
			case ENCLOSING_METHOD:
				return new EnclosingMethodAttribute(nameIndex, is.readUnsignedShort(), is.readUnsignedShort());
			case EXCEPTIONS:
				int numberOfExceptionIndices = is.readUnsignedShort();
				int[] exceptionIndexTable = new int[numberOfExceptionIndices];
				for(int i = 0; i < numberOfExceptionIndices; i++) {
					exceptionIndexTable[i] = is.readUnsignedShort();
				}
				return new ExceptionsAttribute(nameIndex, exceptionIndexTable);
			case INNER_CLASSES:
				int numberOfInnerClasses = is.readUnsignedShort();
				InnerClass[] innerClasses = new InnerClass[numberOfInnerClasses];
				for(int i = 0; i < numberOfInnerClasses; i++) {
					innerClasses[i] = new InnerClass(is.readUnsignedShort(), 
							is.readUnsignedShort(), is.readUnsignedShort(), is.readUnsignedShort());
				}
				return new InnerClassesAttribute(nameIndex, innerClasses);
			case NEST_HOST:
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