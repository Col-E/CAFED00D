package me.coley.cafedude.classfile.annotation;

/**
 * Primitive value element value.
 *
 * @author Matt Coley
 */
public class PrimitiveElementValue extends ElementValue {
	private int valueIndex;

	/**
	 * @param tag
	 * 		ASCII tag representation, indicating the type of primitive element value.
	 * @param valueIndex
	 * 		Index of primitive value constant.
	 */
	public PrimitiveElementValue(char tag, int valueIndex) {
		super(tag);
		this.valueIndex = valueIndex;
	}

	/**
	 * @return Index of primitive value constant.
	 */
	public int getValueIndex() {
		return valueIndex;
	}

	/**
	 * @param valueIndex
	 * 		Index of primitive value constant.
	 */
	public void setValueIndex(int valueIndex) {
		this.valueIndex = valueIndex;
	}

	/**
	 * @return ASCII tag representation, indicating the type of primitive element value.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

	@Override
	public int computeLength() {
		// u1: tag
		// u2: value_index
		return 3;
	}
}
