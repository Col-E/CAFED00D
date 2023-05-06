package me.coley.cafedude.classfile.annotation;

import me.coley.cafedude.classfile.constant.CpEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Array element value.
 *
 * @author Matt Coley
 */
public class ArrayElementValue extends ElementValue {
	private List<ElementValue> array;

	/**
	 * @param tag
	 * 		ASCII tag representation, must be {@code c}.
	 * @param array
	 * 		Array contents.
	 */
	public ArrayElementValue(char tag, List<ElementValue> array) {
		super(tag);
		if (tag != '[')
			throw new IllegalArgumentException("Array element value must have '[' tag");
		this.array = array;
	}

	/**
	 * @return Array contents.
	 */
	public List<ElementValue> getArray() {
		return array;
	}

	/**
	 * @param array
	 * 		Array contents.
	 */
	public void setArray(List<ElementValue> array) {
		this.array = array;
	}

	/**
	 * @return ASCII tag representation of a class, {@code [}.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = new HashSet<>();
		for (ElementValue value : getArray())
			set.addAll(value.cpAccesses());
		return set;
	}

	@Override
	public int computeLength() {
		// u1: tag
		// u2: num_elements
		// ??: elements
		return 3 + array.stream().mapToInt(ElementValue::computeLength).sum();
	}
}
