package software.coley.cafedude.tree.insn;

import software.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction which has a signed integer operand.
 * Instructions that use this is:
 * <ul>
 *     <li>{@link Opcodes#BIPUSH}</li>
 *     <li>{@link Opcodes#SIPUSH}</li>
 *     <li>{@link Opcodes#NEWARRAY}</li>
 * </ul>
 *
 * @author Justus Garbe
 */
public class IntInsn extends Insn {
	protected int operand;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param operand
	 * 		Operand value.
	 */
	public IntInsn(int opcode, int operand) {
		super(InsnKind.INT, opcode);
		this.operand = operand;
	}

	/**
	 * @return Operand value.
	 */
	public int getOperand() {
		return operand;
	}

	/**
	 * @param operand
	 * 		Operand value.
	 */
	public void setOperand(int operand) {
		this.operand = operand;
	}

	@Override
	public int size() {
		// u1 opcode
		// s1 operand
		return 2;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + operand + ")";
	}
}
