package me.coley.cafedude.instruction;

/**
 * Instruction with a single string operand.
 *
 * @author xDark
 */
public class StringOperandInstruction extends BasicInstruction {

	private String operand;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param operand
	 * 		Instruction operand.
	 */
	public StringOperandInstruction(int opcode, String operand) {
		super(opcode);
		this.operand = operand;
	}

	/**
	 * @return instruction operand.
	 */
	public String getOperand() {
		return operand;
	}

	/**
	 * Sets instruction operand.
	 *
	 * @param operand
	 * 		New operand.
	 */
	public void setOperand(String operand) {
		this.operand = operand;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StringOperandInstruction)) return false;
		if (!super.equals(o)) return false;

		StringOperandInstruction that = (StringOperandInstruction) o;

		return operand.equals(that.operand);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + operand.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "insn(" + getOpcode() + ", " + operand + ')';
	}
}
