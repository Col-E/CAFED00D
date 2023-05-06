package me.coley.cafedude.classfile;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.behavior.AttributeHolder;
import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Base class member.
 *
 * @author Matt Coley
 */
public abstract class ClassMember implements AttributeHolder, CpAccessor {
	private List<Attribute> attributes;
	private int access;
	private CpUtf8 name;
	private CpUtf8 type;

	/**
	 * @param attributes
	 * 		Attributes of the member.
	 * @param access
	 * 		Member access flags.
	 * @param name
	 * 		Index of name UTF in pool.
	 * @param type
	 * 		Index of descriptor UTF in pool.
	 */
	public ClassMember(List<Attribute> attributes, int access, CpUtf8 name, CpUtf8 type) {
		this.attributes = attributes;
		this.access = access;
		this.name = name;
		this.type = type;
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
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @param name
	 * 		New index of name UTF in pool.
	 */
	public void setName(CpUtf8 name) {
		this.name = name;
	}

	/**
	 * @return Index of descriptor UTF in pool.
	 */
	public CpUtf8 getType() {
		return type;
	}

	/**
	 * @param type
	 * 		New index of descriptor UTF in pool.
	 */
	public void setType(CpUtf8 type) {
		this.type = type;
	}

	@Nonnull
	@Override
	public List<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(@Nonnull List<Attribute> attributes) {
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
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = new HashSet<>();
		set.add(getName());
		set.add(getType());
		for (Attribute attribute : getAttributes())
			set.addAll(attribute.cpAccesses());
		return set;
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributes, access, name, type);
	}
}
