package me.coley.cafedude.classfile;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.behavior.AttributeHolder;
import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
	 * 		Constant pool entry holding the name of the member.
	 * @param type
	 * 		Constant pool entry holding the type of the member.
	 */
	public ClassMember(@Nonnull List<Attribute> attributes, int access, @Nonnull CpUtf8 name, @Nonnull CpUtf8 type) {
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
	 * 		New member access flags.
	 */
	public void setAccess(int access) {
		this.access = access;
	}

	/**
	 * @return Constant pool entry holding the name of the member.
	 */
	@Nonnull
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @param name
	 * 		New constant pool entry holding the name of the member.
	 */
	public void setName(@Nonnull CpUtf8 name) {
		this.name = name;
	}

	/**
	 * @return Constant pool entry holding the type of the member.
	 */
	@Nonnull
	public CpUtf8 getType() {
		return type;
	}

	/**
	 * @param type
	 * 		New constant pool entry holding the type of the member.
	 */
	public void setType(@Nonnull CpUtf8 type) {
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

	@Nonnull
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ClassMember that = (ClassMember) o;

		if (access != that.access) return false;
		if (!attributes.equals(that.attributes)) return false;
		if (!name.equals(that.name)) return false;
		return type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributes, access, name, type);
	}
}
