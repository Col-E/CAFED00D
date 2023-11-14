package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;

/**
 * An attribute implementation that is used as a default for any unhandled attribute type.
 *
 * @author Matt Coley
 */
public class DefaultAttribute extends Attribute {
	private byte[] data;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param data
	 * 		Literal data stored in attribute.
	 */
	public DefaultAttribute(@Nonnull CpUtf8 name, @Nonnull byte[] data) {
		super(name);
		this.data = data;
	}

	/**
	 * @return Literal data stored in attribute.
	 */
	@Nonnull
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data
	 * 		New literal data stored in attribute.
	 */
	public void setData(@Nonnull byte[] data) {
		this.data = data;
	}

	@Override
	public int computeInternalLength() {
		return data.length;
	}
}
