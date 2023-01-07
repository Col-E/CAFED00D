package me.coley.cafedude.tree;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.tree.Label;

/**
 * Local variable object.
 */
public class Local {

	private String name;
	private Descriptor desc;
	private String signature;
	private Label start;
	private Label end;
	private final int index;

	/**
	 * @param index
	 * 		Local variable index.
	 */
	public Local(int index) {
		this.index = index;
	}

	/**
	 * @return Name of local variable.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * 		Name of local variable.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Descriptor of local variable.
	 */
	public Descriptor getDesc() {
		return desc;
	}

	/**
	 * @param desc
	 * 		Descriptor of local variable.
	 */
	public void setDesc(Descriptor desc) {
		this.desc = desc;
	}

	/**
	 * @return Signature of local variable.
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * @param signature
	 * 		Signature of local variable.
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

	/**
	 * @return Label of start of local variable.
	 */
	public Label getStart() {
		return start;
	}

	/**
	 * @param start
	 * 		Label of start of local variable.
	 */
	public void setStart(Label start) {
		this.start = start;
	}

	/**
	 * @return Label of end of local variable.
	 */
	public Label getEnd() {
		return end;
	}

	/**
	 * @param end
	 * 		Label of end of local variable.
	 */
	public void setEnd(Label end) {
		this.end = end;
	}

	/**
	 * @return Local variable index.
	 */
	public int getIndex() {
		return index;
	}

}
