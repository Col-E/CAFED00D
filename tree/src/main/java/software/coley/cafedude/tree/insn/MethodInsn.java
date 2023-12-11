package software.coley.cafedude.tree.insn;

import software.coley.cafedude.classfile.Descriptor;
import software.coley.cafedude.classfile.instruction.Opcodes;

import javax.annotation.Nonnull;

/**
 * Instruction which has a method descriptor operand.
 * Instructions that use this is:
 * <ul>
 *     <li>{@link Opcodes#INVOKEVIRTUAL}</li>
 *     <li>{@link Opcodes#INVOKESPECIAL}</li>
 *     <li>{@link Opcodes#INVOKESTATIC}</li>
 *     <li>{@link Opcodes#INVOKEINTERFACE}</li>
 * </ul>
 *
 * @author Justus Garbe
 * @see InvokeDynamicInsn
 */
public class MethodInsn extends Insn {
	private String owner;
	private String name;
	private Descriptor descriptor;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param owner
	 * 		Owner of the method.
	 * @param name
	 * 		Name of the method.
	 * @param descriptor
	 * 		Descriptor of the method.
	 */
	public MethodInsn(int opcode, @Nonnull String owner, @Nonnull String name, @Nonnull Descriptor descriptor) {
		super(InsnKind.METHOD, opcode);
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	/**
	 * @return Owner of the method.
	 */
	@Nonnull
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 * 		Owner of the method.
	 */
	public void setOwner(@Nonnull String owner) {
		this.owner = owner;
	}

	/**
	 * @return Name of the method.
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * 		Name of the method.
	 */
	public void setName(@Nonnull String name) {
		this.name = name;
	}

	/**
	 * @return Descriptor of the method.
	 */
	@Nonnull
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		Descriptor of the method.
	 */
	public void setDescriptor(@Nonnull Descriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public int size() {
		// u1 opcode
		// u2 index
		if (getOpcode() == Opcodes.INVOKEINTERFACE){
			// u1 count
			// u1 padding
			return 5;
		}
		return 3;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + owner + "." + name + descriptor.getDescriptor() + ")";
	}
}
