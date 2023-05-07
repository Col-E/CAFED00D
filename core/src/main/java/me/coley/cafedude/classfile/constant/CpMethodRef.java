package me.coley.cafedude.classfile.constant;

import javax.annotation.Nonnull;

/**
 * Method reference pool entry. Points to a reference's {@link CpClass defining class}
 * and the reference's {@link CpNameType name and descriptor}.
 *
 * @author Matt Coley
 */
public class CpMethodRef extends ConstRef {
	/**
	 * @param classRef
	 * 		Constant pool entry holding the method's {@link CpClass defining class}.
	 * @param nameType
	 * 		Constant pool entry holding the method's {@link CpNameType name and descriptor}.
	 */
	public CpMethodRef(@Nonnull CpClass classRef, @Nonnull CpNameType nameType) {
		super(METHOD_REF, classRef, nameType);
	}
}
