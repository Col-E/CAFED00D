package me.coley.cafedude.classfile.constant;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpLong cpLong = (CpLong) o;
		return value == cpLong.value;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(value);
	}
}
