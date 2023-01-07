package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction for the {@link Opcodes#MULTIANEWARRAY} instruction,
 * which has a class reference and a dimension count operand.
 * @see Opcodes#MULTIANEWARRAY
 */
public class MultianewarrayInsn extends Insn {

	private String owner;

	private int dimensions;

	/**
	 * @param owner
	 * 		Owner of the array.
	 * @param dimensions
	 * 		Number of dimensions of the array.
	 */
	public MultianewarrayInsn(String owner, int dimensions) {
		super(Opcodes.MULTIANEWARRAY);
		this.owner = owner;
		this.dimensions = dimensions;
	}

	/**
	 * @return Owner of the array.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 * 		Owner of the array.
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return Number of dimensions of the array.
	 */
	public int getDimensions() {
		return dimensions;
	}

	/**
	 * @param dimensions
	 * 		Number of dimensions of the array.
	 */
	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}

}
