package me.coley.cafedude.attribute;

import java.util.List;

/**
 * Variable generic/type table attribute.
 *
 * @author Matt Coley
 */
public class LocalVariableTypeTableAttribute extends Attribute {
	private List<VarTypeEntry> entries;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param entries
	 * 		Variable type table entries.
	 */
	public LocalVariableTypeTableAttribute(int nameIndex, List<VarTypeEntry> entries) {
		super(nameIndex);
		this.entries = entries;
	}

	@Override
	public int computeInternalLength() {
		// u2: line_number_table_length
		// entry[
		//   u2 start_pc;
		//   u2 length;
		//   u2 name_index;
		//   u2 signature_index;
		//   u2 index;
		// ]
		return 2 + (10 * entries.size());
	}

	/**
	 * @return Table entries.
	 */
	public List<VarTypeEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 * 		New ta+ble entries.
	 */
	public void setEntries(List<VarTypeEntry> entries) {
		this.entries = entries;
	}

	/**
	 * Variable table entry.
	 */
	public static class VarTypeEntry {
		private final int startPc;
		private final int length;
		private final int nameIndex;
		private final int signatureIndex;
		private final int index;

		/**
		 * @param startPc
		 * 		Bytecode offset var starts at.
		 * @param length
		 * 		Bytecode length var spans across.
		 * @param nameIndex
		 * 		CP UTF8 name index.
		 * @param signatureIndex
		 * 		CP UTF8 signature index.
		 * @param index
		 * 		Variable index.
		 */
		public VarTypeEntry(int startPc, int length, int nameIndex, int signatureIndex, int index) {
			this.startPc = startPc;
			this.length = length;
			this.nameIndex = nameIndex;
			this.signatureIndex = signatureIndex;
			this.index = index;
		}

		/**
		 * @return Bytecode offset var starts at.
		 */
		public int getStartPc() {
			return startPc;
		}

		/**
		 * @return Bytecode length var spans across.
		 */
		public int getLength() {
			return length;
		}

		/**
		 * @return CP UTF8 name index.
		 */
		public int getNameIndex() {
			return nameIndex;
		}

		/**
		 * @return CP UTF8 signature index.
		 */
		public int getSignatureIndex() {
			return signatureIndex;
		}

		/**
		 * @return Variable index.
		 */
		public int getIndex() {
			return index;
		}
	}
}
