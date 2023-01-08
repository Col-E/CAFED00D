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
	 * Convert a {@link ConstPoolEntry} to a {@link Constant}.
	 * @param entry
	 * 			Constant pool entry.
	 * @param pool
	 * 			Constant pool to use for resolving references.
	 * @return Constant or {@code null} if the entry is not convertible.
	 */
	public static Constant from(ConstPoolEntry entry, ConstPool pool) {
		switch (entry.getTag()) {
			case UTF8: return new Constant(Constant.Type.STRING, ((CpUtf8) entry).getText());
			case STRING: return from(pool.get(((CpString) entry).getIndex()), pool);
			case INTEGER: return new Constant(Constant.Type.INT, ((CpInt) entry).getValue());
			case FLOAT: return new Constant(Constant.Type.FLOAT, ((CpFloat) entry).getValue());
			case LONG: return new Constant(Constant.Type.LONG, ((CpLong) entry).getValue());
			case DOUBLE: return new Constant(Constant.Type.DOUBLE, ((CpDouble) entry).getValue());
			case CLASS: {
				CpClass cpClass = (CpClass) entry;
				String name = pool.getUtf(cpClass.getIndex());
				return new Constant(Constant.Type.DESCRIPTOR, Descriptor.from(name));
			}
			case METHOD_TYPE: {
				CpMethodType cpMethodType = (CpMethodType) entry;
				String desc = pool.getUtf(cpMethodType.getIndex());
				return new Constant(Constant.Type.DESCRIPTOR, Descriptor.from(desc));
			}
			case METHOD_HANDLE: {
				CpMethodHandle cpMethodHandle = (CpMethodHandle) entry;
				int refIndex = cpMethodHandle.getReferenceIndex();
				ConstRef ref = (ConstRef) pool.get(refIndex);
				CpClass cpClass = (CpClass) pool.get(ref.getClassIndex());
				CpNameType cpNameType = (CpNameType) pool.get(ref.getNameTypeIndex());
				String owner = pool.getUtf(cpClass.getIndex());
				String name = pool.getUtf(cpNameType.getNameIndex());
				String desc = pool.getUtf(cpNameType.getTypeIndex());
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
	public static Constant from(ElementValue value, ConstPool pool) {
		ConstPoolEntry cp;
		if(value instanceof PrimitiveElementValue) {
			PrimitiveElementValue primitive = (PrimitiveElementValue) value;
			cp = pool.get(primitive.getValueIndex());
		} else if(value instanceof Utf8ElementValue) {
			Utf8ElementValue utf8 = (Utf8ElementValue) value;
			cp = pool.get(utf8.getUtfIndex());
		} else if(value instanceof ClassElementValue) {
			ClassElementValue clazz = (ClassElementValue) value;
			cp = pool.get(clazz.getClassIndex());
		} else {
			throw new IllegalStateException("Unknown element value: " + value);
		}
		return ConstantUtil.from(cp, pool);
	}

	/**
	 * Get the UTF8 string of a {@link CpClass} index
	 *
	 * @param classIndex
	 * 			Index of the class constant.
	 * @param pool
	 * 			Constant pool to use for resolving references.
	 * @return UTF8 string.
	 */
	public static String getClassName(int classIndex, ConstPool pool) {
		ConstPoolEntry entry = pool.get(classIndex);
		if(entry.getTag() == CLASS) {
			return pool.get(((CpClass) entry).getIndex()).toString();
		}
		return null;
	}

}
