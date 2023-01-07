package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Label;

/**
 * Instruction for the {@link Opcodes#LOOKUPSWITCH} instruction, which has a default offset and a list of offsets.
 * @see Opcodes#LOOKUPSWITCH
 */
public class LookupSwitchInsn extends Insn {
	private java.util.List<Label> labels;
	private Label defaultLabel;

	protected LookupSwitchInsn(int opcode, java.util.List<Label> labels, Label defaultLabel) {
		super(opcode);
		this.labels = labels;
		this.defaultLabel = defaultLabel;
	}

	public java.util.List<Label> getLabels() {
		return labels;
	}

	public void setLabels(java.util.List<Label> labels) {
		this.labels = labels;
	}

	public Label getDefaultLabel() {
		return defaultLabel;
	}

	public void setDefaultLabel(Label defaultLabel) {
		this.defaultLabel = defaultLabel;
	}
}
