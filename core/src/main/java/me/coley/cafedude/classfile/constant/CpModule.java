package me.coley.cafedude.classfile.constant;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpModule that = (CpModule) o;
		return index == that.index;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(index);
	}
}
