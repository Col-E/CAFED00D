package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;

import java.util.Set;

/**
 * Module main class attribute.
 */
public class ModuleMainClassAttribute extends Attribute implements CpAccessor {

	private int mainClassIndex;

	/**
	 * @param nameIndex Name index in constant pool.
	 * @param mainClassIndex Index of main class in constant pool.
	 */
	public ModuleMainClassAttribute(int nameIndex, int mainClassIndex) {
		super(nameIndex);
		this.mainClassIndex = mainClassIndex;
	}

	/**
	 * @return Index of main class in constant pool.
	 */
	public int getMainClassIndex() {
		return mainClassIndex;
	}

	/**
	 * @param mainClassIndex New index of main class in constant
	 * 					 pool.
	 */
	public void setMainClassIndex(int mainClassIndex) {
		this.mainClassIndex = mainClassIndex;
	}

	@Override
	public Set<Integer> cpAccesses() {
		Set<Integer> set = super.cpAccesses();
		set.add(mainClassIndex);
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: mainClassIndex
		return 2;
	}
}
