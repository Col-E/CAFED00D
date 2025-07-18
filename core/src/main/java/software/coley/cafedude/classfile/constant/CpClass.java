package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

/**
 * Class pool entry. Points to a UTF constant.
 *
 * @author Matt Coley
 */
public non-sealed class CpClass extends CpEntry implements CrossCpReferencing, LoadableConstant {
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
	 * @return Constant pool entry holding the class name.
	 */
	@Nonnull
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @param name
	 * 		New constant pool entry holding the class name.
	 */
	public void setName(@Nonnull CpUtf8 name) {
		this.name = name;
	}

	@Nonnull
	@Override
	public Collection<CpEntry> getReferences() {
		return Collections.singletonList(name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CpClass cpClass = (CpClass) o;

		return name.equals(cpClass.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "class=" + name.getText();
	}
}
