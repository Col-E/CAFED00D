package me.coley.cafedude.tree;

import java.util.List;

public class Label {

	private int offset;
	private List<Integer> lines;

	public Label(int offset) {
		this.offset = offset;
	}

	public Label() {
		this(-1);
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void addLineNumber(int line) {
		lines.add(line);
	}

	public List<Integer> getLines() {
		return lines;
	}

	public void setLines(List<Integer> lines) {
		this.lines = lines;
	}

}
