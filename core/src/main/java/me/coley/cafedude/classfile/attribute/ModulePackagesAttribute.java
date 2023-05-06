package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpPackage;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Module packages attribute.
 *
 * @author Justus Garbe
 */
public class ModulePackagesAttribute extends Attribute implements CpAccessor {
	private List<CpPackage> packages;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param packages
	 * 		Indexes of packages in constant pool.
	 */
	public ModulePackagesAttribute(@Nonnull CpUtf8 name, @Nonnull List<CpPackage> packages) {
		super(name);
		this.packages = packages;
	}

	/**
	 * @return Indexes of packages in constant pool.
	 */
	@Nonnull
	public List<CpPackage> getPackages() {
		return packages;
	}

	/**
	 * @param packages
	 * 		New indexes of packages in constant
	 * 		pool.
	 */
	public void setPackages(@Nonnull List<CpPackage> packages) {
		this.packages = packages;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.addAll(packages);
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: numPackages
		// U2: packageIndex * numPackages
		return 2 + (packages.size() * 2);
	}
}
