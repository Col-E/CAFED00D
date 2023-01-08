package me.coley.cafedude.classfile.constant;

/**
 * Package pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public class CpPackage extends ConstPoolEntry {
	private int index;

	/**
	 * @param index
	 * 		Index of package name UTF in pool.
	 */
	public CpPackage(int index) {
		super(PACKAGE);
		this.index = index;
	}

	/**
	 * @return Index of package name UTF in pool.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 * 		New index of package name UTF in pool.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpPackage that = (CpPackage) o;
		return index == that.index;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(index);
	}
}
