package software.coley.cafedude.tree;

import jakarta.annotation.Nonnull;
import java.util.Set;
import java.util.TreeSet;

/**
 * Label to mark a position in the bytecode.
 *
 * @author Justus Garbe
 */
public class Label {
	private int offset;
	private Set<Integer> lines = new TreeSet<>();

	/**
	 * @param offset
	 * 		Offset of the label.
	 */
	public Label(int offset) {
		this.offset = offset;
	}

	/**
	 * Create an unresolved label.
	 */
	public Label() {
		this(-1);
	}

	/**
	 * @return Offset of the label.
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 * 		Offset of the label.
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Add a line number to this label, a label can have multiple line numbers.
	 *
	 * @param line
	 * 		Line number.
	 */
	public void addLineNumber(int line) {
		lines.add(line);
	}

	/**
	 * @return Line numbers associated with this label.
	 */
	@Nonnull
	public Set<Integer> getLines() {
		return lines;
	}

	/**
	 * Set the line numbers associated with this label.
	 *
	 * @param lines
	 * 		Line numbers.
	 */
	public void setLines(@Nonnull Set<Integer> lines) {
		this.lines = lines;
	}

	/**
	 * @return {@code true} when the label is resolved.
	 */
	public boolean isResolved() {
		return offset >= 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Label label = (Label) obj;
		return offset == label.offset;
	}
}
