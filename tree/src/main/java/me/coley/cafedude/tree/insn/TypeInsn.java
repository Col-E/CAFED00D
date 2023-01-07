package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction which has type descriptor operand.
 * Instructions that use this is:
 * <ul>
 *     <li>{@link Opcodes#NEW}</li>
 *     <li>{@link Opcodes#ANEWARRAY}</li>
 *     <li>{@link Opcodes#CHECKCAST}</li>
 *     <li>{@link Opcodes#INSTANCEOF}</li>
 * </ul>
 */
public class TypeInsn extends Insn {

	private Descriptor descriptor;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param descriptor
	 * 		Descriptor of the type.
	 */
	public TypeInsn(int opcode, Descriptor descriptor) {
		super(opcode);
		this.descriptor = descriptor;
	}

	/**
	 * @return Descriptor of the type.
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		Descriptor of the type.
	 */
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}

}
