package me.coley.cafedude.constant;

/**
 * Integer pool entry.
 *
 * @author Matt Coley
 */
public class CpInt extends ConstPoolEntry {
	private int value;

	/**
	 * @param value
	 * 		Constant value.
	 */
	public CpInt(int value) {
		super(INTEGER);
		this.value = value;
	}

	/**
	 * @return Constant value.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value
	 * 		New constant value.
	 */
	public void setValue(int value) {
		this.value = value;
	}
}
