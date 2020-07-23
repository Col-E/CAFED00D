package me.coley.cafedude.constant;

/**
 * Base constant pool entry.
 *
 * @author Matt Coley
 */
public abstract class ConstPoolEntry {
	// Constants
	public static final int UTF8 = 1;
	public static final int INTEGER = 3;
	public static final int FLOAT = 4;
	public static final int LONG = 5;
	public static final int DOUBLE = 6;
	public static final int CLASS = 7;
	public static final int STRING = 8;
	public static final int FIELD_REF = 9;
	public static final int METHOD_REF = 10;
	public static final int INTERFACE_METHOD_REF = 11;
	public static final int NAME_TYPE = 12;
	public static final int METHOD_HANDLE = 15;
	public static final int METHOD_TYPE = 16;
	public static final int DYNAMIC = 17;
	public static final int INVOKE_DYNAMIC = 18;
	public static final int MODULE = 19;
	public static final int PACKAGE = 20;
	// Instance fields
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
