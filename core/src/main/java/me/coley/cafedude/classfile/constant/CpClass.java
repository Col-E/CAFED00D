package me.coley.cafedude.classfile.constant;

/**
 * Class pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public class CpClass extends ConstPoolEntry {
	private int index;

	/**
	 * @param index
	 * 		Index of class name UTF in pool.
	 */
	public CpClass(int index) {
		super(CLASS);
		this.index = index;
	}

	/**
	 * @return Index of class name UTF in pool.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 * 		New index of class name UTF in pool.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpClass cpClass = (CpClass) o;
		return index == cpClass.index;
	}

	@Override
	public int hashCode() {
		return index;
	}
}
