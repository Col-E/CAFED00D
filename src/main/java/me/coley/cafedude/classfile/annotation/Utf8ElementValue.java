package me.coley.cafedude.classfile.annotation;

import java.util.Set;
import java.util.TreeSet;

/**
 * UTF8 string element value.
 *
 * @author Matt Coley
 */
public class Utf8ElementValue extends ElementValue {
	private int utfIndex;

	/**
	 * @param tag
	 * 		ASCII tag representation, must be {@code s}.
	 * @param utfIndex
	 * 		Index of utf8 constant.
	 */
	public Utf8ElementValue(char tag, int utfIndex) {
		super(tag);
		if (tag != 's')
			throw new IllegalArgumentException("UTF8 element value must have 's' tag");
		this.utfIndex = utfIndex;
	}

	/**
	 * @return Index of utf8 constant.
	 */
	public int getUtfIndex() {
		return utfIndex;
	}

	/**
	 * @param utfIndex
	 * 		Index of utf8 constant.
	 */
	public void setUtfIndex(int utfIndex) {
		this.utfIndex = utfIndex;
	}

	/**
	 * @return ASCII tag representation of a string, {@code s}.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

	@Override
	public Set<Integer> cpAccesses() {
		Set<Integer> set = new TreeSet<>();
		set.add(getUtfIndex());
		return set;
	}

	@Override
	public int computeLength() {
		// u1: tag
		// u2: utf8_index
		return 3;
	}
}
