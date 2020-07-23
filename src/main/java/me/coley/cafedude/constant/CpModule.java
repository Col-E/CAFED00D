package me.coley.cafedude.constant;

/**
 * Module pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public class CpModule extends ConstPoolEntry {
	private int index;

	/**
	 * @param index
	 * 		Index of module name UTF in pool.
	 */
	public CpModule(int index) {
		super(MODULE);
		this.index = index;
	}

	/**
	 * @return Index of module name UTF in pool.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 * 		New index of module name UTF in pool.
	 */
	public void setIndex(int index) {
		this.index = index;
	}
}
