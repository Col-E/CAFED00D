package me.coley.cafedude.attribute;

import java.util.List;

/**
 * Variable table attribute.
 *
 * @author Matt Coley
 */
public class LocalVariableTableAttribute extends Attribute {
	private List<VarEntry> entries;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param entries
	 * 		Variable table entries.
	 */
	public LocalVariableTableAttribute(int nameIndex, List<VarEntry> entries) {
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
		//   u2 descriptor_index;
		//   u2 index;
		// ]
		return 2 + (10 * entries.size());
	}

	/**
	 * @return Table entries.
	 */
	public List<VarEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 * 		New table entries.
	 */
	public void setEntries(List<VarEntry> entries) {
		this.entries = entries;
	}

	/**
	 * Variable table entry.
	 */
	public static class VarEntry {
		private final int startPc;
		private final int length;
		private final int nameIndex;
		private final int descIndex;
		private final int index;

		/**
		 * @param startPc
		 * 		Bytecode offset var starts at.
		 * @param length
		 * 		Bytecode length var spans across.
		 * @param nameIndex
		 * 		CP UTF8 name index.
		 * @param descIndex
		 * 		CP UTF8 desc index.
		 * @param index
		 * 		Variable index.
		 */
		public VarEntry(int startPc, int length, int nameIndex, int descIndex, int index) {
			this.startPc = startPc;
			this.length = length;
			this.nameIndex = nameIndex;
			this.descIndex = descIndex;
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
		 * @return CP UTF8 desc index.
		 */
		public int getDescIndex() {
			return descIndex;
		}

		/**
		 * @return Variable index.
		 */
		public int getIndex() {
			return index;
		}
	}
}
