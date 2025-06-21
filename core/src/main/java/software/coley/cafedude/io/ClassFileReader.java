package software.coley.cafedude.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.cafedude.InvalidClassException;
import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.ConstantPoolConstants;
import software.coley.cafedude.classfile.Field;
import software.coley.cafedude.classfile.Method;
import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.constant.ConstDynamic;
import software.coley.cafedude.classfile.constant.ConstRef;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpDouble;
import software.coley.cafedude.classfile.constant.CpDynamic;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpFieldRef;
import software.coley.cafedude.classfile.constant.CpFloat;
import software.coley.cafedude.classfile.constant.CpInt;
import software.coley.cafedude.classfile.constant.CpInterfaceMethodRef;
import software.coley.cafedude.classfile.constant.CpInvokeDynamic;
import software.coley.cafedude.classfile.constant.CpLong;
import software.coley.cafedude.classfile.constant.CpMethodHandle;
import software.coley.cafedude.classfile.constant.CpMethodRef;
import software.coley.cafedude.classfile.constant.CpMethodType;
import software.coley.cafedude.classfile.constant.CpModule;
import software.coley.cafedude.classfile.constant.CpNameType;
import software.coley.cafedude.classfile.constant.CpPackage;
import software.coley.cafedude.classfile.constant.CpString;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.classfile.constant.Placeholders;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Class file format parser.
 *
 * @author Matt Coley
 * @see ClassFile Parsed class representation.
 * @see ClassFileWriter Class file format writer.
 */
public class ClassFileReader {
	private static final Logger logger = LoggerFactory.getLogger(ClassFileReader.class);
	private IndexableByteStream is;
	// config
	private boolean dropForwardVersioned = true;
	private boolean dropBadContextAttributes = true;
	private boolean dropEofAttributes = true;
	private boolean dropDupeAnnotations = true;
	private boolean checkCodeLength = true;

	/**
	 * Fallback reader. Default to always failing on any input.
	 */
	protected Supplier<FallbackInstructionReader> fallbackReaderSupplier = FallbackInstructionReader::fail;

