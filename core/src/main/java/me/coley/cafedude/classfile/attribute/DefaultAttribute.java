package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpUtf8;

/**
 * An attribute implementation that is used as a default for any unhandled attribute type.
 *
 * @author Matt Coley
 */
public class DefaultAttribute extends Attribute {
	private byte[] data;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param data
	 * 		Literal data stored in attribute.
	 */
	public DefaultAttribute(CpUtf8 name, byte[] data) {
		super(name);
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
