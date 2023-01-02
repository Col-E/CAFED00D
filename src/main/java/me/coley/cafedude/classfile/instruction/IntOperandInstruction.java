package me.coley.cafedude.classfile.instruction;

import me.coley.cafedude.classfile.behavior.CpAccessor;

import java.util.Collections;
import java.util.Set;

/**
 * Instruction with a single int operand.
 *
 * @author xDark
 */
public class IntOperandInstruction extends BasicInstruction implements CpAccessor {
	private int operand;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param operand
	 * 		Instruction operand.
	 */
	public IntOperandInstruction(int opcode, int operand) {
		super(opcode);
		this.operand = operand;
	}

	/**
	 * @return instruction operand.
	 */
	public int getOperand() {
		return operand;
	}

	/**
	 * Sets instruction operand.
	 *
	 * @param operand
	 * 		New operand.
	 */
	public void setOperand(int operand) {
		this.operand = operand;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IntOperandInstruction)) return false;
		if (!super.equals(o)) return false;

		IntOperandInstruction that = (IntOperandInstruction) o;

		return operand == that.operand;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + operand;
		return result;
	}

	@Override
	public int computeSize() {
		switch (getOpcode()) {
			case Opcodes.RET:
			case Opcodes.BIPUSH:
			case Opcodes.ALOAD:
			case Opcodes.ASTORE:
			case Opcodes.DLOAD:
			case Opcodes.DSTORE:
			case Opcodes.FLOAD:
			case Opcodes.FSTORE:
			case Opcodes.ILOAD:
			case Opcodes.ISTORE:
			case Opcodes.LLOAD:
			case Opcodes.LSTORE:
			case Opcodes.LDC:
			case Opcodes.NEWARRAY:
				return 2; // opcode + operand (1 byte)
			default:
				return 3; // opcode + operand (2 bytes)
		}
	}

	@Override
	public String toString() {
		return "insn(" + getOpcode() + ": " + operand + ")";
	}

	@Override
	public Set<Integer> cpAccesses() {
		switch (getOpcode()) {
			case Opcodes.GETFIELD:
			case Opcodes.GETSTATIC:
			case Opcodes.PUTFIELD:
			case Opcodes.PUTSTATIC:
			case Opcodes.INVOKEVIRTUAL:
			case Opcodes.INVOKESPECIAL:
			case Opcodes.INVOKESTATIC:
			case Opcodes.INVOKEINTERFACE:
			case Opcodes.LDC:
			case Opcodes.LDC_W:
			case Opcodes.LDC2_W:
			case Opcodes.NEW:
			case Opcodes.ANEWARRAY:
			case Opcodes.CHECKCAST:
			case Opcodes.INSTANCEOF:
			case Opcodes.MULTIANEWARRAY:
			case Opcodes.INVOKEDYNAMIC:
				return Collections.singleton(operand);
			default:
				return Collections.emptySet();
		}
	}
}
