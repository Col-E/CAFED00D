package me.coley.cafedude.classfile.constant;

import java.util.Objects;

/**
 * NameType pool entry. Points to two UTF constants.
 *
 * @author Matt Coley
 */
public class CpNameType extends CpEntry {
	private CpUtf8 name;
	private CpUtf8 type;

	/**
	 * @param name
	 * 		Index of name UTF string in pool.
	 * @param type
	 * 		Index of descriptor UTF string in pool.
	 */
	public CpNameType(CpUtf8 name, CpUtf8 type) {
		super(NAME_TYPE);
		this.name = name;
		this.type = type;
	}

	/**
	 * @return Index of name UTF string in pool.
	 */
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @param name
	 * 		New index of name UTF string in pool.
	 */
	public void setName(CpUtf8 name) {
		this.name = name;
	}

	/**
	 * @return Index of descriptor UTF string in pool.
	 */
	public CpUtf8 getType() {
		return type;
	}

	/**
	 * @param type
	 * 		New index of descriptor UTF string in pool.
	 */
	public void setType(CpUtf8 type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpNameType that = (CpNameType) o;
		return name.equals(that.name) && type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}
}
