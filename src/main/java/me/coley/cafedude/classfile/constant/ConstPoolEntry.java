package me.coley.cafedude.classfile.constant;

import me.coley.cafedude.Constants;

/**
 * Base constant pool entry.
 *
 * @author Matt Coley
 */
public abstract class ConstPoolEntry implements Constants.ConstantPool {
	private final int tag;

	/**
	 * Create base attribute.
	 *
	 * @param tag
	 * 		Constant's tag.
	 */
	public ConstPoolEntry(int tag) {
		this.tag = tag;
	}

	/**
	 * @return Constant's tag.
	 */
	public int getTag() {
		return tag;
	}

	/**
	 * @return {@code true} if constant uses two pool entries.
	 */
	public boolean isWide() {
		return false;
	}
}
