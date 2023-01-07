package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Constant;

/**
 * Instruction for the group of {@link Opcodes#LDC} instructions which have a constant operand.
 * @see Opcodes#LDC_W
 * @see Opcodes#LDC2_W
 */
public class LdcInsn extends Insn {

	private Constant constant;

	protected LdcInsn(int opcode, Constant constant) {
		super(opcode);
		this.constant = constant;
	}

	public Constant getConstant() {
		return constant;
	}

	public void setConstant(Constant constant) {
		this.constant = constant;
	}

}
