package me.coley.cafedude.tree.frame;

import me.coley.cafedude.tree.Label;

/**
 * Contains a label which represents the offset of the instruction which created
 * the value.
 *
 * @author Justus Garbe
 */
public class UninitializedValue extends Value {
	private Label label;

	/**
	 * @param label
	 * 		Offset of the instruction which created the value.
	 */
	public UninitializedValue(Label label) {
		this.label = label;
	}

	/**
	 * @return Offset of the instruction which created the value.
	 */
	public Label getLabel() {
		return label;
	}

	/**
	 * @param label
	 * 		Offset of the instruction which created the value.
	 */
	public void setLabel(Label label) {
		this.label = label;
	}

}
