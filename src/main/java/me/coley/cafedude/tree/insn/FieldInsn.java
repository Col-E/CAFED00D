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

	protected FieldInsn(int opcode, String owner, String name, Descriptor descriptor) {
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

	public Descriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}

}
