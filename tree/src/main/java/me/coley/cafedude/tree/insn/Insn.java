package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Ungrouped instruction. Instructions that use this are:
 * <ul>
 *     <li>{@link Opcodes#NOP}</li>
 *     <li>{@link Opcodes#MONITORENTER}</li>
 *     <li>{@link Opcodes#MONITOREXIT}</li>
 *     <li>{@link Opcodes#ATHROW}</li>
 * </ul>
 */
public class Insn {

	private final int opcode;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public Insn(int opcode) {
		this.opcode = opcode;
	}

	@Override
	public String toString() {
		return "insn(" + opcode + ")";
	}
}
