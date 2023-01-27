package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import java.util.List;
import java.util.Set;

/**
 * Permitted classes attribute.
 *
 * @author Matt Coley
 */
public class PermittedClassesAttribute extends Attribute {
	private List<CpClass> classes;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param classes
	 * 		Indices of allowed {@code CP_CLASS} values.
	 */
	public PermittedClassesAttribute(CpUtf8 name, List<CpClass> classes) {
		super(name);
		this.classes = classes;
	}

	/**
	 * @return Indices of allowed {@code CP_CLASS} values.
	 */
	public List<CpClass> getClasses() {
		return classes;
	}

	/**
	 * @param classes
	 * 		New indices of allowed {@code CP_CLASS} values.
	 */
	public void setClasses(List<CpClass> classes) {
		this.classes = classes;
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.addAll(getClasses());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// u2: count
		// u2: class_index * count
		return 2 + (2 * classes.size());
	}
}
