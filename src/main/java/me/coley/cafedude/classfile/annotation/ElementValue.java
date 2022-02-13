package me.coley.cafedude.classfile.annotation;

/**
 * Base attribute element value.
 *
 * @author Matt Coley
 */
public abstract class ElementValue {
	private final char tag;

	/**
	 * @param tag
	 * 		ASCII tag representation, indicating the type of element value.
	 */
	public ElementValue(char tag) {
		this.tag = tag;
	}

	/**
	 * @return ASCII tag representation, indicating the type of element value.
	 */
	public char getTag() {
		return tag;
	}

	/**
	 * @return Computed size for the element value.
	 */
	public abstract int computeLength();
}
