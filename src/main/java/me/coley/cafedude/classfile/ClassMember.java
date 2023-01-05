package me.coley.cafedude.classfile;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.behavior.AttributeHolder;
import me.coley.cafedude.classfile.behavior.CpAccessor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Base class member.
 *
 * @author Matt Coley
 */
public abstract class ClassMember implements AttributeHolder, CpAccessor {
	private List<Attribute> attributes;
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

	@Override
	public List<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public <T extends Attribute> T getAttribute(Class<T> type) {
		for (Attribute attribute : attributes) {
			if (type.isInstance(attribute)) {
				return type.cast(attribute);
			}
		}
		return null;
	}

	@Override
	public Set<Integer> cpAccesses() {
		Set<Integer> set = new TreeSet<>();
		set.add(getNameIndex());
		set.add(getTypeIndex());
		for (Attribute attribute : getAttributes())
			set.addAll(attribute.cpAccesses());
		return set;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + access;
		result = 31 * result + nameIndex;
		result = 31 * result + typeIndex;
		return result;
	}
}
