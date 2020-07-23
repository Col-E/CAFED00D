package me.coley.cafedude.attribute;

/**
 * Synthetic marker attribute.
 *
 * @author Matt Coley
 */
public class SyntheticAttribute extends Attribute {
	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 */
	public SyntheticAttribute(int nameIndex) {
		super(nameIndex);
	}

	@Override
	public int computeInternalLength() {
		return 0;
	}
}
