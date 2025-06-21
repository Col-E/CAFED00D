package software.coley.cafedude.tree.insn;

import software.coley.cafedude.classfile.Descriptor;
import software.coley.cafedude.classfile.instruction.Opcodes;

import jakarta.annotation.Nonnull;

/**
 * Instruction which has a field descriptor operand.
 * Instructions that use this is:
 * <ul>
 *     <li>{@link Opcodes#GETSTATIC}</li>
 *     <li>{@link Opcodes#PUTSTATIC}</li>
 *     <li>{@link Opcodes#GETFIELD}</li>
 *     <li>{@link Opcodes#PUTFIELD}</li>
 * </ul>
 *
 * @author Justus Garbe
 */
public class FieldInsn extends Insn {
	private String owner;
	private String name;
	private Descriptor descriptor;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param owner
	 * 		Owner of the field.
	 * @param name
	 * 		Name of the field.
	 * @param descriptor
	 * 		Descriptor of the field.
	 */
	public FieldInsn(int opcode, @Nonnull String owner, @Nonnull String name, @Nonnull Descriptor descriptor) {
		super(InsnKind.FIELD, opcode);
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	/**
	 * @return Owner of the field.
	 */
	@Nonnull
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 * 		Owner of the field.
	 */
	public void setOwner(@Nonnull String owner) {
		this.owner = owner;
	}

	/**
	 * @return Name of the field.
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * 		Name of the field.
	 */
	public void setName(@Nonnull String name) {
		this.name = name;
	}

	/**
	 * @return Descriptor of the field.
	 */
	@Nonnull
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		Descriptor of the field.
	 */
	public void setDescriptor(@Nonnull Descriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public int size() {
		// u1 opcode
		// u2 index
		return 3;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + owner + "." + name + descriptor.getDescriptor() + ")";
	}
}
