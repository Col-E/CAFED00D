package me.coley.cafedude.classfile.constant;

import java.util.Objects;

/**
 * Class pool entry. Points to a UTF constant.
 *
 * @author Matt Coley
 */
public class CpClass extends CpEntry {
	private CpUtf8 name;

	/**
	 * @param index
	 * 		Index of class name UTF in pool.
	 */
	public CpClass(CpUtf8 name) {
		super(CLASS);
		this.name = name;
	}

	/**
	 * @return Index of class name UTF in pool.
	 */
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @param name
	 * 		New index of class name UTF in pool.
	 */
	public void setName(CpUtf8 name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpClass cpClass = (CpClass) o;
		return Objects.equals(name, cpClass.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
