package me.coley.cafedude.classfile.constant;

import me.coley.cafedude.classfile.ConstantPoolConstants;

import java.util.Objects;

/**
 * Base reference pool entry. Points to a reference's {@link CpClass defining class} in pool
 * and the reference's {@link CpNameType name and descriptor} in pool.
 *
 * @author Matt Coley
 */
public abstract class ConstRef extends ConstPoolEntry {
	private int classIndex;
	private int nameTypeIndex;

	/**
	 * @param type
	 * 		Reference type.
	 * 		Must be {@link ConstantPoolConstants#FIELD_REF}, {@link ConstantPoolConstants#METHOD_REF},
	 * 		or {@link ConstantPoolConstants#INTERFACE_METHOD_REF}.
	 * @param classIndex
	 * 		Index of reference {@link CpClass defining class} in pool.
	 * @param nameTypeIndex
	 * 		Index of field/method {@link CpNameType name and descriptor} in pool.
	 */
	public ConstRef(int type, int classIndex, int nameTypeIndex) {
		super(type);
		this.classIndex = classIndex;
		this.nameTypeIndex = nameTypeIndex;
	}

	/**
	 * @return Index of reference {@link CpClass defining class} in pool.
	 */
	public int getClassIndex() {
		return classIndex;
	}

	/**
	 * @param classIndex
	 * 		New index of reference {@link CpClass defining class} in pool.
	 */
	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}

	/**
	 * @return Index of field/method {@link CpNameType name and descriptor} in pool.
	 */
	public int getNameTypeIndex() {
		return nameTypeIndex;
	}

	/**
	 * @param nameTypeIndex
	 * 		New index of field/method {@link CpNameType name and descriptor} in pool.
	 */
	public void setNameTypeIndex(int nameTypeIndex) {
		this.nameTypeIndex = nameTypeIndex;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConstRef constRef = (ConstRef) o;
		return classIndex == constRef.classIndex &&
				nameTypeIndex == constRef.nameTypeIndex;
	}

	@Override
	public int hashCode() {
		return Objects.hash(classIndex, nameTypeIndex);
	}
}
