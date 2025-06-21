package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;

/**
 * Synthetic marker attribute.
 *
 * @author Matt Coley
 */
public class SyntheticAttribute extends Attribute {
	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 */
	public SyntheticAttribute(@Nonnull CpUtf8 name) {
		super(name);
	}

	@Override
	public int computeInternalLength() {
		return 0;
	}
}
