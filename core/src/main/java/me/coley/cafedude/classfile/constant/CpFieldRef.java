package me.coley.cafedude.classfile.constant;

import javax.annotation.Nonnull;

/**
 * Field reference pool entry. Points to a reference's {@link CpClass defining class} in pool
 * and the reference's {@link CpNameType name and descriptor} in pool.
 *
 * @author Matt Coley
 */
public class CpFieldRef extends ConstRef {
	/**
	 * @param classRef
	 * 		Constant pool entry holding the field's {@link CpClass defining class}.
	 * @param nameType
	 * 		Constant pool entry holding the field's {@link CpNameType name and descriptor}.
	 */
	public CpFieldRef(@Nonnull CpClass classRef, @Nonnull CpNameType nameType) {
		super(FIELD_REF, classRef, nameType);
	}
}
