package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Label;

import java.util.List;

/**
 * Instruction for the {@link Opcodes#LOOKUPSWITCH} instruction, which has a default offset and a list of offsets.
 * @see Opcodes#LOOKUPSWITCH
 */
public class LookupSwitchInsn extends Insn {

	private List<Integer> keys;
	private List<Label> labels;
	private Label defaultLabel;

	/**
	 * @param labels
	 * 		Offsets of the switch.
	 * @param defaultLabel
	 * 		Default offset of the switch.
	 */
	public LookupSwitchInsn(List<Integer> keys, List<Label> labels, Label defaultLabel) {
		super(InsnKind.LOOKUP_SWITCH, Opcodes.LOOKUPSWITCH);
		this.keys = keys;
		this.labels = labels;
		this.defaultLabel = defaultLabel;
	}

	/**
	 * @return Keys of the switch.
	 */
	public List<Integer> getKeys() {
		return keys;
	}

	/**
	 * @param keys
	 * 		Keys of the switch.
	 */
	public void setKeys(List<Integer> keys) {
		this.keys = keys;
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

	@Override
	public int size() {
		// u1 opcode
		// u4 padding
		// u4 default
		// u4 npairs
		// { u4 key, u4 offset } npairs times
		return 1 + 4 + 4 + 4 + (keys.size() * 8);
	}
}
