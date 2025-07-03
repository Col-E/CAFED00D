package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;
import software.coley.cafedude.classfile.ConstantPoolConstants;

import java.util.Collection;
import java.util.List;

/**
 * Base reference pool entry. Points to a reference's {@link CpClass defining class}
 * and the reference's {@link CpNameType name and descriptor}.
 *
 * @author Matt Coley
 */
public abstract sealed class ConstRef extends CpEntry implements CrossCpReferencing permits ConstRefInternal, CpFieldRef, CpInterfaceMethodRef, CpMethodRef {
	private CpClass classRef;
	private CpNameType nameType;

	/**
	 * @param type
	 * 		Reference type.
	 * 		Must be {@link ConstantPoolConstants#FIELD_REF}, {@link ConstantPoolConstants#METHOD_REF},
	 * 		or {@link ConstantPoolConstants#INTERFACE_METHOD_REF}.
	 * @param classRef
	 * 		Constant pool entry holding the reference's {@link CpClass defining class}.
	 * @param nameType
	 * 		Constant pool entry holding the reference's {@link CpNameType name and descriptor}.
	 */
	public ConstRef(int type, @Nonnull CpClass classRef, @Nonnull CpNameType nameType) {
		super(type);
		this.classRef = classRef;
		this.nameType = nameType;
	}

	/**
	 * @return Constant pool entry holding the reference's {@link CpClass defining class}.
	 */
	@Nonnull
	public CpClass getClassRef() {
		return classRef;
	}

	/**
	 * @param classRef
	 * 		New constant pool entry holding the reference's {@link CpClass defining class}.
	 */
	public void setClassRef(@Nonnull CpClass classRef) {
		this.classRef = classRef;
	}

	/**
	 * @return Constant pool entry holding the reference's {@link CpNameType name and descriptor}.
	 */
	@Nonnull
	public CpNameType getNameType() {
		return nameType;
	}

	/**
	 * @param nameType
	 * 		New constant pool entry holding the reference's {@link CpNameType name and descriptor}.
	 */
	public void setNameType(@Nonnull CpNameType nameType) {
		this.nameType = nameType;
	}

	@Nonnull
	@Override
	public Collection<CpEntry> getReferences() {
		return List.of(classRef, nameType);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConstRef)) return false;

		ConstRef constRef = (ConstRef) o;

		if (!classRef.equals(constRef.classRef)) return false;
		return nameType.equals(constRef.nameType);
	}

	@Override
	public int hashCode() {
		int result = classRef.hashCode();
		result = 31 * result + nameType.hashCode();
		return result;
	}
}
