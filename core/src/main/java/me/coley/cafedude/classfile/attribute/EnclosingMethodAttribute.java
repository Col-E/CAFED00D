package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpNameType;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
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
	 *        {@code null}.
	 */
	public EnclosingMethodAttribute(@Nonnull CpUtf8 name, @Nonnull CpClass classEntry, @Nonnull CpNameType methodEntry) {
		super(name);
		this.classEntry = classEntry;
		this.methodEntry = methodEntry;
	}

	/**
	 * @return Class index of the enclosing class.
	 */
	@Nonnull
	public CpClass getClassEntry() {
		return classEntry;
	}

	/**
	 * @return Index of the enclosing method.
	 */
	@Nonnull
	public CpNameType getMethodEntry() {
		return methodEntry;
	}

	/**
	 * @param classEntry
	 * 		Set the enclosing class index.
	 */
	public void setClassEntry(@Nonnull CpClass classEntry) {
		this.classEntry = classEntry;
	}

	/**
	 * @param methodEntry
	 * 		Set the enclosing method index.
	 */
	public void setMethodEntry(@Nonnull CpNameType methodEntry) {
		this.methodEntry = methodEntry;
	}

	@Nonnull
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
