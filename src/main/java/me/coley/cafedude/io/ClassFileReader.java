package me.coley.cafedude.io;

import me.coley.cafedude.*;
import me.coley.cafedude.attribute.*;
import me.coley.cafedude.constant.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static me.coley.cafedude.constant.ConstPoolEntry.*;

/**
 * Class file format parser.
 *
 * @author Matt Coley
 * @see ClassFile Parsed class representation.
 * @see ClassFileWriter Class file format writer.
 */
public class ClassFileReader {
	private DataInputStream is;
	private ConstPool pool;
	private boolean isOakVersion;

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
				isOakVersion = (versionMajor == 45 && versionMinor <= 2) || (versionMajor < 45);
				// Constant pool
				int numConstants = is.readUnsignedShort() - 1;
				while (pool.size() < numConstants)
					pool.add(readPoolEntry());
				// Flags
				int access = is.readUnsignedShort();
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
				throw new InvalidClassException(ex);
			}
		} catch (Exception t) {
			t.printStackTrace();
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
			case MODULE:
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
			case Attribute.CODE:
				// Check for illegal usage of code on non-method items
				if (context != AttributeContext.METHOD) {
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
					Attribute attr = readAttribute(AttributeContext.ATTRIBUTE);
					if (attr != null)
						attributes.add(attr);
				}
				return new CodeAttribute(nameIndex, maxStack, maxLocals, code, exceptions, attributes);
			case Attribute.DEPRECATED:
				return new DeprecatedAttribute(nameIndex);
			case Attribute.SOURCE_DEBUG_EXTENSION:
				byte[] debugExtension = new byte[length];
				is.readFully(debugExtension);
				return new DebugExtensionAttribute(nameIndex, debugExtension);
			case Attribute.SYNTHETIC:
				return new SyntheticAttribute(nameIndex);
			case Attribute.ANNOTATION_DEFAULT:
			case Attribute.BOOTSTRAP_METHODS:
			case Attribute.CHARACTER_RANGE_TABLE:
			case Attribute.COMPILATION_ID:
			case Attribute.CONSTANT_VALUE:
			case Attribute.ENCLOSING_METHOD:
			case Attribute.EXCEPTIONS:
			case Attribute.INNER_CLASSES:
			case Attribute.LINE_NUMBER_TABLE:
			case Attribute.LOCAL_VARIABLE_TABLE:
			case Attribute.LOCAL_VARIABLE_TYPE_TABLE:
			case Attribute.METHOD_PARAMETERS:
			case Attribute.MODULE:
			case Attribute.MODULE_HASHES:
			case Attribute.MODULE_MAIN_CLASS:
			case Attribute.MODULE_PACKAGES:
			case Attribute.MODULE_RESOLUTION:
			case Attribute.MODULE_TARGET:
			case Attribute.NEST_HOST:
			case Attribute.NEST_MEMBERS:
			case Attribute.PERMITTED_SUBCLASSES:
			case Attribute.RECORD:
			case Attribute.RUNTIME_INVISIBLE_ANNOTATIONS:
			case Attribute.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case Attribute.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case Attribute.RUNTIME_VISIBLE_ANNOTATIONS:
			case Attribute.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
			case Attribute.RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
			case Attribute.SIGNATURE:
			case Attribute.SOURCE_FILE:
			case Attribute.SOURCE_ID:
			case Attribute.STACK_MAP:
			case Attribute.STACK_MAP_TABLE:
			default:
				// No known/unhandled attribute length is less than 2.
				// So if that is given, we likely have an intentionally malformed attribute.
				if (length < 2 &&
						!(name.equals(Attribute.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS) ||
								name.equals(Attribute.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS) ||
								name.equals(Attribute.METHOD_PARAMETERS)))
				{
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

	/**
	 * Indicates where attribute is applied to.
	 *
	 * @author Matt Coley
	 */
	private enum AttributeContext {
		CLASS, FIELD, METHOD, ATTRIBUTE
	}
}