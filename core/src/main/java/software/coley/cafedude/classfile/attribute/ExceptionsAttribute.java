package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Checked exceptions attribute.
 *
 * @author JCWasmx86
 */
public non-sealed class ExceptionsAttribute extends Attribute {
	private List<CpClass> exceptionTable;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param exceptionTable
	 * 		Indices into the constant pool representing all checked exceptions
	 * 		that may be thrown by this method.
	 */
	public ExceptionsAttribute(@Nonnull CpUtf8 name, @Nonnull List<CpClass> exceptionTable) {
		super(name);
		this.exceptionTable = exceptionTable;
	}

	/**
	 * @return Exception index table.
	 */
	@Nonnull
	public List<CpClass> getExceptionTable() {
		return exceptionTable;
	}

	/**
	 * @param exceptionTable
	 * 		Indices into the constant pool representing all checked exceptions
	 * 		that may be thrown by this method.
	 */
	public void setExceptionTable(@Nonnull List<CpClass> exceptionTable) {
		this.exceptionTable = exceptionTable;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.addAll(getExceptionTable());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// Multiplying with two, as each index has two bytes.
		return 2 + exceptionTable.size() * 2;
	}
}
