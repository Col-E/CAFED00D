package me.coley.cafedude.tree;

import java.util.List;

public class Label {

	private String name;
	private int offset;
	private List<Integer> lines;

	public Label(String name, int offset) {
		this.name = name;
		this.offset = offset;
	}

	public Label(String name) {
		this(name, -1);
	}

	public String getName() {
		return name;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setName(String name) {
		this.name = name;
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
