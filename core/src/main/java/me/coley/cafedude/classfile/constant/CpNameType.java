package me.coley.cafedude.classfile.constant;

/**
 * NameType pool entry. Points to two UTF constants.
 *
 * @author Matt Coley
 */
public class CpNameType extends ConstPoolEntry {
	private int nameIndex;
	private int typeIndex;

	/**
	 * @param nameIndex
	 * 		Index of name UTF string in pool.
	 * @param typeIndex
	 * 		Index of descriptor UTF string in pool.
	 */
	public CpNameType(int nameIndex, int typeIndex) {
		super(NAME_TYPE);
		this.nameIndex = nameIndex;
		this.typeIndex = typeIndex;
	}

	/**
	 * @return Index of name UTF string in pool.
	 */
	public int getNameIndex() {
		return nameIndex;
	}

	/**
	 * @param nameIndex
	 * 		New index of name UTF string in pool.
	 */
	public void setNameIndex(int nameIndex) {
		this.nameIndex = nameIndex;
	}

	/**
	 * @return Index of descriptor UTF string in pool.
	 */
	public int getTypeIndex() {
		return typeIndex;
	}

	/**
	 * @param typeIndex
	 * 		New index of descriptor UTF string in pool.
	 */
	public void setTypeIndex(int typeIndex) {
		this.typeIndex = typeIndex;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpNameType that = (CpNameType) o;
		return nameIndex == that.nameIndex && typeIndex == that.typeIndex;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(nameIndex) + Integer.hashCode(typeIndex);
	}
}
