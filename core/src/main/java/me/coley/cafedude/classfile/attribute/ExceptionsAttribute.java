package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import java.util.List;
import java.util.Set;

/**
 * Checked exceptions attribute.
 *
 * @author JCWasmx86
 */
public class ExceptionsAttribute extends Attribute {
	private List<CpClass> exceptionTable;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param exceptionTable
	 * 		Indices into the constant pool representing all checked exceptions
	 * 		that may be thrown by this method.
	 */
	public ExceptionsAttribute(CpUtf8 name, List<CpClass> exceptionTable) {
		super(name);
		this.exceptionTable = exceptionTable;
	}

	/**
	 * @return Exception index table.
	 */
	public List<CpClass> getExceptionTable() {
		return exceptionTable;
	}

	/**
	 * @param exceptionTable
	 * 		Indices into the constant pool representing all checked exceptions
	 * 		that may be thrown by this method.
	 */
	public void setExceptionTable(List<CpClass> exceptionTable) {
		this.exceptionTable = exceptionTable;
	}

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
