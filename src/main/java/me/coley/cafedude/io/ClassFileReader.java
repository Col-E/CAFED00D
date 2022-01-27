package me.coley.cafedude.io;

import me.coley.cafedude.ClassFile;
import me.coley.cafedude.Constants;
import me.coley.cafedude.Constants.ConstantPool;
import me.coley.cafedude.Field;
import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.Method;
import me.coley.cafedude.attribute.Attribute;
import me.coley.cafedude.attribute.CodeAttribute;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static me.coley.cafedude.Constants.ConstantPool.*;
import static me.coley.cafedude.attribute.AttributeCpAccessValidator.isValid;

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
	private boolean dropIllegalCpRefs = true;
	private boolean dropEofAttributes = true;
	private boolean dropDupeAnnotations = true;

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
				for (int i = 1; i < numConstants; i++) {
					ConstPoolEntry entry = readPoolEntry();
					builder.getPool().add(entry);
					if (entry.isWide()) {
						i++;
					}
				}
				// Flags
				builder.setAccess(is.readUnsignedShort());
				// This/super classes
				builder.setClassIndex(is.readUnsignedShort());
				builder.setSuperIndex(is.readUnsignedShort());
				// Interfaces
				int numInterfaces = is.readUnsignedShort();
				for (int i = 0; i < numInterfaces; i++)
					builder.addInterface(is.readUnsignedShort());
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
					Attribute attr = new AttributeReader(this, builder, is).readAttribute(AttributeContext.CLASS);
					if (attr != null && (!doDropIllegalCpRefs() || isValid(builder.getPool(), attr)))
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
	 * @param builder
	 * 		Class being built/read.
	 *
	 * @return Field member.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	private Field readField(ClassBuilder builder) throws IOException {
		int access = is.readUnsignedShort();
		int nameIndex = is.readUnsignedShort();
		int typeIndex = is.readUnsignedShort();
		int numAttributes = is.readUnsignedShort();
		List<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < numAttributes; i++) {
			Attribute attr = new AttributeReader(this, builder, is).readAttribute(AttributeContext.FIELD);
			if (attr != null && (!doDropIllegalCpRefs() || isValid(builder.getPool(), attr)))
				attributes.add(attr);
		}
		return new Field(attributes, access, nameIndex, typeIndex);
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
	private Method readMethod(ClassBuilder builder) throws IOException {
		int access = is.readUnsignedShort();
		int nameIndex = is.readUnsignedShort();
		int typeIndex = is.readUnsignedShort();
		int numAttributes = is.readUnsignedShort();
		List<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < numAttributes; i++) {
			Attribute attr = new AttributeReader(this, builder, is).readAttribute(AttributeContext.METHOD);
			if (doDropIllegalCpRefs() && attr instanceof CodeAttribute && (access & Constants.ACC_ABSTRACT) > 0) {
				String methodName = builder.getPool().getUtf(nameIndex);
				logger.debug("Illegal code attribute on abstract method {}", methodName);
				continue;
			}
			if (attr != null && (!doDropIllegalCpRefs() || isValid(builder.getPool(), attr)))
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
	 * @param dropIllegalCpRefs
	 *        {@code true} if attributes with CP refs to illegal positions should be removed.
	 *        {@code false} to ignore and keep illegal positions.
	 */
	public void setDropIllegalCpRefs(boolean dropIllegalCpRefs) {
		this.dropIllegalCpRefs = dropIllegalCpRefs;
	}
}