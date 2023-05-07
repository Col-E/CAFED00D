package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction for the {@link Opcodes#IINC} instruction,
 * which has a local variable index and an increment integer operand.
 *
 * @author Justus Garbe
 * @see Opcodes#IINC
 */
public class IIncInsn extends Insn {
	private int index;
	private int increment;

	/**
	 * @param index
	 * 		Local variable index.
	 * @param increment
	 * 		Increment value.
	 */
	public IIncInsn(int index, int increment) {
		super(InsnKind.IINC, Opcodes.IINC);
		this.index = index;
		this.increment = increment;
	}

	/**
	 * @return Local variable index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 * 		Local variable index.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return Increment value.
	 */
	public int getIncrement() {
		return increment;
	}

	/**
	 * @param increment
	 * 		Increment value.
	 */
	public void setIncrement(int increment) {
		this.increment = increment;
	}

	@Override
	public int size() {
		// u1 opcode
		// u1 index
		// u1 increment
		return 3;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + index + ", " + increment + ")";
	}
}
