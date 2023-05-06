package me.coley.cafedude.classfile.constant;

import javax.annotation.Nonnull;
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
	 * 		Constant pool entry holding the module's name.
	 */
	public CpModule(@Nonnull CpUtf8 name) {
		super(MODULE);
		this.name = name;
	}

	/**
	 * @return Constant pool entry holding the module's name.
	 */
	@Nonnull
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @param name
	 * 		New constant pool entry holding the module's name.
	 */
	public void setName(@Nonnull CpUtf8 name) {
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
