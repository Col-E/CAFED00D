package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;

import java.util.Set;
import java.util.TreeSet;

/**
 * Base attribute.
 *
 * @author Matt Coley
 */
public abstract class Attribute implements CpAccessor {
	private final int nameIndex;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 */
	public Attribute(int nameIndex) {
		this.nameIndex = nameIndex;
	}

	/**
	 * @return Name index in constant pool.
	 */
	public int getNameIndex() {
		return nameIndex;
	}

	/**
	 * @return Computed size for the internal length value of this attribute for serialization.
	 */
	public abstract int computeInternalLength();

	/**
	 * Complete length is the {@link #getNameIndex() U2:name_index}
	 * plus the {@link #computeInternalLength() U4:attribute_length}
	 * plus the {@link #computeInternalLength() internal length}
	 *
	 * @return Computed size for the complete attribute.
	 */
	public int computeCompleteLength() {
		// u2: Name index
		// u4: Attribute length
		// ??: Internal length
		return 6 + computeInternalLength();
	}

	@Override
	public Set<Integer> cpAccesses() {
		Set<Integer> set = new TreeSet<>();
		set.add(getNameIndex());
		return set;
	}
}
