package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction which has an operand that is a local variable index, to either store or load from.
 * Instructions that use this is:
 * <ul>
 *     <li>{@link Opcodes#ILOAD}</li>
 *     <li>{@link Opcodes#LLOAD}</li>
 *     <li>{@link Opcodes#FLOAD}</li>
 *     <li>{@link Opcodes#DLOAD}</li>
 *     <li>{@link Opcodes#ALOAD}</li>
 *     <li>{@link Opcodes#ISTORE}</li>
 *     <li>{@link Opcodes#LSTORE}</li>
 *     <li>{@link Opcodes#FSTORE}</li>
 *     <li>{@link Opcodes#DSTORE}</li>
 *     <li>{@link Opcodes#ASTORE}</li>
 * </ul>
 * And all XLOAD_N and XSTORE_N instructions.
 * @see Opcodes#ILOAD_0
 * @see Opcodes#ISTORE_0
 */
public class VarInsn extends Insn {

	private int index;

	protected VarInsn(int opcode, int index) {
		super(opcode);
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
