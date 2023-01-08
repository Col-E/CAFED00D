package me.coley.cafedude.classfile.constant;

/**
 * Float pool entry.
 *
 * @author Matt Coley
 */
public class CpFloat extends ConstPoolEntry {
	private float value;

	/**
	 * @param value
	 * 		Constant value.
	 */
	public CpFloat(float value) {
		super(FLOAT);
		this.value = value;
	}

	/**
	 * @return Constant value.
	 */
	public float getValue() {
		return value;
	}

	/**
	 * @param value
	 * 		New constant value.
	 */
	public void setValue(float value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpFloat cpFloat = (CpFloat) o;
		return Float.compare(cpFloat.value, value) == 0;
	}

	@Override
	public int hashCode() {
		return Float.hashCode(value);
	}
}
