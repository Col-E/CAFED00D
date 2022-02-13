package me.coley.cafedude.classfile.attribute;

/**
 * An attribute implementation that is used as a default for any unhandled attribute type.
 *
 * @author Matt Coley
 */
public class DefaultAttribute extends Attribute {
	private byte[] data;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param data
	 * 		Literal data stored in attribute.
	 */
	public DefaultAttribute(int nameIndex, byte[] data) {
		super(nameIndex);
		this.data = data;
	}

	/**
	 * @return Literal data stored in attribute.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data
	 * 		New literal data stored in attribute.
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public int computeInternalLength() {
		return data.length;
	}
}
