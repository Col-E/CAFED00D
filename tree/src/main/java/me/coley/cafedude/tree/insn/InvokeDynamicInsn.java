package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.Handle;

import java.util.List;

/**
 * Instruction for the invoke-dynamic opcode which contains a handle to the bootstrap method,
 * the name and type of the method to be dynamically invoked, and zero or more extra arguments
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.invokedynamic">InvokeDynamic</a>
 */
public class InvokeDynamicInsn extends Insn {

	private String name;
	private Descriptor descriptor;
	private Handle bootstrapMethod;
	private List<Constant> bootstrapArguments;

	/**
	 * @param name
	 * 		Name of the method.
	 * @param descriptor
	 * 		Descriptor of the method.
	 * @param bootstrapMethod
	 * 		Handle to the bootstrap method.
	 * @param bootstrapArguments
	 * 		Zero or more extra arguments for the bootstrap method.
	 */
	public InvokeDynamicInsn(String name, Descriptor descriptor, Handle bootstrapMethod,
							 List<Constant> bootstrapArguments) {
		super(InsnKind.INVOKE_DYNAMIC, Opcodes.INVOKEDYNAMIC);
		this.name = name;
		this.descriptor = descriptor;
		this.bootstrapMethod = bootstrapMethod;
		this.bootstrapArguments = bootstrapArguments;
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
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		Descriptor of the method.
	 */
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * @return Handle to the bootstrap method.
	 */
	public Handle getBootstrapMethod() {
		return bootstrapMethod;
	}

	/**
	 * @param bootstrapMethod
	 * 		Handle to the bootstrap method.
	 */
	public void setBootstrapMethod(Handle bootstrapMethod) {
		this.bootstrapMethod = bootstrapMethod;
	}

	/**
	 * @return Zero or more arguments for the bootstrap method.
	 */
	public List<Constant> getBootstrapArguments() {
		return bootstrapArguments;
	}

	/**
	 * @param bootstrapArguments
	 * 		Zero or more extra arguments for the bootstrap method.
	 */
	public void setBootstrapArguments(List<Constant> bootstrapArguments) {
		this.bootstrapArguments = bootstrapArguments;
	}

	@Override
	public int size() {
		// u1 opcode
		// u2 bootstrapMethodAttrIndex
		// u2 nameAndTypeIndex
		// bootstrapsArguments are detached from this instruction, because they come from the
		// BootstrapMethods attribute
		return 5;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("invokedynamic(").append('\n');
		sb.append("  ").append(bootstrapMethod).append('\n');
		sb.append("  ").append(name).append(' ').append(descriptor.getDescriptor()).append('\n');
		for (Constant constant : bootstrapArguments) {
			sb.append("  ").append(constant).append('\n');
		}
		sb.append(')');
		return sb.toString();
	}
}
