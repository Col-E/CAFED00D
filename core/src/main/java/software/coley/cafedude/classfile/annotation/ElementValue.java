package software.coley.cafedude.classfile.annotation;

import software.coley.cafedude.classfile.behavior.CpAccessor;

/**
 * Base attribute element value.
 *
 * @author Matt Coley
 */
public sealed abstract class ElementValue implements CpAccessor permits AnnotationElementValue, ArrayElementValue,
		ClassElementValue, EnumElementValue, PrimitiveElementValue, Utf8ElementValue {
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
	 *
	 * @see ElementValueConstants Possible 'tag' values
	 */
	public char getTag() {
		return tag;
	}

	/**
	 * @return Computed size for the element value.
	 */
	public abstract int computeLength();
}
