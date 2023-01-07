package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Label;

import java.util.List;

/**
 * Instruction for the {@link Opcodes#LOOKUPSWITCH} instruction, which has a default offset and a list of offsets.
 * @see Opcodes#LOOKUPSWITCH
 */
public class LookupSwitchInsn extends Insn {
	private List<Label> labels;
	private Label defaultLabel;

	/**
	 * @param labels
	 * 		Offsets of the switch.
	 * @param defaultLabel
	 * 		Default offset of the switch.
	 */
	public LookupSwitchInsn(List<Label> labels, Label defaultLabel) {
		super(Opcodes.LOOKUPSWITCH);
		this.labels = labels;
		this.defaultLabel = defaultLabel;
	}

	/**
	 * @return Offsets of the switch.
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
}
