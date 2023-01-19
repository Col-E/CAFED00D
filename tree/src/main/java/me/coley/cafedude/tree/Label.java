package me.coley.cafedude.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Label to mark a position in the bytecode.
 */
public class Label {

	private int offset;
	private List<Integer> lines = new ArrayList<>();

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
	public List<Integer> getLines() {
		return lines;
	}

	/**
	 * Set the line numbers associated with this label.
	 *
	 * @param lines
	 * 		Line numbers.
	 */
	public void setLines(List<Integer> lines) {
		this.lines = lines;
	}

	public boolean isResolved() {
		return offset != -1;
	}

}
