package software.coley.cafedude.classfile.constant;

import javax.annotation.Nonnull;

/**
 * UTF8 pool entry.
 *
 * @author Matt Coley
 */
public class CpUtf8 extends CpEntry {
	private String text;

	/**
	 * Create UTF8 attribute.
	 *
	 * @param text
	 * 		Constant text.
	 */
	public CpUtf8(@Nonnull String text) {
		super(UTF8);
		this.text = text;
	}

	/**
	 * @return Constant text.
	 */
	@Nonnull
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 * 		New constant text.
	 */
	public void setText(@Nonnull String text) {
		this.text = text;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpUtf8 cpUtf8 = (CpUtf8) o;
		return text.equals(cpUtf8.text);
	}

	@Override
	public int hashCode() {
		return text.hashCode();
	}
}
