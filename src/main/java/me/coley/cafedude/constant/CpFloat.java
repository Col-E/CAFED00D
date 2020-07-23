package me.coley.cafedude.constant;

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
}