	/**
	 * @param code
	 * 		Class bytecode to read.
	 *
	 * @return Parsed class file.
	 *
	 * @throws InvalidClassException
	 * 		When some class reading exception occurs.
	 */
	@Nonnull
	public ClassFile read(@Nonnull byte[] code) throws InvalidClassException {
		ClassBuilder builder = new ClassBuilder();
		try {
			try (IndexableByteStream is = new IndexableByteStream(code)) {
				this.is = is;

				// Read magic header
				if (is.readInt() != 0xCAFEBABE)
					throw new InvalidClassException("Does not start with 0xCAFEBABE");

				// Version
				builder.setVersionMinor(is.readUnsignedShort());
				builder.setVersionMajor(is.readUnsignedShort());

				// Constant pool
				int numConstants = is.readUnsignedShort();
				int start = is.getIndex();
				ConstPool constPool = builder.getPool();

				// first pass
				for (int i = 1; i < numConstants; i++) {
					CpEntry entry = readPoolEntryBasic();
					constPool.add(entry);
					if (entry.isWide()) {
						i++;
					}
				}

				// rewind
				int diff = is.getIndex() - start;
				is.reset(diff);

				// second pass
				for (int i = 1; i < numConstants; i++) {
					CpEntry entry = constPool.get(i);
					readPoolEntryResolve(constPool, entry);
					if (entry.isWide()) {
						i++;
					}
				}

				// Flags
				builder.setAccess(is.readUnsignedShort());

				// This/super classes
				builder.setThisClass((CpClass) constPool.get(is.readUnsignedShort()));
				builder.setSuperClass((CpClass) constPool.get(is.readUnsignedShort()));

				// Interfaces
				int numInterfaces = is.readUnsignedShort();
				for (int i = 0; i < numInterfaces; i++)
					builder.addInterface((CpClass) constPool.get(is.readUnsignedShort()));

				// Fields
				int numFields = is.readUnsignedShort();
				for (int i = 0; i < numFields; i++)
					builder.addField(readField(builder));

				// Methods
				int numMethods = is.readUnsignedShort();
				for (int i = 0; i < numMethods; i++)
					builder.addMethod(readMethod(builder));

				// Attributes
				int numAttributes = is.readUnsignedShort();
				for (int i = 0; i < numAttributes; i++) {
					Attribute attr = AttributeReader.readFromClass(this, builder, is, AttributeContext.CLASS);
					if (attr != null)
						builder.addAttribute(attr);
				}
				return builder.build();
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
	@Nonnull
	private CpEntry readPoolEntryBasic() throws IOException, InvalidClassException {
		int tag = is.readUnsignedByte();
		switch (tag) {
			case ConstantPoolConstants.UTF8:
				return new CpUtf8(is.readUTF());
			case ConstantPoolConstants.INTEGER:
				return new CpInt(is.readInt());
			case ConstantPoolConstants.FLOAT:
				return new CpFloat(is.readFloat());
			case ConstantPoolConstants.LONG:
				return new CpLong(is.readLong());
			case ConstantPoolConstants.DOUBLE:
				return new CpDouble(is.readDouble());
			case ConstantPoolConstants.STRING:
				is.readUnsignedShort();
				return new CpString(Placeholders.UTF8);
			case ConstantPoolConstants.CLASS:
				is.readUnsignedShort();
				return new CpClass(Placeholders.UTF8);
			case ConstantPoolConstants.FIELD_REF:
				is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpFieldRef(Placeholders.CLASS, Placeholders.NAME_TYPE);
			case ConstantPoolConstants.METHOD_REF:
				is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpMethodRef(Placeholders.CLASS, Placeholders.NAME_TYPE);
			case ConstantPoolConstants.INTERFACE_METHOD_REF:
				is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpInterfaceMethodRef(Placeholders.CLASS, Placeholders.NAME_TYPE);
			case ConstantPoolConstants.NAME_TYPE:
				is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpNameType(Placeholders.UTF8, Placeholders.UTF8);
			case ConstantPoolConstants.DYNAMIC:
				int bsmIndex = is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpDynamic(bsmIndex, Placeholders.NAME_TYPE);
			case ConstantPoolConstants.METHOD_HANDLE:
				byte refKind = is.readByte();
				is.readUnsignedShort();
				return new CpMethodHandle(refKind, Placeholders.CONST_REF);
			case ConstantPoolConstants.METHOD_TYPE:
				is.readUnsignedShort();
				return new CpMethodType(Placeholders.UTF8);
			case ConstantPoolConstants.INVOKE_DYNAMIC:
				int bsmIndex2 = is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpInvokeDynamic(bsmIndex2, Placeholders.NAME_TYPE);
			case ConstantPoolConstants.MODULE:
				is.readUnsignedShort();
				return new CpModule(Placeholders.UTF8);
			case ConstantPoolConstants.PACKAGE:
				is.readUnsignedShort();
				return new CpPackage(Placeholders.UTF8);
			default:
				throw new InvalidClassException("Unknown constant-pool tag: " + tag);
		}
	}

	private void readPoolEntryResolve(@Nonnull ConstPool constPool, @Nonnull CpEntry entry)
			throws IOException, InvalidClassException {
		int tag = entry.getTag();
		if (tag != is.readUnsignedByte())
			throw new InvalidClassException("Constant pool tag mismatch");
		switch (tag) {
			case ConstantPoolConstants.UTF8:
				is.readUTF();
				break;
			case ConstantPoolConstants.INTEGER:
				is.readInt();
				break;
			case ConstantPoolConstants.FLOAT:
				is.readFloat();
				break;
			case ConstantPoolConstants.LONG:
				is.readLong();
				break;
			case ConstantPoolConstants.DOUBLE:
				is.readDouble();
				break;
			case ConstantPoolConstants.STRING: {
				CpUtf8 utf8 = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpString string = (CpString) entry;
				string.setString(utf8);
				break;
			}
			case ConstantPoolConstants.CLASS: {
				CpUtf8 utf8 = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpClass clazz = (CpClass) entry;
				clazz.setName(utf8);
				break;
			}
			case ConstantPoolConstants.FIELD_REF:
			case ConstantPoolConstants.METHOD_REF:
			case ConstantPoolConstants.INTERFACE_METHOD_REF: {
				CpClass clazz = (CpClass) constPool.get(is.readUnsignedShort());
				CpNameType nameType = (CpNameType) constPool.get(is.readUnsignedShort());
				ConstRef ref = (ConstRef) entry;
				ref.setClassRef(clazz);
				ref.setNameType(nameType);
				break;
			}
			case ConstantPoolConstants.NAME_TYPE: {
				CpUtf8 name = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpUtf8 type = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpNameType nameType = (CpNameType) entry;
				nameType.setName(name);
				nameType.setType(type);
				break;
			}
			case ConstantPoolConstants.DYNAMIC:
			case ConstantPoolConstants.INVOKE_DYNAMIC: {
				is.readUnsignedShort();
				CpNameType nameType = (CpNameType) constPool.get(is.readUnsignedShort());
				ConstDynamic dynamic = (ConstDynamic) entry;
				dynamic.setNameType(nameType);
				break;
			}
			case ConstantPoolConstants.METHOD_HANDLE: {
				is.readByte();
				ConstRef ref = (ConstRef) constPool.get(is.readUnsignedShort());
				CpMethodHandle methodHandle = (CpMethodHandle) entry;
				methodHandle.setReference(ref);
				break;
			}
			case ConstantPoolConstants.METHOD_TYPE: {
				CpUtf8 type = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpMethodType methodType = (CpMethodType) entry;
				methodType.setDescriptor(type);
				break;
			}
			case ConstantPoolConstants.MODULE: {
				CpUtf8 name = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpModule module = (CpModule) entry;
				module.setName(name);
				break;
			}
			case ConstantPoolConstants.PACKAGE: {
				CpUtf8 name = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpPackage pkg = (CpPackage) entry;
				pkg.setPackageName(name);
				break;
			}
			default:
				throw new IOException("Unknown CP tag: " + tag);
		}
	}

	/**
	 * @param builder
	 * 		Class being built/read.
	 *
	 * @return Field member.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private Field readField(@Nonnull ClassBuilder builder) throws IOException {
		int access = is.readUnsignedShort();
		CpUtf8 name = (CpUtf8) builder.getPool().get(is.readUnsignedShort());
		CpUtf8 type = (CpUtf8) builder.getPool().get(is.readUnsignedShort());
		int numAttributes = is.readUnsignedShort();
		List<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < numAttributes; i++) {
			Attribute attr = AttributeReader.readFromClass(this, builder, is, AttributeContext.FIELD);
			if (attr != null)
				attributes.add(attr);
		}
		return new Field(attributes, access, name, type);
	}

	/**
	 * @param builder
	 * 		Class being built/read.
	 *
	 * @return Method member.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	private Method readMethod(@Nonnull ClassBuilder builder) throws IOException {
		int access = is.readUnsignedShort();
		CpUtf8 name = (CpUtf8) builder.getPool().get(is.readUnsignedShort());
		CpUtf8 type = (CpUtf8) builder.getPool().get(is.readUnsignedShort());
		int numAttributes = is.readUnsignedShort();
		List<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < numAttributes; i++) {
			Attribute attr = AttributeReader.readFromClass(this, builder, is, AttributeContext.METHOD);
			if (attr != null)
				attributes.add(attr);
		}
		return new Method(attributes, access, name, type);
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
	 * @return {@code true} if attributes declared in the wrongs contexts should be removed.
	 */
	public boolean doDropBadContextAttributes() {
		return dropBadContextAttributes;
	}

	/**
	 * @param dropBadContextAttributes
	 *        {@code true} if attributes declared in the wrong contexts should be removed.
	 */
	public void setDropBadContextAttributes(boolean dropBadContextAttributes) {
		this.dropBadContextAttributes = dropBadContextAttributes;
	}

	/**
	 * @return {@code true} if attributes that when parsed yield EOF exceptions should be removed.
	 */
	public boolean doDropEofAttributes() {
		return dropEofAttributes;
	}

	/**
	 * @param dropEofAttributes
	 *        {@code true} if attributes that when parsed yield EOF exceptions should be removed.
	 */
	public void setDropEofAttributes(boolean dropEofAttributes) {
		this.dropEofAttributes = dropEofAttributes;
	}

	/**
	 * @return {@code true} to automatically remove duplicate annotations on an item.
	 * There can only be at most one instance of an attribute of a given type targeting an item at a time.
	 */
	public boolean doDropDupeAnnotations() {
		return dropDupeAnnotations;
	}

	/**
	 * @param dropDupeAnnotations
	 *        {@code true} to automatically remove duplicate annotations on an item.
	 */
	public void setDropDupeAnnotations(boolean dropDupeAnnotations) {
		this.dropDupeAnnotations = dropDupeAnnotations;
	}

	/**
	 * @return {@code true} to automatically remove methods with bogus code lengths.
	 */
	public boolean doCheckCodeLength() {
		return checkCodeLength;
	}

	/**
	 * @param checkCodeLength
	 *        {@code true} to automatically remove methods with bogus code lengths.
	 */
	public void setCheckCodeLength(boolean checkCodeLength) {
		this.checkCodeLength = checkCodeLength;
	}
}