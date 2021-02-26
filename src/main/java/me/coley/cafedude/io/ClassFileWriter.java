package me.coley.cafedude.io;

import me.coley.cafedude.*;
import me.coley.cafedude.attribute.*;
import me.coley.cafedude.constant.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
				writeField(field, clazz);
			// Methods
			out.writeShort(clazz.getMethods().size());
			for (Method method : clazz.getMethods())
				writeMethod(method, clazz);
			// Attributes
			out.writeShort(clazz.getAttributes().size());
			for (Attribute attribute : clazz.getAttributes())
				writeAttribute(attribute, clazz);
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
			case Constants.ConstantPool.UTF8:
				out.writeUTF(((CpUtf8) entry).getText());
				break;
			case Constants.ConstantPool.INTEGER:
				out.writeInt(((CpInt) entry).getValue());
				break;
			case Constants.ConstantPool.FLOAT:
				out.writeFloat(((CpFloat) entry).getValue());
				break;
			case Constants.ConstantPool.LONG:
				out.writeLong(((CpLong) entry).getValue());
				break;
			case Constants.ConstantPool.DOUBLE:
				out.writeDouble(((CpDouble) entry).getValue());
				break;
			case Constants.ConstantPool.STRING:
				out.writeShort(((CpString) entry).getIndex());
				break;
			case Constants.ConstantPool.CLASS:
				out.writeShort(((CpClass) entry).getIndex());
				break;
			case Constants.ConstantPool.FIELD_REF:
			case Constants.ConstantPool.METHOD_REF:
			case Constants.ConstantPool.INTERFACE_METHOD_REF:
				out.writeShort(((ConstRef) entry).getClassIndex());
				out.writeShort(((ConstRef) entry).getNameTypeIndex());
				break;
			case Constants.ConstantPool.NAME_TYPE:
				out.writeShort(((CpNameType) entry).getNameIndex());
				out.writeShort(((CpNameType) entry).getTypeIndex());
				break;
			case Constants.ConstantPool.DYNAMIC:
				out.writeShort(((CpDynamic) entry).getBsmIndex());
				out.writeShort(((CpDynamic) entry).getNameTypeIndex());
				break;
			case Constants.ConstantPool.METHOD_HANDLE:
				out.writeByte(((CpMethodHandle) entry).getKind());
				out.writeShort(((CpMethodHandle) entry).getReferenceIndex());
				break;
			case Constants.ConstantPool.METHOD_TYPE:
				out.writeShort(((CpMethodType) entry).getIndex());
				break;
			case Constants.ConstantPool.INVOKE_DYNAMIC:
				out.writeShort(((CpInvokeDynamic) entry).getBsmIndex());
				out.writeShort(((CpInvokeDynamic) entry).getNameTypeIndex());
				break;
			case Constants.ConstantPool.MODULE:
				out.writeShort(((CpModule) entry).getIndex());
				break;
			case Constants.ConstantPool.PACKAGE:
				out.writeShort(((CpPackage) entry).getIndex());
				break;
			default:
				throw new InvalidClassException("Unknown constant-pool tag: " + tag);
		}
	}

	/**
	 * @param attribute
	 * 		Attribute to write.
	 * @param clazz
	 * 		Class to pull constant pool data from.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 * @throws InvalidClassException
	 * 		When the attribute name points to a non-utf8 constant.
	 */
	private void writeAttribute(Attribute attribute, ClassFile clazz) throws IOException, InvalidClassException {
		if (attribute instanceof DefaultAttribute) {
			DefaultAttribute dflt = (DefaultAttribute) attribute;
			out.writeShort(dflt.getNameIndex());
			out.writeInt(dflt.getData().length);
			out.write(dflt.getData());
		} else {
			ConstPoolEntry cpName = clazz.getCp(attribute.getNameIndex());
			if (!(cpName instanceof CpUtf8))
				throw new InvalidClassException("Attribute name index does not point to CP_UTF8");
			String attrName = ((CpUtf8) cpName).getText();
			switch (attrName) {
				case Constants.Attributes.BOOTSTRAP_METHODS:
					break;
				case Constants.Attributes.CHARACTER_RANGE_TABLE:
					break;
				case Constants.Attributes.CODE:
					// TODO: Explain where the "- 6" comes from
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
						writeAttribute(subAttribute, clazz);
					break;
				case Constants.Attributes.CONSTANT_VALUE:
					break;
				case Constants.Attributes.COMPILATION_ID:
					break;
				case Constants.Attributes.DEPRECATED:
				case Constants.Attributes.SYNTHETIC:
					out.writeShort(attribute.getNameIndex());
					out.writeInt(0);
					break;
				case Constants.Attributes.ENCLOSING_METHOD:
					break;
				case Constants.Attributes.EXCEPTIONS:
					ExceptionsAttribute exceptionsAttibute = (ExceptionsAttribute) attribute;
					out.writeShort(exceptionsAttibute.getNameIndex());
					out.writeInt(exceptionsAttibute.computeInternalLength());
					out.writeShort(exceptionsAttibute.getExceptionIndexTable().length);
					for(int index : exceptionsAttibute.getExceptionIndexTable()) {
						out.writeShort(index);
					}
					break;
				case Constants.Attributes.INNER_CLASSES:
					break;
				case Constants.Attributes.LINE_NUMBER_TABLE:
					break;
				case Constants.Attributes.LOCAL_VARIABLE_TABLE:
					break;
				case Constants.Attributes.LOCAL_VARIABLE_TYPE_TABLE:
					break;
				case Constants.Attributes.METHOD_PARAMETERS:
					break;
				case Constants.Attributes.MODULE:
					break;
				case Constants.Attributes.MODULE_HASHES:
					break;
				case Constants.Attributes.MODULE_MAIN_CLASS:
					break;
				case Constants.Attributes.MODULE_PACKAGES:
					break;
				case Constants.Attributes.MODULE_RESOLUTION:
					break;
				case Constants.Attributes.MODULE_TARGET:
					break;
				case Constants.Attributes.NEST_HOST:
					break;
				case Constants.Attributes.NEST_MEMBERS:
					break;
				case Constants.Attributes.RECORD:
					break;
				case Constants.Attributes.RUNTIME_VISIBLE_ANNOTATIONS:
				case Constants.Attributes.RUNTIME_INVISIBLE_ANNOTATIONS:
					new AnnotationWriter(out)
							.writeAnnotations((AnnotationsAttribute) attribute);
					break;
				case Constants.Attributes.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				case Constants.Attributes.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
					new AnnotationWriter(out)
							.writeParameterAnnotations((ParameterAnnotationsAttribute) attribute);
					break;
				case Constants.Attributes.RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				case Constants.Attributes.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
					new AnnotationWriter(out)
							.writeTypeAnnotations((AnnotationsAttribute) attribute);
					break;
				case Constants.Attributes.ANNOTATION_DEFAULT:
					new AnnotationWriter(out)
							.writeAnnotationDefault((AnnotationDefault) attribute);
					break;
				case Constants.Attributes.PERMITTED_SUBCLASSES:
					break;
				case Constants.Attributes.SIGNATURE:
					break;
				case Constants.Attributes.SOURCE_DEBUG_EXTENSION:
					DebugExtensionAttribute debugExtension = (DebugExtensionAttribute) attribute;
					out.writeShort(debugExtension.getNameIndex());
					out.writeInt(debugExtension.getDebugExtension().length);
					out.write(debugExtension.getDebugExtension());
					break;
				case Constants.Attributes.SOURCE_FILE:
					break;
				case Constants.Attributes.SOURCE_ID:
					break;
				case Constants.Attributes.STACK_MAP:
					break;
				case Constants.Attributes.STACK_MAP_TABLE:
					break;
				default:
					break;
			}
		}
	}

	/**
	 * @param field
	 * 		Field to write.
	 * @param clazz
	 * 		Declaring class.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 * @throws InvalidClassException
	 * 		When an attached attribute is invalid.
	 */
	private void writeField(Field field, ClassFile clazz) throws IOException, InvalidClassException {
		out.writeShort(field.getAccess());
		out.writeShort(field.getNameIndex());
		out.writeShort(field.getTypeIndex());
		out.writeShort(field.getAttributes().size());
		for (Attribute attribute : field.getAttributes())
			writeAttribute(attribute, clazz);
	}

	/**
	 * @param method
	 * 		Method to write.
	 * @param clazz
	 * 		Declaring class.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 * @throws InvalidClassException
	 * 		When an attached attribute is invalid.
	 */
	private void writeMethod(Method method, ClassFile clazz) throws IOException, InvalidClassException {
		out.writeShort(method.getAccess());
		out.writeShort(method.getNameIndex());
		out.writeShort(method.getTypeIndex());
		out.writeShort(method.getAttributes().size());
		for (Attribute attribute : method.getAttributes())
			writeAttribute(attribute, clazz);
	}
}
