package me.coley.cafedude.instruction;

/**
 * Instruction with a single int operand.
 *
 * @author xDark
 */
public class IntOperandInstruction extends BasicInstruction {

	private int operand;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param operand
	 * 		Instruction operand.
	 */
	public IntOperandInstruction(int opcode, int operand) {
		super(opcode);
		this.operand = operand;
	}

	/**
	 * @return instruction operand.
	 */
	public int getOperand() {
		return operand;
	}

	/**
	 * Sets instruction operand.
	 *
	 * @param operand
	 * 		New operand.
	 */
	public void setOperand(int operand) {
		this.operand = operand;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IntOperandInstruction)) return false;
		if (!super.equals(o)) return false;

		IntOperandInstruction that = (IntOperandInstruction) o;

		return operand == that.operand;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + operand;
		return result;
	}

	@Override
	public String toString() {
		return "insn(" + getOpcode() + ": " + operand + ")";
	}
}
