package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction for the group of array operations with no operands.
 * Instructions that use this is:
 * <ul>
 *     <li>{@link Opcodes#IALOAD}</li>
 *     <li>{@link Opcodes#LALOAD}</li>
 *     <li>{@link Opcodes#FALOAD}</li>
 *     <li>{@link Opcodes#DALOAD}</li>
 *     <li>{@link Opcodes#AALOAD}</li>
 *     <li>{@link Opcodes#BALOAD}</li>
 *     <li>{@link Opcodes#CALOAD}</li>
 *     <li>{@link Opcodes#SALOAD}</li>
 *     <li>{@link Opcodes#IASTORE}</li>
 *     <li>{@link Opcodes#LASTORE}</li>
 *     <li>{@link Opcodes#FASTORE}</li>
 *     <li>{@link Opcodes#DASTORE}</li>
 *     <li>{@link Opcodes#AASTORE}</li>
 *     <li>{@link Opcodes#BASTORE}</li>
 *     <li>{@link Opcodes#CASTORE}</li>
 *     <li>{@link Opcodes#SASTORE}</li>
 *     <li>{@link Opcodes#ARRAYLENGTH}</li>
 * </ul>
 *
 * @author Justus Garbe
 */
public class ArrayInsn extends Insn {
	/**
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public ArrayInsn(int opcode) {
		super(InsnKind.ARRAY, opcode);
	}
}
