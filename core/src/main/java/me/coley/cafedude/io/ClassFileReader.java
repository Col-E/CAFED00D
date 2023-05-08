package me.coley.cafedude.io;

import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Field;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.constant.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static me.coley.cafedude.classfile.ConstantPoolConstants.*;

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
	private boolean dropEofAttributes = true;
	private boolean dropDupeAnnotations = true;
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
					Attribute attr = AttributeReader.read(this, builder, is, AttributeContext.CLASS);
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
				is.readUnsignedShort();
				return new CpString(Placeholders.UTF8);
			case CLASS:
				is.readUnsignedShort();
				return new CpClass(Placeholders.UTF8);
			case FIELD_REF:
				is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpFieldRef(Placeholders.CLASS, Placeholders.NAME_TYPE);
			case METHOD_REF:
				is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpMethodRef(Placeholders.CLASS, Placeholders.NAME_TYPE);
			case INTERFACE_METHOD_REF:
				is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpInterfaceMethodRef(Placeholders.CLASS, Placeholders.NAME_TYPE);
			case NAME_TYPE:
				is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpNameType(Placeholders.UTF8, Placeholders.UTF8);
			case DYNAMIC:
				int bsmIndex = is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpDynamic(bsmIndex, Placeholders.NAME_TYPE);
			case METHOD_HANDLE:
				byte refKind = is.readByte();
				is.readUnsignedShort();
				return new CpMethodHandle(refKind, Placeholders.CONST_REF);
			case METHOD_TYPE:
				is.readUnsignedShort();
				return new CpMethodType(Placeholders.UTF8);
			case INVOKE_DYNAMIC:
				int bsmIndex2 = is.readUnsignedShort();
				is.readUnsignedShort();
				return new CpInvokeDynamic(bsmIndex2, Placeholders.NAME_TYPE);
			case MODULE:
				is.readUnsignedShort();
				return new CpModule(Placeholders.UTF8);
			case PACKAGE:
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
			case UTF8:
				is.readUTF();
				break;
			case INTEGER:
				is.readInt();
				break;
			case FLOAT:
				is.readFloat();
				break;
			case LONG:
				is.readLong();
				break;
			case DOUBLE:
				is.readDouble();
				break;
			case STRING: {
				CpUtf8 utf8 = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpString string = (CpString) entry;
				string.setString(utf8);
				break;
			}
			case CLASS: {
				CpUtf8 utf8 = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpClass clazz = (CpClass) entry;
				clazz.setName(utf8);
				break;
			}
			case FIELD_REF:
			case METHOD_REF:
			case INTERFACE_METHOD_REF: {
				CpClass clazz = (CpClass) constPool.get(is.readUnsignedShort());
				CpNameType nameType = (CpNameType) constPool.get(is.readUnsignedShort());
				ConstRef ref = (ConstRef) entry;
				ref.setClassRef(clazz);
				ref.setNameType(nameType);
				break;
			}
			case NAME_TYPE: {
				CpUtf8 name = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpUtf8 type = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpNameType nameType = (CpNameType) entry;
				nameType.setName(name);
				nameType.setType(type);
				break;
			}
			case DYNAMIC:
			case INVOKE_DYNAMIC: {
				is.readUnsignedShort();
				CpNameType nameType = (CpNameType) constPool.get(is.readUnsignedShort());
				ConstDynamic dynamic = (ConstDynamic) entry;
				dynamic.setNameType(nameType);
				break;
			}
			case METHOD_HANDLE: {
				is.readByte();
				ConstRef ref = (ConstRef) constPool.get(is.readUnsignedShort());
				CpMethodHandle methodHandle = (CpMethodHandle) entry;
				methodHandle.setReference(ref);
				break;
			}
			case METHOD_TYPE: {
				CpUtf8 type = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpMethodType methodType = (CpMethodType) entry;
				methodType.setDescriptor(type);
				break;
			}
			case MODULE: {
				CpUtf8 name = (CpUtf8) constPool.get(is.readUnsignedShort());
				CpModule module = (CpModule) entry;
				module.setName(name);
				break;
			}
			case PACKAGE: {
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
			Attribute attr = AttributeReader.read(this, builder, is, AttributeContext.FIELD);
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
			Attribute attr = AttributeReader.read(this, builder, is, AttributeContext.METHOD);
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
}