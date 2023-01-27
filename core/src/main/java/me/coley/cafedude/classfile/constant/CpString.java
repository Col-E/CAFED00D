package me.coley.cafedude.classfile.constant;

/**
 * String pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public class CpString extends CpEntry {
	private CpUtf8 string;

	/**
	 * @param string
	 * 		Index of UTF string in pool.
	 */
	public CpString(CpUtf8 string) {
		super(STRING);
		this.string = string;
	}

	/**
	 * @return Index of UTF string in pool.
	 */
	public CpUtf8 getString() {
		return string;
	}

	/**
	 * @param string
	 * 		New index of UTF string in pool.
	 */
	public void setString(CpUtf8 string) {
		this.string = string;
	}
}
