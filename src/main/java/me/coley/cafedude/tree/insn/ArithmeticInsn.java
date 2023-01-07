package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction for the arithmetic instructions.
 * Instructions include:
 * <ul>
 *     <li>{@link Opcodes#IADD}</li>
 *     <li>{@link Opcodes#LADD}</li>
 *     <li>{@link Opcodes#FADD}</li>
 *     <li>{@link Opcodes#DADD}</li>
 *     <li>{@link Opcodes#ISUB}</li>
 *     <li>{@link Opcodes#LSUB}</li>
 *     <li>{@link Opcodes#FSUB}</li>
 *     <li>{@link Opcodes#DSUB}</li>
 *     <li>{@link Opcodes#IMUL}</li>
 *     <li>{@link Opcodes#LMUL}</li>
 *     <li>{@link Opcodes#FMUL}</li>
 *     <li>{@link Opcodes#DMUL}</li>
 *     <li>{@link Opcodes#IDIV}</li>
 *     <li>{@link Opcodes#LDIV}</li>
 *     <li>{@link Opcodes#FDIV}</li>
 *     <li>{@link Opcodes#DDIV}</li>
 *     <li>{@link Opcodes#IREM}</li>
 *     <li>{@link Opcodes#LREM}</li>
 *     <li>{@link Opcodes#FREM}</li>
 *     <li>{@link Opcodes#DREM}</li>
 *     <li>{@link Opcodes#INEG}</li>
 *     <li>{@link Opcodes#LNEG}</li>
 *     <li>{@link Opcodes#FNEG}</li>
 *     <li>{@link Opcodes#DNEG}</li>
 *     <li>{@link Opcodes#ISHL}</li>
 *     <li>{@link Opcodes#LSHL}</li>
 *     <li>{@link Opcodes#ISHR}</li>
 *     <li>{@link Opcodes#LSHR}</li>
 *     <li>{@link Opcodes#IUSHR}</li>
 *     <li>{@link Opcodes#LUSHR}</li>
 *     <li>{@link Opcodes#IAND}</li>
 *     <li>{@link Opcodes#LAND}</li>
 *     <li>{@link Opcodes#IOR}</li>
 *     <li>{@link Opcodes#LOR}</li>
 *     <li>{@link Opcodes#IXOR}</li>
 *     <li>{@link Opcodes#LXOR}</li>
 *     <li>{@link Opcodes#I2L}</li>
 *     <li>{@link Opcodes#I2F}</li>
 *     <li>{@link Opcodes#I2D}</li>
 *     <li>{@link Opcodes#L2I}</li>
 *     <li>{@link Opcodes#L2F}</li>
 *     <li>{@link Opcodes#L2D}</li>
 *     <li>{@link Opcodes#F2I}</li>
 *     <li>{@link Opcodes#F2L}</li>
 *	   <li>{@link Opcodes#F2D}</li>
 *	   <li>{@link Opcodes#D2I}</li>
 *	   <li>{@link Opcodes#D2L}</li>
 *	   <li>{@link Opcodes#D2F}</li>
 *	   <li>{@link Opcodes#I2B}</li>
 *	   <li>{@link Opcodes#I2C}</li>
 *	   <li>{@link Opcodes#I2S}</li>
 *	   <li>{@link Opcodes#LCMP}</li>
 *	   <li>{@link Opcodes#FCMPL}</li>
 *	   <li>{@link Opcodes#FCMPG}</li>
 *	   <li>{@link Opcodes#DCMPL}</li>
 *	   <li>{@link Opcodes#DCMPG}</li>
 * </ul>
 */
public class ArithmeticInsn extends Insn {

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public ArithmeticInsn(int opcode) {
		super(opcode);
	}

}
