package me.coley.cafedude.classfile.constant;

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
	 * 		Index of package name UTF in pool.
	 */
	public CpPackage(CpUtf8 packageName) {
		super(PACKAGE);
		this.packageName = packageName;
	}

	/**
	 * @return Index of package name UTF in pool.
	 */
	public CpUtf8 getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName
	 * 		New index of package name UTF in pool.
	 */
	public void setPackageName(CpUtf8 packageName) {
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
