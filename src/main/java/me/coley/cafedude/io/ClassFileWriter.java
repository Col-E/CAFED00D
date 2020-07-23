package me.coley.cafedude.io;

import me.coley.cafedude.ClassFile;
import me.coley.cafedude.Field;
import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.Method;
import me.coley.cafedude.attribute.*;
import me.coley.cafedude.constant.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static me.coley.cafedude.constant.ConstPoolEntry.*;

/**
 * Class file format writer.
 *
 * @author Matt Coley
 * @see ClassFile Parsed class representation.
 * @see ClassFileWriter Class file format writer.
 */
public class ClassFileWriter {
	private DataOutputStream out;

	/**
	 * @param clazz
	 * 		Parsed class file.
	 *
	 * @return Bytecode of class.
	 *
	 * @throws InvalidClassException
	 * 		when the class cannot be written.
	 */
	public byte[] write(ClassFile clazz) throws InvalidClassException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(baos)) {
			this.out = out;
			// Write magic header
			out.writeInt(0xCAFEBABE);
			// Version
			out.writeShort(clazz.getVersionMinor());
			out.writeShort(clazz.getVersionMajor());
			// Constant pool
			out.writeShort(clazz.getPool().size() + 1);
			for (ConstPoolEntry entry : clazz.getPool())
				writeCpEntry(entry);
			// Flags
			out.writeShort(clazz.getAccess());
			// This/super classes
			out.writeShort(clazz.getClassIndex());
			out.writeShort(clazz.getSuperIndex());
			// Interfaces
			out.writeShort(clazz.getInterfaceIndices().size());
			for (int interfaceIdx : clazz.getInterfaceIndices())
				out.writeShort(interfaceIdx);
			// Fields
			out.writeShort(clazz.getFields().size());
			for (Field field : clazz.getFields())
				writeField(field);
			// Methods
			out.writeShort(clazz.getMethods().size());
			for (Method method : clazz.getMethods())
				writeMethod(method);
			// Attributes
			out.writeShort(clazz.getAttributes().size());
			for (Attribute attribute : clazz.getAttributes())
				writeAttribute(attribute);
			return baos.toByteArray();
		} catch (IOException ex) {
			throw new InvalidClassException(ex);
		}
	}

	/**
	 * @param entry
	 * 		Constant pool entry to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 * @throws InvalidClassException
	 * 		When the class has unexpected data.
	 */
	private void writeCpEntry(ConstPoolEntry entry) throws IOException, InvalidClassException {
		int tag = entry.getTag();
		out.writeByte(tag);
		switch (tag) {
			case UTF8:
				out.writeUTF(((CpUtf8) entry).getText());
				break;
			case INTEGER:
				out.writeInt(((CpInt) entry).getValue());
				break;
			case FLOAT:
				out.writeFloat(((CpFloat) entry).getValue());
				break;
			case LONG:
				out.writeLong(((CpLong) entry).getValue());
				break;
			case DOUBLE:
				out.writeDouble(((CpDouble) entry).getValue());
				break;
			case STRING:
				out.writeShort(((CpString) entry).getIndex());
				break;
			case CLASS:
				out.writeShort(((CpClass) entry).getIndex());
				break;
			case FIELD_REF:
			case METHOD_REF:
			case INTERFACE_METHOD_REF:
				out.writeShort(((ConstRef) entry).getClassIndex());
				out.writeShort(((ConstRef) entry).getNameTypeIndex());
				break;
			case NAME_TYPE:
				out.writeShort(((CpNameType) entry).getNameIndex());
				out.writeShort(((CpNameType) entry).getTypeIndex());
				break;
			case DYNAMIC:
				out.writeShort(((CpDynamic) entry).getBsmIndex());
				out.writeShort(((CpDynamic) entry).getNameTypeIndex());
				break;
			case METHOD_HANDLE:
				out.writeByte(((CpMethodHandle) entry).getKind());
				out.writeShort(((CpMethodHandle) entry).getReferenceIndex());
				break;
			case METHOD_TYPE:
				out.writeShort(((CpMethodType) entry).getIndex());
				break;
			case INVOKE_DYNAMIC:
				out.writeShort(((CpInvokeDynamic) entry).getBsmIndex());
				out.writeShort(((CpInvokeDynamic) entry).getNameTypeIndex());
				break;
			case MODULE:
				out.writeShort(((CpModule) entry).getIndex());
				break;
			case PACKAGE:
				out.writeShort(((CpPackage) entry).getIndex());
				break;
			default:
				throw new InvalidClassException("Unknown constant-pool tag: " + tag);
		}
	}

	/**
	 * @param attribute
	 * 		Attribute to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	private void writeAttribute(Attribute attribute) throws IOException {
		if (attribute instanceof DefaultAttribute) {
			DefaultAttribute dflt = (DefaultAttribute) attribute;
			out.writeShort(dflt.getNameIndex());
			out.writeInt(dflt.getData().length);
			out.write(dflt.getData());
		} else if (attribute instanceof CodeAttribute) {
			CodeAttribute code = (CodeAttribute) attribute;
			out.writeShort(code.getNameIndex());
			out.writeInt(code.computeInternalLength() - 6);
			out.writeShort(code.getMaxStack());
			out.writeShort(code.getMaxLocals());
			out.writeInt(code.getCode().length);
			out.write(code.getCode());
			out.writeShort(code.getExceptionTable().size());
			for (CodeAttribute.ExceptionTableEntry tableEntry : code.getExceptionTable()) {
				out.writeShort(tableEntry.getStartPc());
				out.writeShort(tableEntry.getEndPc());
				out.writeShort(tableEntry.getHandlerPc());
				out.writeShort(tableEntry.getCatchTypeIndex());
			}
			out.writeShort(code.getAttributes().size());
			for (Attribute subAttribute : code.getAttributes())
				writeAttribute(subAttribute);
		} else if (attribute instanceof SyntheticAttribute || attribute instanceof DeprecatedAttribute) {
			out.writeShort(attribute.getNameIndex());
			out.writeInt(0);
		} else if (attribute instanceof DebugExtensionAttribute) {
			DebugExtensionAttribute debugExtension = (DebugExtensionAttribute) attribute;
			out.writeShort(debugExtension.getNameIndex());
			out.writeInt(debugExtension.getDebugExtension().length);
			out.write(debugExtension.getDebugExtension());
		} else {
			throw new UnsupportedOperationException("Attr type serialization not supported: " +
					attribute.getClass().getName());
		}
	}

	/**
	 * @param field
	 * 		Field to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	private void writeField(Field field) throws IOException {
		out.writeShort(field.getAccess());
		out.writeShort(field.getNameIndex());
		out.writeShort(field.getTypeIndex());
		out.writeShort(field.getAttributes().size());
		for (Attribute attribute : field.getAttributes())
			writeAttribute(attribute);
	}

	/**
	 * @param method
	 * 		Method to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	private void writeMethod(Method method) throws IOException {
		out.writeShort(method.getAccess());
		out.writeShort(method.getNameIndex());
		out.writeShort(method.getTypeIndex());
		out.writeShort(method.getAttributes().size());
		for (Attribute attribute : method.getAttributes())
			writeAttribute(attribute);
	}
}
