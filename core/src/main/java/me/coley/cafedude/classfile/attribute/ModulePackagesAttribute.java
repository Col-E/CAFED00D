package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;

import java.util.List;
import java.util.Set;

/**
 * Module packages attribute.
 */
public class ModulePackagesAttribute extends Attribute implements CpAccessor {

	private List<Integer> packageIndexes;

	/**
	 * @param nameIndex Name index in constant pool.
	 * @param packageIndexes Indexes of packages in constant pool.
	 */
	public ModulePackagesAttribute(int nameIndex, List<Integer> packageIndexes) {
		super(nameIndex);
		this.packageIndexes = packageIndexes;
	}

	/**
	 * @return Indexes of packages in constant pool.
	 */
	public List<Integer> getPackageIndexes() {
		return packageIndexes;
	}

	/**
	 * @param packageIndexes New indexes of packages in constant
	 * 					 pool.
	 */
	public void setPackageIndexes(List<Integer> packageIndexes) {
		this.packageIndexes = packageIndexes;
	}

	@Override
	public Set<Integer> cpAccesses() {
		Set<Integer> set = super.cpAccesses();
		set.addAll(packageIndexes);
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: numPackages
		// U2: packageIndex * numPackages
		return 2 + (packageIndexes.size() * 2);
	}
}
