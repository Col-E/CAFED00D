package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction for pushing a constant value onto the stack.
 * Includes the following instructions:
 * <ul>
 *     <li>{@link Opcodes#ACONST_NULL}</li>
 *     <li>{@link Opcodes#ICONST_M1}</li>
 *     <li>{@link Opcodes#ICONST_0}</li>
 *     <li>{@link Opcodes#ICONST_1}</li>
 *     <li>{@link Opcodes#ICONST_2}</li>
 *     <li>{@link Opcodes#ICONST_3}</li>
 *     <li>{@link Opcodes#ICONST_4}</li>
 *     <li>{@link Opcodes#ICONST_5}</li>
 *     <li>{@link Opcodes#LCONST_0}</li>
 *     <li>{@link Opcodes#LCONST_1}</li>
 *     <li>{@link Opcodes#FCONST_0}</li>
 *     <li>{@link Opcodes#FCONST_1}</li>
 *     <li>{@link Opcodes#FCONST_2}</li>
 *     <li>{@link Opcodes#DCONST_0}</li>
 *     <li>{@link Opcodes#DCONST_1}</li>
 * </ul>
 */
public class ConstantInsn extends Insn {
	/**
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public ConstantInsn(int opcode) {
		super(InsnKind.CONSTANT, opcode);
	}
}
