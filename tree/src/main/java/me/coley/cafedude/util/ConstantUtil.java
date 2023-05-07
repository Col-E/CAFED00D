package me.coley.cafedude.util;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.annotation.ClassElementValue;
import me.coley.cafedude.classfile.annotation.ElementValue;
import me.coley.cafedude.classfile.annotation.PrimitiveElementValue;
import me.coley.cafedude.classfile.annotation.Utf8ElementValue;
import me.coley.cafedude.classfile.constant.*;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.Handle;

import javax.annotation.Nonnull;

import static me.coley.cafedude.classfile.ConstantPoolConstants.*;

/**
 * Utility for converting constant pool entries to their tree representation.
 *
 * @author Justus Garbe
 */
public class ConstantUtil {

	/**
	 * Convert a {@link CpEntry} to a {@link Constant}.
	 *
	 * @param entry
	 * 		Constant pool entry.
	 *
	 * @return Constant or {@code null} if the entry is not convertible.
	 *
	 * @throws IllegalArgumentException
	 * 		When the constant type is not convertible.
	 */
	@Nonnull
	public static Constant from(@Nonnull CpEntry entry) {
		switch (entry.getTag()) {
			case UTF8:
				return Constant.of(((CpUtf8) entry).getText());
			case STRING:
				return from(((CpString) entry).getString());
			case INTEGER:
				return Constant.of(((CpInt) entry).getValue());
			case FLOAT:
				return Constant.of(((CpFloat) entry).getValue());
			case LONG:
				return Constant.of(((CpLong) entry).getValue());
			case DOUBLE:
				return Constant.of(((CpDouble) entry).getValue());
			case CLASS: {
				CpClass cpClass = (CpClass) entry;
				return Constant.of(Descriptor.from('L' + cpClass.getName().getText() + ';'));
			}
			case METHOD_TYPE: {
				CpMethodType cpMethodType = (CpMethodType) entry;
				return Constant.of(Descriptor.from(cpMethodType.getDescriptor().getText()));
			}
			case METHOD_HANDLE: {
				CpMethodHandle cpMethodHandle = (CpMethodHandle) entry;
				ConstRef ref = cpMethodHandle.getReference();
				CpNameType nt = ref.getNameType();
				String owner = ref.getClassRef().getName().getText();
				String name = nt.getName().getText();
				String desc = nt.getType().getText();
				return Constant.of(
						new Handle(Handle.Tag.fromKind(cpMethodHandle.getKind()), owner, name, Descriptor.from(desc)));
			}
			default:
				throw new IllegalArgumentException("Non convertible constant type: " + entry.getTag());
		}
	}

	/**
	 * Convert a {@link ElementValue} to a {@link Constant}.
	 *
	 * @param value
	 * 		Element value. {@link PrimitiveElementValue}, {@link Utf8ElementValue} or {@link ClassElementValue}.
	 *
	 * @return Constant or {@code null} if the value is not convertible.
	 *
	 * @throws IllegalArgumentException
	 * 		If a invalid element value is encountered.
	 */
	@Nonnull
	public static Constant from(@Nonnull ElementValue value) {
		CpEntry cp;
		if (value instanceof PrimitiveElementValue) {
			PrimitiveElementValue primitive = (PrimitiveElementValue) value;
			cp = primitive.getValue();
		} else if (value instanceof Utf8ElementValue) {
			Utf8ElementValue utf8 = (Utf8ElementValue) value;
			cp = utf8.getValue();
		} else if (value instanceof ClassElementValue) {
			ClassElementValue clazz = (ClassElementValue) value;
			cp = clazz.getClassEntry();
		} else {
			throw new IllegalStateException("Unknown element value: " + value);
		}
		return ConstantUtil.from(cp);
	}

}
