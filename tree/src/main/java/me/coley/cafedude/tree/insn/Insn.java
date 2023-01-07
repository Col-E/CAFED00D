package me.coley.cafedude.tree.insn;

public class Insn {

	private int opcode;

	protected Insn(int opcode) {
		this.opcode = opcode;
	}

	@Override
	public String toString() {
		return "insn(" + opcode + ")";
	}
}
