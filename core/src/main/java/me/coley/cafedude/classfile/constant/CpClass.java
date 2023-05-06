package me.coley.cafedude.classfile.constant;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Class pool entry. Points to a UTF constant.
 *
 * @author Matt Coley
 */
public class CpClass extends CpEntry {
	private CpUtf8 name;

	/**
	 * @param name
	 * 		Constant pool entry holding the class name.
	 */
	public CpClass(@Nonnull CpUtf8 name) {
		super(CLASS);
		this.name = name;
	}

	/**
	 * @return Index of class name UTF in pool.
	 */
	@Nonnull
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @param name
	 * 		New index of class name UTF in pool.
	 */
	public void setName(@Nonnull CpUtf8 name) {
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
