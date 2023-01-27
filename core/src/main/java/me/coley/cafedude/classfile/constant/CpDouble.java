package me.coley.cafedude.classfile.constant;

/**
 * Double pool entry.
 *
 * @author Matt Coley
 */
public class CpDouble extends CpEntry {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpDouble cpDouble = (CpDouble) o;
		return Double.compare(cpDouble.value, value) == 0;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(value);
	}
}
