package me.coley.cafedude.classfile.constant;

import javax.annotation.Nonnull;

/**
 * Interface method reference pool entry. Points to a reference's {@link CpClass defining class} in pool
 * and the reference's {@link CpNameType name and descriptor} in pool.
 *
 * @author Matt Coley
 */
public class CpInterfaceMethodRef extends ConstRef {
	/**
	 * @param classRef
	 * 		Constant pool entry holding the method's {@link CpClass defining class}.
	 * @param nameType
	 * 		Constant pool entry holding the method's {@link CpNameType name and descriptor}.
	 */
	public CpInterfaceMethodRef(@Nonnull CpClass classRef, @Nonnull CpNameType nameType) {
		super(INTERFACE_METHOD_REF, classRef, nameType);
	}
}
