package me.coley.cafedude.instruction;

/**
 * Wide instruction
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
