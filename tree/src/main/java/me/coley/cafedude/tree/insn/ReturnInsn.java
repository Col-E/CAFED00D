package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction for the {@link Opcodes#RETURN} instructions, which have no operands.
 * Instructions that use this are:
 * <ul>
 *     <li>{@link Opcodes#RETURN}</li>
 *     <li>{@link Opcodes#ARETURN}</li>
 *     <li>{@link Opcodes#DRETURN}</li>
 *     <li>{@link Opcodes#FRETURN}</li>
 *     <li>{@link Opcodes#IRETURN}</li>
 *     <li>{@link Opcodes#LRETURN}</li>
 * </ul>
 *
 * @author Justus Garbe
 */
public class ReturnInsn extends Insn {

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public ReturnInsn(int opcode) {
		super(InsnKind.RETURN, opcode);
	}

}
