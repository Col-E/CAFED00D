package me.coley.cafedude.tree;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.tree.Label;

public class Local {

	private String name;
	private Descriptor desc;
	private String signature;
	private Label start;
	private Label end;
	private final int index;

	public Local(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Descriptor getDesc() {
		return desc;
	}

	public void setDesc(Descriptor desc) {
		this.desc = desc;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Label getStart() {
		return start;
	}

	public void setStart(Label start) {
		this.start = start;
	}

	public Label getEnd() {
		return end;
	}

	public void setEnd(Label end) {
		this.end = end;
	}

	public int getIndex() {
		return index;
	}

}
