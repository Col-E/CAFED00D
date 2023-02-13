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

	public LdcInsn(Constant constant) {
		this(Opcodes.LDC, constant);
	}

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param constant
	 * 		Constant operand.
	 */
	public LdcInsn(int opcode, Constant constant) {
		super(InsnKind.LDC, opcode);
		this.constant = constant;
	}

	/**
	 * @return Constant operand.
	 */
	public Constant getConstant() {
		return constant;
	}

	/**
	 * @param constant
	 * 		Constant operand.
	 */
	public void setConstant(Constant constant) {
		this.constant = constant;
	}

	@Override
	public int size() {
		switch (getOpcode()) {
			case Opcodes.LDC:
				return 2;
			case Opcodes.LDC_W:
			case Opcodes.LDC2_W:
				return 3;
			default:
				throw new IllegalStateException("Invalid opcode for LdcInsn: " + getOpcode());
		}
	}
}
