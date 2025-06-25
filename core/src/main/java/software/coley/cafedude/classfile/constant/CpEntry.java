package software.coley.cafedude.classfile.constant;

import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.ConstantPoolConstants;

/**
 * Base constant pool entry.
 *
 * @author Matt Coley
 */
public sealed abstract class CpEntry implements ConstantPoolConstants permits ConstDynamic, ConstRef, CpClass, CpDouble,
		CpFloat, CpInt, CpLong, CpMethodHandle, CpMethodType, CpModule, CpNameType, CpPackage, CpString, CpUtf8, CpInternal {
	/**
	 * Flag to validate {@link #getIndex()} when called.
	 */
	public static boolean checkIndices = true;
	/**
	 * CP tag.
	 */
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
		if (checkIndices && index <= 0)
			throw new IllegalStateException("Cannot get index of CpEntry{tag=" + getTag() + "} - not present in ConstPool");
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
	 * @return {@code true} when the index has been computed.
	 * This is only done after insertion into a {@link ConstPool}.
	 */
	public boolean isInPool() {
		return index > 0;
	}

	/**
	 * @return {@code true} when constant uses two pool entries.
	 */
	public boolean isWide() {
		return false;
	}
}
