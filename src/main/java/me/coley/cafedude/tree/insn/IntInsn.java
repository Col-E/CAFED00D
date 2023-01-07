package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction which has a signed integer operand.
 * Instructions that use this is:
 * <ul>
 *     <li>{@link Opcodes#BIPUSH}</li>
 *     <li>{@link Opcodes#SIPUSH}</li>
 *     <li>{@link Opcodes#NEWARRAY}</li>
 * </ul>
 */
public class IntInsn extends Insn {

	protected int operand;

	protected IntInsn(int opcode, int operand) {
		super(opcode);
		this.operand = operand;
	}

	public int getOperand() {
		return operand;
	}

	public void setOperand(int operand) {
		this.operand = operand;
	}

}
