package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Label;

import java.util.List;

/**
 * Instruction for the {@link Opcodes#TABLESWITCH} instruction, which has a default offset and a list of offsets.
 * @see Opcodes#TABLESWITCH
 */
public class TableSwitchInsn extends Insn {

	private int min;
	private int max;
	private List<Label> labels;
	private Label defaultLabel;

	/**
	 * @param min
	 * 		Minimum value of the switch.
	 * @param max
	 * 		Maximum value of the switch.
	 * @param labels
	 * 		Offsets of the switch.
	 * @param defaultLabel
	 * 		Default offset of the switch.
	 */
	public TableSwitchInsn(int opcode, int min, int max, List<Label> labels, Label defaultLabel) {
		super(opcode);
		this.labels = labels;
		this.defaultLabel = defaultLabel;
		this.min = min;
		this.max = max;
	}

	/**
	 * @return Minimum value of the switch.
	 */
	public List<Label> getLabels() {
		return labels;
	}

	/**
	 * @param labels
	 * 		Offsets of the switch.
	 */
	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}

	/**
	 * @return Default offset of the switch.
	 */
	public Label getDefaultLabel() {
		return defaultLabel;
	}

	/**
	 * @param defaultLabel
	 * 		Default offset of the switch.
	 */
	public void setDefaultLabel(Label defaultLabel) {
		this.defaultLabel = defaultLabel;
	}

	/**
	 * @return Minimum value of the switch.
	 */
	public int getMin() {
		return min;
	}

	/**
	 * @param min
	 * 		Minimum value of the switch.
	 */
	public void setMin(int min) {
		this.min = min;
	}

	/**
	 * @return Maximum value of the switch.
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @param max
	 * 		Maximum value of the switch.
	 */
	public void setMax(int max) {
		this.max = max;
	}

}
