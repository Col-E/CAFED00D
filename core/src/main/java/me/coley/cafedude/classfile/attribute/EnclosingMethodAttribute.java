package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpNameType;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
	 * 		Constant pool entry holding the attribute name.
	 * @param classEntry
	 * 		Constant pool entry holding the enclosing class type.
	 * @param methodEntry
	 * 		Constant pool entry holding the enclosing method name and descriptor, if known.
	 * 		May be {@code null}.
	 */
	public EnclosingMethodAttribute(@Nonnull CpUtf8 name, @Nonnull CpClass classEntry, @Nullable CpNameType methodEntry) {
		super(name);
		this.classEntry = classEntry;
		this.methodEntry = methodEntry;
	}

	/**
	 * @return Constant pool entry holding the enclosing class type.
	 */
	@Nonnull
	public CpClass getClassEntry() {
		return classEntry;
	}

	/**
	 * @return Constant pool entry holding the enclosing method name and descriptor, if known.
	 * May be {@code null} if the containing method was a:
	 * <ul>
	 *     <li>Constructor</li>
	 *     <li>Static initializer</li>
	 *     <li>Instance field initializer <i>(Gets auto-generated into the constructor)</i></li>
	 *     <li>static field initializer <i>(Gets auto-generated into the static initializer)</i></li>
	 * </ul>
	 */
	@Nullable
	public CpNameType getMethodEntry() {
		return methodEntry;
	}

	/**
	 * @param classEntry
	 * 		New constant pool entry holding the enclosing class type.
	 */
	public void setClassEntry(@Nonnull CpClass classEntry) {
		this.classEntry = classEntry;
	}

	/**
	 * @param methodEntry
	 * 		New constant pool entry holding the enclosing method name and descriptor, if known.
	 */
	public void setMethodEntry(@Nullable CpNameType methodEntry) {
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
