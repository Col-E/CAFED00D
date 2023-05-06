package me.coley.cafedude.classfile.constant;

import me.coley.cafedude.classfile.ConstantPoolConstants;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Base reference pool entry. Points to a reference's {@link CpClass defining class} in pool
 * and the reference's {@link CpNameType name and descriptor} in pool.
 *
 * @author Matt Coley
 */
public abstract class ConstRef extends CpEntry {
	private CpClass classRef;
	private CpNameType nameType;

	/**
	 * @param type
	 * 		Reference type.
	 * 		Must be {@link ConstantPoolConstants#FIELD_REF}, {@link ConstantPoolConstants#METHOD_REF},
	 * 		or {@link ConstantPoolConstants#INTERFACE_METHOD_REF}.
	 * @param classRef
	 * 		Index of reference {@link CpClass defining class} in pool.
	 * @param nameType
	 * 		Index of field/method {@link CpNameType name and descriptor} in pool.
	 */
	public ConstRef(int type, @Nonnull CpClass classRef, @Nonnull CpNameType nameType) {
		super(type);
		this.classRef = classRef;
		this.nameType = nameType;
	}

	/**
	 * @return Index of reference {@link CpClass defining class} in pool.
	 */
	@Nonnull
	public CpClass getClassRef() {
		return classRef;
	}

	/**
	 * @param classRef
	 * 		New index of reference {@link CpClass defining class} in pool.
	 */
	public void setClassRef(@Nonnull CpClass classRef) {
		this.classRef = classRef;
	}

	/**
	 * @return Index of field/method {@link CpNameType name and descriptor} in pool.
	 */
	@Nonnull
	public CpNameType getNameType() {
		return nameType;
	}

	/**
	 * @param nameType
	 * 		New index of field/method {@link CpNameType name and descriptor} in pool.
	 */
	public void setNameType(@Nonnull CpNameType nameType) {
		this.nameType = nameType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConstRef constRef = (ConstRef) o;
		return classRef == constRef.classRef &&
				nameType == constRef.nameType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(classRef, nameType);
	}
}
