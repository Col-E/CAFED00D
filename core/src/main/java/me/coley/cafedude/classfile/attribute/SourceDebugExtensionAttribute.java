package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpUtf8;

/**
 * Source debug extension attribute. The contained data has no internal value to the JVM.
 *
 * @author Matt Coley
 */
public class SourceDebugExtensionAttribute extends Attribute {
	private byte[] debugExtension;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param debugExtension
	 * 		Extension data stored in attribute.
	 */
	public SourceDebugExtensionAttribute(CpUtf8 name, byte[] debugExtension) {
		super(name);
		this.debugExtension = debugExtension;
	}

	/**
	 * @return Extension data stored in attribute.
	 */
	public byte[] getDebugExtension() {
		return debugExtension;
	}

	/**
	 * @param debugExtension
	 * 		New extension data stored in attribute.
	 */
	public void setDebugExtension(byte[] debugExtension) {
		this.debugExtension = debugExtension;
	}

	@Override
	public int computeInternalLength() {
		return debugExtension.length;
	}
}
