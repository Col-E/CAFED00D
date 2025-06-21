package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;

/**
 * Interface method reference pool entry. Points to a reference's {@link CpClass defining class}
 * and the reference's {@link CpNameType name and descriptor}.
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
