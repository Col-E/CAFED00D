package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction for the {@link Opcodes#MULTIANEWARRAY} instruction, which has a class reference and a dimension count operand.
 * @see Opcodes#MULTIANEWARRAY
 */
public class MultianewarrayInsn extends Insn {

	private String owner;

	private int dimensions;

	protected MultianewarrayInsn(int opcode, String owner, int dimensions) {
		super(opcode);
		this.owner = owner;
		this.dimensions = dimensions;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public int getDimensions() {
		return dimensions;
	}

	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}

}
