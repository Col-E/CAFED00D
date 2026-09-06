package software.coley.cafedude.classfile.annotation;

import software.coley.cafedude.classfile.ConstantPoolConstants;
import software.coley.cafedude.classfile.constant.CpEntry;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * Primitive value element value.
 *
 * @author Matt Coley
 */
public non-sealed class PrimitiveElementValue extends ElementValue {
	private CpEntry value;

	/**
	 * @param tag
	 * 		ASCII tag representation, indicating the type of primitive element value.
	 * @param value
	 * 		Constant pool entry holding the primitive value constant.
	 */
	public PrimitiveElementValue(char tag, @Nonnull CpEntry value) {
		super(tag);
		if (requiredConstantTag(tag) != value.getTag())
			throw new IllegalArgumentException("Primitive tag '" + tag + "' has a mismatched constant kind: " + value.getTag());
		this.value = value;
	}

	/**
	 * @param tag
	 * 		ASCII tag representation of the primitive type.
	 *
	 * @return The constant-pool tag the value must use, or {@code -1} when the tag is not a
	 * primitive element-value tag (JVMS 4.7.16.1, Table 4.7.16.1-A).
	 */
	public static int requiredConstantTag(char tag) {
		return switch (tag) {
			case 'B', 'C', 'I', 'S', 'Z' -> ConstantPoolConstants.INTEGER;
			case 'F' -> ConstantPoolConstants.FLOAT;
			case 'J' -> ConstantPoolConstants.LONG;
			case 'D' -> ConstantPoolConstants.DOUBLE;
			default -> -1;
		};
	}

	/**
	 * @return Constant pool entry holding the primitive value constant.
	 */
	@Nonnull
	public CpEntry getValue() {
		return value;
	}

	/**
	 * @param value
	 * 		New constant pool entry holding the primitive value constant.
	 */
	public void setValue(@Nonnull CpEntry value) {
		this.value = value;
	}

	/**
	 * @return ASCII tag representation, indicating the type of primitive element value.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		return Collections.singleton(value);
	}

	@Override
	public int computeLength() {
		// u1: tag
		// u2: value_index
		return 3;
	}
}
