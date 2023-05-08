package me.coley.cafedude.classfile.instruction;

/**
 * Instruction that does not have any operands.
 *
 * @author xDark
 */
public class BasicInstruction extends Instruction {
	/**
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public BasicInstruction(int opcode) {
		super(opcode);
	}

	@Override
	public String toString() {
		return "insn(" + OpcodeNames.name(getOpcode()) + ")";
	}
}
