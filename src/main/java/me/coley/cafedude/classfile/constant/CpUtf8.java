package me.coley.cafedude.classfile.constant;

/**
 * UTF8 pool entry.
 *
 * @author Matt Coley
 */
public class CpUtf8 extends ConstPoolEntry {
	private String text;

	/**
	 * Create UTF8 attribute.
	 *
	 * @param text
	 * 		Constant text.
	 */
	public CpUtf8(String text) {
		super(UTF8);
		this.text = text;
	}

	/**
	 * @return Constant text.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 * 		New constant text.
	 */
	public void setText(String text) {
		this.text = text;
	}
}
