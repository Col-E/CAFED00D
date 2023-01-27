package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpUtf8;

/**
 * Deprecation marker attribute.
 *
 * @author Matt Coley
 */
public class DeprecatedAttribute extends Attribute {
	/**
	 * @param name
	 * 		Name index in constant pool.
	 */
	public DeprecatedAttribute(CpUtf8 name) {
		super(name);
	}

	@Override
	public int computeInternalLength() {
		return 0;
	}
}
