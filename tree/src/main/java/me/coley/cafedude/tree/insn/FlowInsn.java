package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Label;

/**
 * Instructions for flow control instructions with a single label operand.
 * Includes the following instructions:
 * <ul>
 *     <li>{@link Opcodes#IFNULL}</li>
 *     <li>{@link Opcodes#IFNONNULL}</li>
 *     <li>{@link Opcodes#IFEQ}</li>
 *     <li>{@link Opcodes#IFNE}</li>
 *     <li>{@link Opcodes#IFLT}</li>
 *     <li>{@link Opcodes#IFGE}</li>
 *     <li>{@link Opcodes#IFGT}</li>
 *     <li>{@link Opcodes#IFLE}</li>
 *     <li>{@link Opcodes#IF_ICMPEQ}</li>
 *     <li>{@link Opcodes#IF_ICMPNE}</li>
 *     <li>{@link Opcodes#IF_ICMPLT}</li>
 *     <li>{@link Opcodes#IF_ICMPGE}</li>
 *     <li>{@link Opcodes#IF_ICMPGT}</li>
 *     <li>{@link Opcodes#IF_ICMPLE}</li>
 *     <li>{@link Opcodes#IF_ACMPEQ}</li>
 *     <li>{@link Opcodes#IF_ACMPNE}</li>
 *     <li>{@link Opcodes#GOTO}</li>
 *     <li>{@link Opcodes#JSR}</li>
 * </ul>
 */
public class FlowInsn extends Insn {

	private Label label;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param label
	 * 		Label of the instruction.
	 */
	public FlowInsn(int opcode, Label label) {
		super(InsnKind.FLOW, opcode);
		this.label = label;
	}

	/**
	 * @return Label of the instruction.
	 */
	public Label getLabel() {
		return label;
	}

	/**
	 * @param label
	 * 		Label of the instruction.
	 */
	public void setLabel(Label label) {
		this.label = label;
	}

	@Override
	public int size() {
		// u1 opcode
		// u2 label offset
		return 3;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + label.getOffset() + ")";
	}
}
