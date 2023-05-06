package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Module main class attribute.
 *
 * @author Justus Garbe
 */
public class ModuleMainClassAttribute extends Attribute implements CpAccessor {
	private CpClass mainClass;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param mainClass
	 * 		Index of main class in constant pool.
	 */
	public ModuleMainClassAttribute(@Nonnull CpUtf8 name, @Nonnull CpClass mainClass) {
		super(name);
		this.mainClass = mainClass;
	}

	/**
	 * @return Index of main class in constant pool.
	 */
	@Nonnull
	public CpClass getMainClass() {
		return mainClass;
	}

	/**
	 * @param mainClass
	 * 		New index of main class in constant
	 * 		pool.
	 */
	public void setMainClass(@Nonnull CpClass mainClass) {
		this.mainClass = mainClass;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(mainClass);
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: mainClassIndex
		return 2;
	}
}
