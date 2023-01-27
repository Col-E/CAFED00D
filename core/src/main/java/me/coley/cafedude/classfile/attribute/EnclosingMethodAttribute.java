package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.*;

import java.util.Set;

/**
 * Enclosing method attribute
 *
 * @author JCWasmx86
 */
public class EnclosingMethodAttribute extends Attribute {
	private CpClass classEntry;
	private CpNameType methodEntry;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param classEntry
	 * 		Index into the constant pool representing the innermost class that encloses
	 * 		the declaration of the current class.
	 * @param methodEntry
	 * 		Used for anonymous classes e.g. in a method or constructor. If not, it is
	 * 		{@code null}.
	 */
	public EnclosingMethodAttribute(CpUtf8 name, CpClass classEntry, CpNameType methodEntry) {
		super(name);
		this.classEntry = classEntry;
		this.methodEntry = methodEntry;
	}

	/**
	 * @return Class index of the enclosing class.
	 */
	public CpClass getClassEntry() {
		return classEntry;
	}

	/**
	 * @return Index of the enclosing method.
	 */
	public CpNameType getMethodEntry() {
		return methodEntry;
	}

	/**
	 * @param classEntry
	 * 		Set the enclosing class index.
	 */
	public void setClassEntry(CpClass classEntry) {
		this.classEntry = classEntry;
	}

	/**
	 * @param methodEntry
	 * 		Set the enclosing method index.
	 */
	public void setMethodEntry(CpNameType methodEntry) {
		this.methodEntry = methodEntry;
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(getClassEntry());
		set.add(getMethodEntry());
		return set;
	}

	@Override
	public int computeInternalLength() {
		return 4;
	}
}
