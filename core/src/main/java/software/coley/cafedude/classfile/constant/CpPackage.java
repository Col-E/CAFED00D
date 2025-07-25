package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Package pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public non-sealed class CpPackage extends CpEntry implements CrossCpReferencing {
	private CpUtf8 packageName;

	/**
	 * @param packageName
	 * 		Constant pool entry holding the package name.
	 */
	public CpPackage(@Nonnull CpUtf8 packageName) {
		super(PACKAGE);
		this.packageName = packageName;
	}

	/**
	 * @return Constant pool entry holding the package name.
	 */
	@Nonnull
	public CpUtf8 getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName
	 * 		New constant pool entry holding the package name.
	 */
	public void setPackageName(@Nonnull CpUtf8 packageName) {
		this.packageName = packageName;
	}

	@Nonnull
	@Override
	public Collection<CpEntry> getReferences() {
		return Collections.singletonList(packageName);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CpPackage cpPackage)) return false;

		return Objects.equals(packageName, cpPackage.packageName);
	}

	@Override
	public int hashCode() {
		return packageName.hashCode();
	}

	@Override
	public String toString() {
		return "package=" + getPackageName().getText();
	}
}
