package me.coley.cafedude;

import me.coley.cafedude.tree.Label;
import me.coley.cafedude.tree.insn.Insn;
import me.coley.cafedude.util.OpcodeUtil;

public class UnresolvedLabelException extends InvalidCodeException {

	private final Label label;
	private final int offset;
	private final Insn instruction;

	public UnresolvedLabelException(Label label, int offset, Insn instruction) {
		super("Unresolved label at " + offset + ": "
				+ OpcodeUtil.getOpcodeName(instruction.getOpcode()));
		this.label = label;
		this.offset = offset;
		this.instruction = instruction;
	}

	public UnresolvedLabelException(Label label, String where) {
		super("Unresolved label at " + where);
		this.label = label;
		this.offset = -1;
		this.instruction = null;
	}

	public Label getLabel() {
		return label;
	}

	public int getOffset() {
		return offset;
	}

	public Insn getInstruction() {
		return instruction;
	}



}
