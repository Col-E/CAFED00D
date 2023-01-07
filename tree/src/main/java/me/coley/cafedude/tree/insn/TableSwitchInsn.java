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

	protected TableSwitchInsn(int opcode, int min, int max, List<Label> labels, Label defaultLabel) {
		super(opcode);
		this.labels = labels;
		this.defaultLabel = defaultLabel;
		this.min = min;
		this.max = max;
	}

	public List<Label> getLabels() {
		return labels;
	}

	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}

	public Label getDefaultLabel() {
		return defaultLabel;
	}

	public void setDefaultLabel(Label defaultLabel) {
		this.defaultLabel = defaultLabel;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

}
