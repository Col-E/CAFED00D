package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * Debug character range table attribute.
 * <br>
 * This is a non-standard attribute implemented by OpenJDK.
 *
 * @author Matt Coley
 */
public non-sealed class CharacterRangeTableAttribute extends Attribute {
	private List<CharacterRangeInfo> characterRangeTable;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param characterRangeTable
	 * 		Table of character range entries.
	 */
	public CharacterRangeTableAttribute(@Nonnull CpUtf8 name, @Nonnull List<CharacterRangeInfo> characterRangeTable) {
		super(name);
		this.characterRangeTable = characterRangeTable;
	}

	/**
	 * @return Table of character range entries.
	 */
	@Nonnull
	public List<CharacterRangeInfo> getCharacterRangeTable() {
		return characterRangeTable;
	}

	/**
	 * @param characterRangeTable
	 * 		Table of character range entries.
	 */
	public void setCharacterRangeTable(@Nonnull List<CharacterRangeInfo> characterRangeTable) {
		this.characterRangeTable = characterRangeTable;
	}

	@Override
	public int computeInternalLength() {
		// u2: table_size
		// entry[
		//   u2 start_pc;
		//   u2 end_pc;
		//   s4 char_range_start;
		//   s4 char_range_end;
		//   u2 flags;
		// ]
		return 2 * (14 * characterRangeTable.size());
	}

	/**
	 * Model of a single character range entry.
	 * <p/>
	 * Each character range entry associates a range of indices in the method code
	 * with a range of characters in the source file. The position in the source
	 * is encoded as {@code lineNumber << 10 + columnNumber}.
	 * <p/>
	 * Note that column numbers are not the same as byte indices in a column as multibyte
	 * characters may be present in the source file.
	 * <p/>
	 * Each character range entry includes a flag which indicates what kind of range is described.
	 */
	public static class CharacterRangeInfo {
		public static final int FLAG_STATEMENT = 0x0001;
		public static final int FLAG_BLOCK = 0x0002;
		public static final int FLAG_ASSIGNMENT = 0x0004;
		public static final int FLAG_FLOW_CONTROLLER = 0x0008;
		public static final int FLAG_FLOW_TARGET = 0x0010;
		public static final int FLAG_INVOKE = 0x0020;
		public static final int FLAG_CREATE = 0x0040;
		public static final int FLAG_BRANCH_TRUE = 0x0080;
		public static final int FLAG_BRANCH_FALSE = 0x0100;
		private int startPc;
		private int endPc;
		private int characterRangeStart;
		private int characterRangeEnd;
		private int flags;

		/**
		 * @param startPc
		 * 		Start index <i>(exclusive)</i> in the method code.
		 * @param endPc
		 * 		End index <i>(exclusive)</i> in the method code.
		 * @param characterRangeStart
		 * 		End index <i>(exclusive)</i> in the method code.
		 * @param characterRangeEnd
		 * 		End index <i>(exclusive)</i> in the method code.
		 * @param flags
		 * 		Flags of this character range entry.
		 */
		public CharacterRangeInfo(int startPc, int endPc, int characterRangeStart, int characterRangeEnd, int flags) {
			this.startPc = startPc;
			this.endPc = endPc;
			this.characterRangeStart = characterRangeStart;
			this.characterRangeEnd = characterRangeEnd;
			this.flags = flags;
		}

		/**
		 * @return Start index <i>(exclusive)</i> in the method code.
		 */
		public int getStartPc() {
			return startPc;
		}

		/**
		 * @param startPc
		 * 		Start index <i>(exclusive)</i> in the method code.
		 */
		public void setStartPc(int startPc) {
			this.startPc = startPc;
		}

		/**
		 * @return End index <i>(exclusive)</i> in the method code.
		 */
		public int getEndPc() {
			return endPc;
		}

		/**
		 * @param endPc
		 * 		End index <i>(exclusive)</i> in the method code.
		 */
		public void setEndPc(int endPc) {
			this.endPc = endPc;
		}

		/**
		 * @return Encoded start of character positions <i>(exclusive)</i> in the source file.
		 */
		public int getCharacterRangeStart() {
			return characterRangeStart;
		}

		/**
		 * @param characterRangeStart
		 * 		Encoded start of character positions <i>(exclusive)</i> in the source file.
		 */
		public void setCharacterRangeStart(int characterRangeStart) {
			this.characterRangeStart = characterRangeStart;
		}

		/**
		 * @param line
		 * 		Line of start character position in the source file.
		 * @param column
		 * 		Column of start character position in the source file.
		 */
		public void setCharacterRangeStart(int line, int column) {
			setCharacterRangeStart(line << 10 + column);
		}

		/**
		 * @return Encoded end of character positions <i>(exclusive)</i> in the source file.
		 */
		public int getCharacterRangeEnd() {
			return characterRangeEnd;
		}

		/**
		 * @param characterRangeEnd
		 * 		Encoded end of character positions <i>(exclusive)</i> in the source file.
		 */
		public void setCharacterRangeEnd(int characterRangeEnd) {
			this.characterRangeEnd = characterRangeEnd;
		}

		/**
		 * @param line
		 * 		Line of end character position in the source file.
		 * @param column
		 * 		Column of end character position in the source file.
		 */
		public void setCharacterRangeEnd(int line, int column) {
			setCharacterRangeStart(line << 10 + column);
		}

		/**
		 * @return Flags of this character range entry.
		 */
		public int getFlags() {
			return flags;
		}

		/**
		 * @param flags
		 * 		Flags to assign to this character range entry.
		 */
		public void setFlags(int flags) {
			this.flags = flags;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof CharacterRangeInfo that)) return false;

			if (startPc != that.startPc) return false;
			if (endPc != that.endPc) return false;
			if (characterRangeStart != that.characterRangeStart) return false;
			if (characterRangeEnd != that.characterRangeEnd) return false;
			return flags == that.flags;
		}

		@Override
		public int hashCode() {
			int result = startPc;
			result = 31 * result + endPc;
			result = 31 * result + characterRangeStart;
			result = 31 * result + characterRangeEnd;
			result = 31 * result + flags;
			return result;
		}
	}
}
