package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Nest host attribute, points to host class.
 *
 * @author Matt Coley
 */
public class NestHostAttribute extends Attribute {
	private CpClass hostClass;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param hostClass
	 * 		Constant pool entry holding the host class type.
	 */
	public NestHostAttribute(@Nonnull CpUtf8 name, @Nonnull CpClass hostClass) {
		super(name);
		this.hostClass = hostClass;
	}

	/**
	 * @return Constant pool entry holding the host class type.
	 */
	@Nonnull
	public CpClass getHostClass() {
		return hostClass;
	}

	/**
	 * @param hostClass
	 * 		New constant pool entry holding the host class type.
	 */
	public void setHostClass(@Nonnull CpClass hostClass) {
		this.hostClass = hostClass;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(getHostClass());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: hostClassIndex
		return 2;
	}
}
