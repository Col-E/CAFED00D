package me.coley.cafedude.classfile.instruction;

import me.coley.cafedude.classfile.behavior.CpAccessor;

import java.util.Collections;
import java.util.Set;

/**
 * Instruction with two int operands.
 *
 * @author xDark
 */
public class BiIntOperandInstruction extends BasicInstruction implements CpAccessor {
	private int firstOperand;
	private int secondOperand;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param firstOperand
	 * 		First instruction operand.
	 * @param secondOperand
	 * 		Second instruction operand.
	 */
	public BiIntOperandInstruction(int opcode, int firstOperand, int secondOperand) {
		super(opcode);
		this.firstOperand = firstOperand;
		this.secondOperand = secondOperand;
	}

	/**
	 * @return first instruction operand.
	 */
	public int getFirstOperand() {
		return firstOperand;
	}

	/**
	 * Sets the first instruction operand.
	 *
	 * @param firstOperand
	 * 		New operand.
	 */
	public void setFirstOperand(int firstOperand) {
		this.firstOperand = firstOperand;
	}

	/**
	 * @return first instruction operand.
	 */
	public int getSecondOperand() {
		return secondOperand;
	}

	/**
	 * Sets the second instruction operand.
	 *
	 * @param secondOperand
	 * 		New operand.
	 */
	public void setSecondOperand(int secondOperand) {
		this.secondOperand = secondOperand;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BiIntOperandInstruction)) return false;
		if (!super.equals(o)) return false;

		BiIntOperandInstruction that = (BiIntOperandInstruction) o;

		if (firstOperand != that.firstOperand) return false;
		return secondOperand == that.secondOperand;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + firstOperand;
		result = 31 * result + secondOperand;
		return result;
	}

	@Override
	public int computeSize() {
		if(getOpcode() == Opcodes.MULTIANEWARRAY) {
			return 1 + 2 + 1; // opcode + index (short) + dimensions (byte)
		} else if(getOpcode() == Opcodes.IINC) {
			return 1 + 1 + 1; // opcode + index (byte) + constant (byte)
		} else {
			return -1;
		}
	}

	@Override
	public String toString() {
		return "insn(" + getOpcode() + ": " + firstOperand + ", " + secondOperand + ")";
	}

	@Override
	public Set<Integer> cpAccesses() {
		if(getOpcode() == Opcodes.MULTIANEWARRAY) {
			return Collections.singleton(firstOperand);
		}
		return Collections.emptySet();
	}
}
