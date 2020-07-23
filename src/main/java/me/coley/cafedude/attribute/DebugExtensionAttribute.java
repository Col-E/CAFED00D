package me.coley.cafedude.attribute;

/**
 * Source debug extension attribute. The contained data has no internal value to the JVM.
 *
 * @author Matt Coley
 */
public class DebugExtensionAttribute extends Attribute {
	private byte[] debugExtension;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param debugExtension
	 * 		Extension data stored in attribute.
	 */
	public DebugExtensionAttribute(int nameIndex, byte[] debugExtension) {
		super(nameIndex);
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
