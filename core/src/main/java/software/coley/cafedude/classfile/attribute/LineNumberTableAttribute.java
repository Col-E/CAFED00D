package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * Line numbers attribute.
 *
 * @author Matt Coley
 */
public class LineNumberTableAttribute extends Attribute {
	private List<LineEntry> entries;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param entries
	 * 		Line number table entries.
	 */
	public LineNumberTableAttribute(@Nonnull CpUtf8 name, @Nonnull List<LineEntry> entries) {
		super(name);
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
	@Nonnull
	public List<LineEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 * 		New table entries.
	 */
	public void setEntries(@Nonnull List<LineEntry> entries) {
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

		@Override
		public String toString() {
			return "LineEntry{" +
					"startPc=" + startPc +
					", line=" + line +
					'}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			LineEntry lineEntry = (LineEntry) o;

			if (startPc != lineEntry.startPc) return false;
			return line == lineEntry.line;
		}

		@Override
		public int hashCode() {
			int result = startPc;
			result = 31 * result + line;
			return result;
		}
	}
}
