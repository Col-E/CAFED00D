package software.coley.cafedude.classfile.instruction;

/**
 * Instruction with a single int operand.
 *
 * @author xDark
 */
public non-sealed class IntOperandInstruction extends Instruction {
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
	public int computeSize() {
		switch (getOpcode()) {
			case Opcodes.RET:
			case Opcodes.BIPUSH:
			case Opcodes.ALOAD:
			case Opcodes.ASTORE:
			case Opcodes.DLOAD:
			case Opcodes.DSTORE:
			case Opcodes.FLOAD:
			case Opcodes.FSTORE:
			case Opcodes.ILOAD:
			case Opcodes.ISTORE:
			case Opcodes.LLOAD:
			case Opcodes.LSTORE:
			case Opcodes.NEWARRAY:
				return 2; // opcode + operand (1 byte)
			case Opcodes.GOTO_W:
			case Opcodes.JSR_W:
				return 5; // opcode + operand (4 bytes)
			default:
				return 3; // opcode + operand (2 bytes)
		}
	}

	@Override
	public String toString() {
		return super.toString() + " operand=" + operand;
	}

}
