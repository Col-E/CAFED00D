package me.coley.cafedude.instruction;

/**
 * Instruction with two int operands.
 *
 * @author xDark
 */
public class BiIntOperandInstruction extends BasicInstruction {

	private int firstOperand;
	private int secondOperand;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param firstOperand
	 * 		First instruction operand.
	 * @param secondOperand
	 * 		Second instruction operand.
	 */
	public BiIntOperandInstruction(int opcode, int firstOperand, int secondOperand) {
		super(opcode);
		this.firstOperand = firstOperand;
		this.secondOperand = secondOperand;
	}

	/**
	 * @return first instruction operand.
	 */
	public int getFirstOperand() {
		return firstOperand;
	}

	/**
	 * Sets the first instruction operand.
	 *
	 * @param firstOperand
	 * 		New operand.
	 */
	public void setFirstOperand(int firstOperand) {
		this.firstOperand = firstOperand;
	}

	/**
	 * @return first instruction operand.
	 */
	public int getSecondOperand() {
		return secondOperand;
	}

	/**
	 * Sets the second instruction operand.
	 *
	 * @param secondOperand
	 * 		New operand.
	 */
	public void setSecondOperand(int secondOperand) {
		this.secondOperand = secondOperand;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BiIntOperandInstruction)) return false;
		if (!super.equals(o)) return false;

		BiIntOperandInstruction that = (BiIntOperandInstruction) o;

		if (firstOperand != that.firstOperand) return false;
		return secondOperand == that.secondOperand;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + firstOperand;
		result = 31 * result + secondOperand;
		return result;
	}

	@Override
	public String toString() {
		return "insn(" + getOpcode() + ": " + firstOperand + ", " + secondOperand + ")";
	}
}
