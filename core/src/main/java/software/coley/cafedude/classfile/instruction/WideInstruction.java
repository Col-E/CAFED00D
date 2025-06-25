package software.coley.cafedude.classfile.instruction;

/**
 * Wide instruction
 *
 * @author xDark
 */
public non-sealed class WideInstruction extends Instruction {
	private final Instruction backing;

	/**
	 * @param backing
	 * 		Backing instruction.
	 */
	public WideInstruction(Instruction backing) {
		super(Opcodes.WIDE);
		this.backing = backing;
	}

	/**
	 * @return Backing instruction.
	 */
	public Instruction getBacking() {
		return backing;
	}

	@Override
	public int computeSize() {
		if (backing.getOpcode() == Opcodes.IINC) {
			// IINC is unique in that it becomes 6 bytes:
			//   https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-6.html#jvms-6.5.wide
			//
			// opcode
			// iinc
			// indexbyte1
			// indexbyte2
			// constbyte1
			// constbyte2
			return 6;
		}
		// opcode
		// input opcode
		// indexbyte1
		// indexbyte2
		return 4;
	}
}
