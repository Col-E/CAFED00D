package me.coley.cafedude.io;

import me.coley.cafedude.ClassFile;
import me.coley.cafedude.ConstPool;
import me.coley.cafedude.Constants;
import me.coley.cafedude.Constants.Attributes;
import me.coley.cafedude.Constants.ConstantPool;
import me.coley.cafedude.Field;
import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.Method;
import me.coley.cafedude.attribute.Attribute;
import me.coley.cafedude.attribute.AttributeContexts;
import me.coley.cafedude.attribute.AttributeCpAccessValidator;
import me.coley.cafedude.attribute.AttributeVersions;
import me.coley.cafedude.attribute.BootstrapMethodsAttribute;
import me.coley.cafedude.attribute.BootstrapMethodsAttribute.BootstrapMethod;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.coley.cafedude.Constants.Attributes.*;
import static me.coley.cafedude.Constants.ConstantPool.*;
import static me.coley.cafedude.Constants.JAVA1;

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
	private boolean dropIllegalCpRefs = true;

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
					if (attr != null && (!doDropIllegalCpRefs() || AttributeCpAccessValidator.isValid(pool, attr)))
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
		if (doDropForwardVersioned()) {
			int introducedAt = AttributeVersions.getIntroducedVersion(name);
			if (introducedAt > version) {
				logger.debug("Found '{}' in class version {}, min supported is {}", name, version, introducedAt);
				is.skipBytes(length);
				return null;
			}
		}
		// Check for illegal usage contexts
		Collection<AttributeContext> allowedContexts = AttributeContexts.getAllowedContexts(name);
		if (!allowedContexts.contains(context)) {
			logger.debug("Found '{}' declared in illegal context {}, allowed contexts: {}",
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
					// The reason for this null check is because illegal attributes return null and are dropped.
					// The second validation check asserts that all CP refs in the attribute point to valid
					// indices and are of the expected types.
					Attribute attr = readAttribute(AttributeContext.ATTRIBUTE);
					if (attr != null && (!doDropIllegalCpRefs() || AttributeCpAccessValidator.isValid(pool, attr)))
						attributes.add(attr);
				}
				return new CodeAttribute(nameIndex, maxStack, maxLocals, code, exceptions, attributes);
			case CONSTANT_VALUE:
				int valueIndex = is.readUnsignedShort();
				return new ConstantValueAttribute(nameIndex, valueIndex);
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
				return new NestHostAttribute(nameIndex, hostClassIndex);
			case NEST_MEMBERS:
				int count = is.readUnsignedShort();
				List<Integer> memberClassIndices = new ArrayList<>();
				for (int i = 0; i < count; i++) {
					int classIndex = is.readUnsignedShort();
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
				return new AnnotationReader(pool, is, length, nameIndex, context).readAnnotations();
			case RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				return new AnnotationReader(pool, is, length, nameIndex, context).readParameterAnnotations();
			case RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				return new AnnotationReader(pool, is, length, nameIndex, context).readTypeAnnotations();
			case ANNOTATION_DEFAULT:
				return new AnnotationReader(pool, is, length, nameIndex, context).readAnnotationDefault();
			case SYNTHETIC:
				return new SyntheticAttribute(nameIndex);
			case BOOTSTRAP_METHODS:
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
				break;
		}
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
			if (attr != null && (!doDropIllegalCpRefs() || AttributeCpAccessValidator.isValid(pool, attr)))
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
			if (attr != null && (!doDropIllegalCpRefs() || AttributeCpAccessValidator.isValid(pool, attr)))
				attributes.add(attr);
		}
		return new Method(attributes, access, nameIndex, typeIndex);
	}

	/**
	 * @return {@code true} if attributes declared from future versions should be removed.
	 */
	public boolean doDropForwardVersioned() {
		return dropForwardVersioned;
	}

	/**
	 * @param dropForwardVersioned
	 *        {@code true} if attributes declared from future versions should be removed.
	 */
	public void setDropForwardVersioned(boolean dropForwardVersioned) {
		this.dropForwardVersioned = dropForwardVersioned;
	}

	/**
	 * @return {@code true} if attributes with CP refs to illegal positions should be removed.
	 */
	public boolean doDropIllegalCpRefs() {
		return dropIllegalCpRefs;
	}

	/**
	 * @param dropIllegalCpRefs
	 *        {@code true} if attributes with CP refs to illegal positions should be removed.
	 *        {@code false} to ignore and keep illegal positions.
	 */
	public void setDropIllegalCpRefs(boolean dropIllegalCpRefs) {
		this.dropIllegalCpRefs = dropIllegalCpRefs;
	}
}