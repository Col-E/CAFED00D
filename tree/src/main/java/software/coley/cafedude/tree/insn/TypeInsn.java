package software.coley.cafedude.tree.insn;

import software.coley.cafedude.classfile.Descriptor;
import software.coley.cafedude.classfile.instruction.Opcodes;

import javax.annotation.Nonnull;

/**
 * Instruction which has type descriptor operand.
 * Instructions that use this is:
 * <ul>
 *     <li>{@link Opcodes#NEW}</li>
 *     <li>{@link Opcodes#ANEWARRAY}</li>
 *     <li>{@link Opcodes#CHECKCAST}</li>
 *     <li>{@link Opcodes#INSTANCEOF}</li>
 * </ul>
 *
 * @author Justus Garbe
 */
public class TypeInsn extends Insn {
	private Descriptor descriptor;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param descriptor
	 * 		Descriptor of the type.
	 */
	public TypeInsn(int opcode, @Nonnull Descriptor descriptor) {
		super(InsnKind.TYPE, opcode);
		this.descriptor = descriptor;
	}

	/**
	 * @return Descriptor of the type.
	 */
	@Nonnull
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		Descriptor of the type.
	 */
	public void setDescriptor(@Nonnull Descriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public int size() {
		// u1 opcode
		// u2 descriptor
		return 3;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + descriptor + ")";
	}
}
