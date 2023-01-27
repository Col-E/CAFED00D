package me.coley.cafedude.classfile.constant;

import java.util.Objects;

/**
 * Module pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public class CpModule extends CpEntry {
	private CpUtf8 name;

	/**
	 * @param name
	 * 		Index of module name UTF in pool.
	 */
	public CpModule(CpUtf8 name) {
		super(MODULE);
		this.name = name;
	}

	/**
	 * @return Index of module name UTF in pool.
	 */
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @param name
	 * 		New index of module name UTF in pool.
	 */
	public void setName(CpUtf8 name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpModule that = (CpModule) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
