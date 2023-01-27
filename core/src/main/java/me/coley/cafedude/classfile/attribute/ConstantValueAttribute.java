package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

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
	 * 		Name index in constant pool.
	 * @param constantValue
	 * 		Index in the constant pool representing the value of this attribute.
	 */
	public ConstantValueAttribute(CpUtf8 name, CpEntry constantValue) {
		super(name);
		this.constantValue = constantValue;
	}

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
	 */
	public CpEntry getConstantValue() {
		return constantValue;
	}

	/**
	 * @param constantValueIndex
	 * 		Index in the constant pool representing the value of this attribute.
	 */
	public void setConstantValueIndex(CpEntry constantValue) {
		this.constantValue = constantValue;
	}
}
