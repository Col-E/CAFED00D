package me.coley.cafedude.tree;

public class Label {

	private String name;
	private int offset;

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

}
