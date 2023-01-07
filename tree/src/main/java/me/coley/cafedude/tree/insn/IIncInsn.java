package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction for the {@link Opcodes#IINC} instruction, which has a local variable index and an increment integer operand.
 * @see Opcodes#IINC
 */
public class IIncInsn extends Insn {

	private int index;

	private int increment;

	protected IIncInsn(int opcode, int index, int increment) {
		super(opcode);
		this.index = index;
		this.increment = increment;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIncrement() {
		return increment;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}

}
