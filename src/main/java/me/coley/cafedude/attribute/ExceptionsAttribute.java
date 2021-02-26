package me.coley.cafedude.attribute;

/**
 * Checked exceptions attribute.
 * 
 * @author JCWasmx86
 *
 */
public class ExceptionsAttribute extends Attribute {

	private int[] exceptionIndexTable;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param exceptionIndexTable
	 * 		Indices into the constant pool representing all checked exceptions
	 * 		that may be thrown by this method.
	 */
	public ExceptionsAttribute(int nameIndex, int[] exceptionIndexTable) {
		super(nameIndex);
		this.exceptionIndexTable = exceptionIndexTable;
	}

	/**
	 * @return Exception index table.
	 */
	public int[] getExceptionIndexTable() {
		return exceptionIndexTable;
	}

	/**
	 * @param exceptionIndexTable
	 * 		Indices into the constant pool representing all checked exceptions
	 * 		that may be thrown by this method.
	 */
	public void setExceptionIndexTable(int[] exceptionIndexTable) {
		this.exceptionIndexTable = exceptionIndexTable;
	}

	@Override
	public int computeInternalLength() {
		// Multiplying with two, as each index has two bytes.
		return 2 + exceptionIndexTable.length * 2;
	}
}
