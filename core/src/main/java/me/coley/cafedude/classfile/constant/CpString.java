package me.coley.cafedude.classfile.constant;

/**
 * String pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public class CpString extends ConstPoolEntry {
	private int index;

	/**
	 * @param index
	 * 		Index of UTF string in pool.
	 */
	public CpString(int index) {
		super(STRING);
		this.index = index;
	}

	/**
	 * @return Index of UTF string in pool.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 * 		New index of UTF string in pool.
	 */
	public void setIndex(int index) {
		this.index = index;
	}
}
