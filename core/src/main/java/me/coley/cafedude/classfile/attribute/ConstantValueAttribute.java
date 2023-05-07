package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Constant value attribute
 *
 * @author JCWasmx86
 */
public class ConstantValueAttribute extends Attribute {
	private CpEntry constantValue;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param constantValue
	 * 		Index in the constant pool representing the value of this attribute.
	 */
	public ConstantValueAttribute(@Nonnull CpUtf8 name, @Nonnull CpEntry constantValue) {
		super(name);
		this.constantValue = constantValue;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(getConstantValue());
		return set;
	}

	@Override
	public int computeInternalLength() {
		return 2;
	}

	/**
	 * @return Index in the constant pool representing the value of this attribute.
	 */@Nonnull
	public CpEntry getConstantValue() {
		return constantValue;
	}

	/**
	 * @param constantValueIndex
	 * 		Index in the constant pool representing the value of this attribute.
	 */
	public void setConstantValueIndex(@Nonnull CpEntry constantValue) {
		this.constantValue = constantValue;
	}
}
