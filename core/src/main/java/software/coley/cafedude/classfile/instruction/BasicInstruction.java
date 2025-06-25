package software.coley.cafedude.classfile.instruction;

/**
 * Instruction that does not have any operands.
 *
 * @author xDark
 */
public non-sealed class BasicInstruction extends Instruction {
	/**
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public BasicInstruction(int opcode) {
		super(opcode);
	}
}
