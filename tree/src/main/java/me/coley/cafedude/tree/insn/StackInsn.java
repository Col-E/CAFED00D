package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction for the stack manipulation instructions with no operands.
 * Includes the following instructions:
 * <ul>
 *     <li>{@link Opcodes#POP}</li>
 *     <li>{@link Opcodes#POP2}</li>
 *     <li>{@link Opcodes#DUP}</li>
 *     <li>{@link Opcodes#DUP_X1}</li>
 *     <li>{@link Opcodes#DUP_X2}</li>
 *     <li>{@link Opcodes#DUP2}</li>
 *     <li>{@link Opcodes#DUP2_X1}</li>
 *     <li>{@link Opcodes#DUP2_X2}</li>
 *     <li>{@link Opcodes#SWAP}</li>
 * </ul>
 */
public class StackInsn extends Insn {
	/**
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public StackInsn(int opcode) {
		super(InsnKind.STACK, opcode);
	}
}
