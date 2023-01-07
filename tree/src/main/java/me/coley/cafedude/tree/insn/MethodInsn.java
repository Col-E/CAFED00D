package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Instruction which has a method descriptor operand.
 * Instructions that use this is:
 * <ul>
 *     <li>{@link Opcodes#INVOKEVIRTUAL}</li>
 *     <li>{@link Opcodes#INVOKESPECIAL}</li>
 *     <li>{@link Opcodes#INVOKESTATIC}</li>
 *     <li>{@link Opcodes#INVOKEINTERFACE}</li>
 * </ul>
 * @see me.coley.cafedude.tree.insn.InvokeDynamicInsn
 */
public class MethodInsn extends Insn {

	private String owner;
	private String name;
	private String descriptor;

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
	public MethodInsn(int opcode, String owner, String name, String descriptor) {
		super(opcode);
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	/**
	 * @return Owner of the method.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 * 		Owner of the method.
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return Name of the method.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * 		Name of the method.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Descriptor of the method.
	 */
	public String getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		Descriptor of the method.
	 */
	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

}
