package me.coley.cafedude.classfile.attribute;

/**
 * Deprecation marker attribute.
 *
 * @author Matt Coley
 */
public class DeprecatedAttribute extends Attribute {
	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 */
	public DeprecatedAttribute(int nameIndex) {
		super(nameIndex);
	}

	@Override
	public int computeInternalLength() {
		return 0;
	}
}
