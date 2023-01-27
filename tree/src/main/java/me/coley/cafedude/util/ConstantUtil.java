package me.coley.cafedude.util;

import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.annotation.ClassElementValue;
import me.coley.cafedude.classfile.annotation.ElementValue;
import me.coley.cafedude.classfile.annotation.PrimitiveElementValue;
import me.coley.cafedude.classfile.annotation.Utf8ElementValue;
import me.coley.cafedude.classfile.constant.*;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.Handle;

import static me.coley.cafedude.classfile.ConstantPoolConstants.*;

/**
 * Utility for converting constant pool entries to their tree representation.
 */
public class ConstantUtil {

	/**
	 * Convert a {@link CpEntry} to a {@link Constant}.
	 * @param entry
	 * 			Constant pool entry.
	 * @param pool
	 * 			Constant pool to use for resolving references.
	 * @return Constant or {@code null} if the entry is not convertible.
	 */
	public static Constant from(CpEntry entry) {
		switch (entry.getTag()) {
			case UTF8: return new Constant(Constant.Type.STRING, ((CpUtf8) entry).getText());
			case STRING: return from(((CpString) entry).getString());
			case INTEGER: return new Constant(Constant.Type.INT, ((CpInt) entry).getValue());
			case FLOAT: return new Constant(Constant.Type.FLOAT, ((CpFloat) entry).getValue());
			case LONG: return new Constant(Constant.Type.LONG, ((CpLong) entry).getValue());
			case DOUBLE: return new Constant(Constant.Type.DOUBLE, ((CpDouble) entry).getValue());
			case CLASS: {
				CpClass cpClass = (CpClass) entry;
				return new Constant(Constant.Type.DESCRIPTOR,
						Descriptor.from('L' + cpClass.getName().getText() + ';'));
			}
			case METHOD_TYPE: {
				CpMethodType cpMethodType = (CpMethodType) entry;
				return new Constant(Constant.Type.DESCRIPTOR,
						Descriptor.from(cpMethodType.getDescriptor().getText()));
			}
			case METHOD_HANDLE: {
				CpMethodHandle cpMethodHandle = (CpMethodHandle) entry;
				ConstRef ref = cpMethodHandle.getReference();
				CpNameType nt = ref.getNameType();
				String owner = ref.getClassRef().getName().getText();
				String name = nt.getName().getText();
				String desc = nt.getType().getText();
				return new Constant(Constant.Type.HANDLE,
						new Handle(Handle.Tag.fromKind(cpMethodHandle.getKind()), owner, name, Descriptor.from(desc)));
			}
			default: return null;
		}
	}

	/**
	 * Convert a {@link ElementValue} to a {@link Constant}.
	 *
	 * @param value
	 * 			Element value. {@link PrimitiveElementValue}, {@link Utf8ElementValue} or {@link ClassElementValue}.
	 * @param pool
	 * 			Constant pool to use for resolving references.
	 * @return Constant or {@code null} if the value is not convertible.
	 * @throws IllegalArgumentException If a invalid element value is encountered.
	 */
	public static Constant from(ElementValue value) {
		CpEntry cp;
		if(value instanceof PrimitiveElementValue) {
			PrimitiveElementValue primitive = (PrimitiveElementValue) value;
			cp = primitive.getValue();
		} else if(value instanceof Utf8ElementValue) {
			Utf8ElementValue utf8 = (Utf8ElementValue) value;
			cp = utf8.getValue();
		} else if(value instanceof ClassElementValue) {
			ClassElementValue clazz = (ClassElementValue) value;
			cp = clazz.getClassEntry();
		} else {
			throw new IllegalStateException("Unknown element value: " + value);
		}
		return ConstantUtil.from(cp);
	}

}
