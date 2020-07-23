package me.coley.cafedude.constant;

/**
 * Long pool entry.
 *
 * @author Matt Coley
 */
public class CpLong extends ConstPoolEntry {
	private long value;

	/**
	 * @param value
	 * 		Constant value.
	 */
	public CpLong(long value) {
		super(LONG);
		this.value = value;
	}

	/**
	 * @return Constant value.
	 */
	public long getValue() {
		return value;
	}

	/**
	 * @param value
	 * 		New constant value.
	 */
	public void setValue(long value) {
		this.value = value;
	}

	@Override
	public boolean isWide() {
		return true;
	}
}
