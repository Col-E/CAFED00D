package me.coley.cafedude.tree.insn;

import me.coley.cafedude.tree.Label;

import javax.annotation.Nonnull;

/**
 * Instruction that acts as a label pointer, the label inside the instruction
 * points to the next instruction.
 *
 * @author Justus Garbe
 */
public class LabelInsn extends Insn {
	private final Label label;

	public LabelInsn(@Nonnull Label label) {
		super(InsnKind.LABEL, LABEL_INSN_OPCODE);
		this.label = label;
	}

	@Nonnull
	public Label getLabel() {
		return label;
	}

	@Override
	public int size() {
		return 0; // doesn't have a size (virtual)
	}

	@Override
	public String toString() {
		return "label(" + label.getOffset() + ")";
	}
}
