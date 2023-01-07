package me.coley.cafedude.tree.frame;

/**
 * Contains an integer which represents the type of the value. It can be one of
 * the following:
 * <ul>
 *     <li>{@link #TOP}</li>
 *     <li>{@link #INTEGER}</li>
 *     <li>{@link #FLOAT}</li>
 *     <li>{@link #DOUBLE}</li>
 *     <li>{@link #LONG}</li>
 *     <li>{@link #NULL}</li>
 *     <li>{@link #UNINITIALIZED_THIS}</li>
 * </ul>
 */
public class PrimitiveValue extends Value {

	private int value;

	/**
	 * @param value
	 * 		Type of the value.
	 */
	public PrimitiveValue(int value) {
		this.value = value;
	}

	/**
	 * @return Type of the value.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value
	 * 		Type of the value.
	 */
	public void setValue(int value) {
		this.value = value;
	}

}
