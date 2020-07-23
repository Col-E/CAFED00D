package me.coley.cafedude.constant;

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
}
