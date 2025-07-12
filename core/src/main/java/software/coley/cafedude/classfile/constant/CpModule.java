package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

/**
 * Module pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public non-sealed class CpModule extends CpEntry implements CrossCpReferencing {
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

	@Nonnull
	@Override
	public Collection<CpEntry> getReferences() {
		return Collections.singletonList(name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CpModule)) return false;

		CpModule cpModule = (CpModule) o;

		return name.equals(cpModule.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "module=" + name.getText();
	}
}
