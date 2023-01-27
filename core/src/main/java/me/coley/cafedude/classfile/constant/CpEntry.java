package me.coley.cafedude.classfile.constant;

import me.coley.cafedude.classfile.ConstantPoolConstants;

/**
 * Base constant pool entry.
 *
 * @author Matt Coley
 */
public abstract class CpEntry implements ConstantPoolConstants {
	private final int tag;
	/**
	 * Index is the index this entry has in a constant pool, < 1 if not in a pool.
	 */
	private int index;

	/**
	 * Create base attribute.
	 *
	 * @param tag
	 * 		Constant's tag.
	 */
	public CpEntry(int tag) {
		this.tag = tag;
	}

	/**
	 * @return Constant's tag.
	 */
	public int getTag() {
		return tag;
	}

	/**
	 * @return Index of the constant in the constant pool.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 * 		New index of the constant in the constant pool.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return if in a constant pool.
	 */
	public boolean isInPool() {
		return index > 0;
	}

	/**
	 * @return {@code true} if constant uses two pool entries.
	 */
	public boolean isWide() {
		return false;
	}
}
