package me.coley.cafedude;

import me.coley.cafedude.attribute.Attribute;

import java.util.List;

/**
 * Base class member.
 *
 * @author Matt Coley
 */
public abstract class ClassMember {
	private final List<Attribute> attributes;
	private int access;
	private int nameIndex;
	private int typeIndex;

	/**
	 * @param attributes
	 * 		Attributes of the member.
	 * @param access
	 * 		Member access flags.
	 * @param nameIndex
	 * 		Index of name UTF in pool.
	 * @param typeIndex
	 * 		Index of descriptor UTF in pool.
	 */
	public ClassMember(List<Attribute> attributes, int access, int nameIndex, int typeIndex) {
		this.attributes = attributes;
		this.access = access;
		this.nameIndex = nameIndex;
		this.typeIndex = typeIndex;
	}

	/**
	 * @return Attributes of the member.
	 */
	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @return Member access flags.
	 */
	public int getAccess() {
		return access;
	}

	/**
	 * @param access
	 * 		New  member access flags.
	 */
	public void setAccess(int access) {
		this.access = access;
	}

	/**
	 * @return Index of name UTF in pool.
	 */
	public int getNameIndex() {
		return nameIndex;
	}

	/**
	 * @param nameIndex
	 * 		New index of name UTF in pool.
	 */
	public void setNameIndex(int nameIndex) {
		this.nameIndex = nameIndex;
	}

	/**
	 * @return Index of descriptor UTF in pool.
	 */
	public int getTypeIndex() {
		return typeIndex;
	}

	/**
	 * @param typeIndex
	 * 		New index of descriptor UTF in pool.
	 */
	public void setTypeIndex(int typeIndex) {
		this.typeIndex = typeIndex;
	}
}
