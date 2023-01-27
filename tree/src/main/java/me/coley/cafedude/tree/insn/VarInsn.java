package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.visitor.reader.CodeReader;
import me.coley.cafedude.tree.visitor.writer.CodeWriter;

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
 * {@link CodeReader} will convert all XLOAD_N and XSTORE_N instructions to XLOAD and XSTORE with respective
 * operand values. <br>
 * {@link CodeWriter} will convert them to XLOAD_N and XSTORE_N instructions if possible.
 * @see Opcodes#ILOAD_0
 * @see Opcodes#ISTORE_0
 */
public class VarInsn extends Insn {

	private int index;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param index
	 * 		Local variable index.
	 */
	public VarInsn(int opcode, int index) {
		super(InsnKind.VAR, opcode);
		this.index = index;
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

	@Override
	public int size() {
		// u1 opcode
		// u1 index
		return 2;
	}
}
