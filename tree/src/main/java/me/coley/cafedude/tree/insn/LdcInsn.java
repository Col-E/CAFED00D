package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Constant;

import javax.annotation.Nonnull;

/**
 * Instruction for the group of {@link Opcodes#LDC} instructions which have a constant operand.
 *
 * @author Justus Garbe
 * @see Opcodes#LDC_W
 * @see Opcodes#LDC2_W
 */
public class LdcInsn extends Insn {
	private Constant constant;

	/**
	 * @param constant
	 * 		Constant operand.
	 */
	public LdcInsn(@Nonnull Constant constant) {
		this(Opcodes.LDC, constant);
	}

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param constant
	 * 		Constant operand.
	 */
	public LdcInsn(int opcode, @Nonnull Constant constant) {
		super(InsnKind.LDC, opcode);
		this.constant = constant;
	}

	/**
	 * @return Constant operand.
	 */
	@Nonnull
	public Constant getConstant() {
		return constant;
	}

	/**
	 * @param constant
	 * 		Constant operand.
	 */
	public void setConstant(@Nonnull Constant constant) {
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
