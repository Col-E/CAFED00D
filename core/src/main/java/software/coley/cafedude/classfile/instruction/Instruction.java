package software.coley.cafedude.classfile.instruction;

/**
 * Node that represents bytecode instruction.
 *
 * @author xDark
 */
public sealed abstract class Instruction permits BasicInstruction, CpRefInstruction, IincInstruction,
		IntOperandInstruction, LookupSwitchInstruction, MultiANewArrayInstruction, TableSwitchInstruction, WideInstruction {
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

	/**
	 * @return Size of this instruction.
	 */
	public int computeSize() {
		return 1; // opcode
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Instruction that)) return false;

		return opcode == that.opcode;
	}

	@Override
	public int hashCode() {
		return opcode;
	}

	@Override
	public String toString() {
		return OpcodeNames.name(getOpcode());
	}
}
