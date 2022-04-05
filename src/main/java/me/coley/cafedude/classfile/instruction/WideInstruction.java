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
}
