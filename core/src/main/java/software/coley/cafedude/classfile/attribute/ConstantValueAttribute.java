package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
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
	 * 		Constant pool entry holding the constant value.
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
	 * @return Constant pool entry holding the constant value.
	 */
	@Nonnull
	public CpEntry getConstantValue() {
		return constantValue;
	}

	/**
	 * @param constantValue
	 * 		New constant pool entry holding the constant value.
	 */
	public void setConstantValueIndex(@Nonnull CpEntry constantValue) {
		this.constantValue = constantValue;
	}
}
