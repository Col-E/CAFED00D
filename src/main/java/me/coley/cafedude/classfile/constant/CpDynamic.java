package me.coley.cafedude.classfile.constant;

/**
 * Dynamic value pool entry. Points to a {@link CpNameType NameType} constant
 * and a bootstrap method index in the class's bootstrap-methods attribute.
 *
 * @author Matt Coley
 */
public class CpDynamic extends ConstPoolEntry {
	private int bsmIndex;
	private int nameTypeIndex;

	/**
	 * @param bsmIndex
	 * 		Index in the class's bootstrap method attribute-table.
	 * @param nameTypeIndex
	 * 		Index of {@link CpNameType} in pool.
	 */
	public CpDynamic(int bsmIndex, int nameTypeIndex) {
		super(DYNAMIC);
		this.bsmIndex = bsmIndex;
		this.nameTypeIndex = nameTypeIndex;
	}

	/**
	 * @return Index in the class's bootstrap method attribute-table.
	 */
	public int getBsmIndex() {
		return bsmIndex;
	}

	/**
	 * @param bsmIndex
	 * 		New index in the class's bootstrap method attribute-table.
	 */
	public void setBsmIndex(int bsmIndex) {
		this.bsmIndex = bsmIndex;
	}

	/**
	 * @return Index of {@link CpNameType} in pool.
	 */
	public int getNameTypeIndex() {
		return nameTypeIndex;
	}

	/**
	 * @param nameTypeIndex
	 * 		New index of {@link CpNameType} in pool.
	 */
	public void setNameTypeIndex(int nameTypeIndex) {
		this.nameTypeIndex = nameTypeIndex;
	}
}
