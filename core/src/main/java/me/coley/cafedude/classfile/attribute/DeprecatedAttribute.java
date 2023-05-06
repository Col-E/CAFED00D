package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;

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
	public DeprecatedAttribute(@Nonnull CpUtf8 name) {
		super(name);
	}

	@Override
	public int computeInternalLength() {
		return 0;
	}
}
