package software.coley.cafedude.classfile.constant;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Package pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public class CpPackage extends CpEntry {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpPackage that = (CpPackage) o;
		return Objects.equals(packageName, that.packageName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(packageName);
	}
}
