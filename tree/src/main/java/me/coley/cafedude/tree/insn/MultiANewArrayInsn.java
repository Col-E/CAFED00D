package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

import javax.annotation.Nonnull;

/**
 * Instruction for the {@link Opcodes#MULTIANEWARRAY} instruction,
 * which has a class reference and a dimension count operand.
 *
 * @author Justus Garbe
 * @see Opcodes#MULTIANEWARRAY
 */
public class MultiANewArrayInsn extends Insn {
	private String owner;
	private int dimensions;

	/**
	 * @param owner
	 * 		Owner of the array.
	 * @param dimensions
	 * 		Number of dimensions of the array.
	 */
	public MultiANewArrayInsn(@Nonnull String owner, int dimensions) {
		super(InsnKind.MULTI_ANEWARRAY, Opcodes.MULTIANEWARRAY);
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

	@Override
	public int size() {
		// u1 opcode
		// u2 index
		// u1 dimensions
		return 4;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + owner + ", " + dimensions + ")";
	}
}
