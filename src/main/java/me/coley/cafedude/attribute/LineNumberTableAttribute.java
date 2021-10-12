package me.coley.cafedude.attribute;

import java.util.List;

/**
 * Line numbers attribute.
 *
 * @author Matt Coley
 */
public class LineNumberTableAttribute extends Attribute {
	private List<LineEntry> entries;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param entries
	 * 		Line number table entries.
	 */
	public LineNumberTableAttribute(int nameIndex, List<LineEntry> entries) {
		super(nameIndex);
		this.entries = entries;
	}

	@Override
	public int computeInternalLength() {
		// u2: line_number_table_length
		// u2 * 2 * X
		return 2 + (4 * entries.size());
	}

	/**
	 * @return Table entries.
	 */
	public List<LineEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 * 		New table entries.
	 */
	public void setEntries(List<LineEntry> entries) {
		this.entries = entries;
	}

	/**
	 * Line number table entry.
	 */
	public static class LineEntry {
		private final int startPc;
		private final int line;

		/**
		 * @param startPc
		 * 		Start offset in bytecode.
		 * @param line
		 * 		Line number at offset.
		 */
		public LineEntry(int startPc, int line) {
			this.startPc = startPc;
			this.line = line;
		}

		/**
		 * @return Start offset in bytecode.
		 */
		public int getStartPc() {
			return startPc;
		}

		/**
		 * @return Line number at offset.
		 */
		public int getLine() {
			return line;
		}
	}
}
