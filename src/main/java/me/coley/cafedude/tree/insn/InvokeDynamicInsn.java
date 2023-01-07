package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.Handle;

/**
 * Instruction for the invoke-dynamic opcode which contains a handle to the bootstrap method,
 * the name and type of the method to be dynamically invoked, and zero or more extra arguments
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.invokedynamic">InvokeDynamic</a>
 */
public class InvokeDynamicInsn extends Insn {

	private String name;
	private Descriptor descriptor;
	private Handle bootstrapMethod;
	private Constant[] bootstrapArguments;

	protected InvokeDynamicInsn(String name, Descriptor descriptor, Handle bootstrapMethod, Constant[] bootstrapArguments) {
		super(Opcodes.INVOKEDYNAMIC);
		this.name = name;
		this.descriptor = descriptor;
		this.bootstrapMethod = bootstrapMethod;
		this.bootstrapArguments = bootstrapArguments;
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

	public Handle getBootstrapMethod() {
		return bootstrapMethod;
	}

	public void setBootstrapMethod(Handle bootstrapMethod) {
		this.bootstrapMethod = bootstrapMethod;
	}

	public Constant[] getBootstrapArguments() {
		return bootstrapArguments;
	}

	public void setBootstrapArguments(Constant[] bootstrapArguments) {
		this.bootstrapArguments = bootstrapArguments;
	}
}
