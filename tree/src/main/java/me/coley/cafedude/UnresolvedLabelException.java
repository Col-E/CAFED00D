package me.coley.cafedude;

import me.coley.cafedude.tree.Label;
import me.coley.cafedude.tree.insn.Insn;
import me.coley.cafedude.util.OpcodeUtil;

/**
 * Exception thrown for unresolved label references.
 *
 * @author Justus Garbe
 */
public class UnresolvedLabelException extends InvalidCodeException {
	private final Label label;
	private final int offset;
	private final Insn instruction;

	/**
	 * @param label
	 *		Label that was unresolved.
	 * @param offset
	 * 		Offset of the instruction that referenced the label.
	 * @param instruction
	 * 		Instruction that referenced the label.
	 */
	public UnresolvedLabelException(Label label, int offset, Insn instruction) {
		super("Unresolved label at " + offset + ": "
				+ OpcodeUtil.getOpcodeName(instruction.getOpcode()));
		this.label = label;
		this.offset = offset;
		this.instruction = instruction;
	}

	/**
	 * @param label
	 * 		Label that was unresolved.
	 * @param where
	 * 		Location of the label reference.
	 */
	public UnresolvedLabelException(Label label, String where) {
		super("Unresolved label at " + where);
		this.label = label;
		this.offset = -1;
		this.instruction = null;
	}

	/**
	 * @return Label that was unresolved.
	 */
	public Label getLabel() {
		return label;
	}

	/**
	 * @return Offset of the instruction that referenced the label.
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @return Instruction that referenced the label.
	 */
	public Insn getInstruction() {
		return instruction;
	}
}
