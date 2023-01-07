package me.coley.cafedude.tree.frame;

import me.coley.cafedude.tree.Label;

/**
 * Contains a label which represents the offset of the instruction which created
 * the value.
 */
public class UninitializedValue extends Value {

	private Label label;

	public UninitializedValue(Label label) {
		this.label = label;
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}

}
