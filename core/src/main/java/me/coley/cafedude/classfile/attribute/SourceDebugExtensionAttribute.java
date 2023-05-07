package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;

/**
 * Source debug extension attribute. The contained data has no internal value to the JVM.
 *
 * @author Matt Coley
 */
public class SourceDebugExtensionAttribute extends Attribute {
	private byte[] debugExtension;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param debugExtension
	 * 		Extension data stored in attribute.
	 */
	public SourceDebugExtensionAttribute(@Nonnull CpUtf8 name, @Nonnull byte[] debugExtension) {
		super(name);
		this.debugExtension = debugExtension;
	}

	/**
	 * @return Extension data stored in attribute.
	 */
	@Nonnull
	public byte[] getDebugExtension() {
		return debugExtension;
	}

	/**
	 * @param debugExtension
	 * 		New extension data stored in attribute.
	 */
	public void setDebugExtension(@Nonnull byte[] debugExtension) {
		this.debugExtension = debugExtension;
	}

	@Override
	public int computeInternalLength() {
		return debugExtension.length;
	}
}
