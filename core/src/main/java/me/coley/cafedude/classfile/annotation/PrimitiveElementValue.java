package me.coley.cafedude.classfile.annotation;

import me.coley.cafedude.classfile.constant.CpEntry;

import java.util.Collections;
import java.util.Set;

/**
 * Primitive value element value.
 *
 * @author Matt Coley
 */
public class PrimitiveElementValue extends ElementValue {
	private CpEntry value;

	/**
	 * @param tag
	 * 		ASCII tag representation, indicating the type of primitive element value.
	 * @param value
	 * 		Index of primitive value constant.
	 */
	public PrimitiveElementValue(char tag, CpEntry value) {
		super(tag);
		this.value = value;
	}

	/**
	 * @return Index of primitive value constant.
	 */
	public CpEntry getValue() {
		return value;
	}

	/**
	 * @param value
	 * 		Index of primitive value constant.
	 */
	public void setValue(CpEntry value) {
		this.value = value;
	}

	/**
	 * @return ASCII tag representation, indicating the type of primitive element value.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		return Collections.singleton(value);
	}

	@Override
	public int computeLength() {
		// u1: tag
		// u2: value_index
		return 3;
	}
}
