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

	protected MethodInsn(int opcode, String owner, String name, String descriptor) {
		super(opcode);
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

}
