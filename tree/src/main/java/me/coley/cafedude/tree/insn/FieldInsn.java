package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction which has a field descriptor operand.
 * Instructions that use this is:
 * <ul>
 *     <li>{@link Opcodes#GETSTATIC}</li>
 *     <li>{@link Opcodes#PUTSTATIC}</li>
 *     <li>{@link Opcodes#GETFIELD}</li>
 *     <li>{@link Opcodes#PUTFIELD}</li>
 * </ul>
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
	public FieldInsn(int opcode, String owner, String name, Descriptor descriptor) {
		super(opcode);
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	/**
	 * @return Owner of the field.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 * 		Owner of the field.
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return Name of the field.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * 		Name of the field.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Descriptor of the field.
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		Descriptor of the field.
	 */
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}

}
