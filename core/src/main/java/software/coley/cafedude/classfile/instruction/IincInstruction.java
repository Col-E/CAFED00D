package software.coley.cafedude.classfile.instruction;

/**
 * Instruction for the {@code iinc} opcode.
 *
 * @author xDark
 */
public non-sealed class IincInstruction extends Instruction {
	private int var;
	private int increment;

	/**
	 * @param var
	 * 		First instruction operand.
	 * @param increment
	 * 		Second instruction operand.
	 */
	public IincInstruction(int var, int increment) {
		super(Opcodes.IINC);
		this.var = var;
		this.increment = increment;
	}

	/**
	 * @return first instruction operand.
	 */
	public int getVar() {
		return var;
	}

	/**
	 * Sets the first instruction operand.
	 *
	 * @param var
	 * 		New operand.
	 */
	public void setVar(int var) {
		this.var = var;
	}

	/**
	 * @return first instruction operand.
	 */
	public int getIncrement() {
		return increment;
	}

	/**
	 * Sets the second instruction operand.
	 *
	 * @param increment
	 * 		New operand.
	 */
	public void setIncrement(int increment) {
		this.increment = increment;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IincInstruction)) return false;
		if (!super.equals(o)) return false;

		IincInstruction that = (IincInstruction) o;

		if (var != that.var) return false;
		return increment == that.increment;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + var;
		result = 31 * result + increment;
		return result;
	}

	@Override
	public int computeSize() {
		return 3; // opcode + var + increment
	}

	@Override
	public String toString() {
		return "insn(" + OpcodeNames.name(getOpcode()) + ": " + var + ", " + increment + ")";
	}

}
