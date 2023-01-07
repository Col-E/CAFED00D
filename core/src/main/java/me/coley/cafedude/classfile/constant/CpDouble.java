package me.coley.cafedude.classfile.constant;

/**
 * Double pool entry.
 *
 * @author Matt Coley
 */
public class CpDouble extends ConstPoolEntry {
	private double value;

	/**
	 * @param value
	 * 		Constant value.
	 */
	public CpDouble(double value) {
		super(DOUBLE);
		this.value = value;
	}

	/**
	 * @return Constant value.
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value
	 * 		New constant value.
	 */
	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public boolean isWide() {
		return true;
	}
}
