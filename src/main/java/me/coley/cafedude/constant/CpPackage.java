package me.coley.cafedude.constant;

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
}
