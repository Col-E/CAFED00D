package me.coley.cafedude.annotation;

/**
 * Class element value.
 *
 * @author Matt Coley
 */
public class ClassElementValue extends ElementValue {
	private int classIndex;

	/**
	 * @param tag
	 * 		ASCII tag representation, must be {@code c}.
	 * @param classIndex
	 * 		Index of class constant.
	 */
	public ClassElementValue(char tag, int classIndex) {
		super(tag);
		if (tag != 'c')
			throw new IllegalArgumentException("Class element value must have 'c' tag");
		this.classIndex = classIndex;
	}

	/**
	 * @return Index of class constant.
	 */
	public int getClassIndex() {
		return classIndex;
	}

	/**
	 * @param classIndex
	 * 		Index of class constant.
	 */
	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}

	/**
	 * @return ASCII tag representation of a class, {@code c}.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

	@Override
	public int computeLength() {
		// u1: tag
		// u2: class_index
		return 3;
	}
}
