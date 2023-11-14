package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;

/**
 * Deprecation marker attribute.
 *
 * @author Matt Coley
 */
public class DeprecatedAttribute extends Attribute {
	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 */
	public DeprecatedAttribute(@Nonnull CpUtf8 name) {
		super(name);
	}

	@Override
	public int computeInternalLength() {
		return 0;
	}
}
