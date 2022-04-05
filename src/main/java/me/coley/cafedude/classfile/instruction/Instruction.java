package me.coley.cafedude.classfile.instruction;

/**
 * Node that represents bytecode instruction.
 *
 * @author xDark
 */
public abstract class Instruction {

	private int opcode;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 */
	protected Instruction(int opcode) {
		this.opcode = opcode;
	}

	/**
	 * @return instruction opcode.
	 */
	public int getOpcode() {
		return opcode;
	}

	/**
	 * Sets instruction opcode.
	 *
	 * @param opcode
	 * 		New opcode.
	 */
	public void setOpcode(int opcode) {
		this.opcode = opcode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Instruction)) return false;

		Instruction that = (Instruction) o;

		return opcode == that.opcode;
	}

	@Override
	public int hashCode() {
		return opcode;
	}
}
