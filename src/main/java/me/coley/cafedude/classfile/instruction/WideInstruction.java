package me.coley.cafedude.classfile.instruction;

/**
 * Wide instruction
 *
 * @author xDark
 */
public class WideInstruction extends BasicInstruction {
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
		switch (backing.getOpcode()) {
			case Opcodes.IINC:
				return 6;
			case Opcodes.LLOAD:
			case Opcodes.DLOAD:
			case Opcodes.LSTORE:
			case Opcodes.DSTORE:
				return 4;
			default:
				return 3;
		}
	}
}
