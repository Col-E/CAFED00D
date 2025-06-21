package software.coley.cafedude.tree.insn;

import software.coley.cafedude.classfile.Descriptor;
import software.coley.cafedude.classfile.instruction.Opcodes;
import software.coley.cafedude.tree.Constant;
import software.coley.cafedude.tree.Handle;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * Instruction for the invoke-dynamic opcode which contains a handle to the bootstrap method,
 * the name and type of the method to be dynamically invoked, and zero or more extra arguments
 *
 * @author Justus Garbe
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
	public InvokeDynamicInsn(@Nonnull String name, @Nonnull Descriptor descriptor, @Nonnull Handle bootstrapMethod,
							 @Nonnull List<Constant> bootstrapArguments) {
		super(InsnKind.INVOKE_DYNAMIC, Opcodes.INVOKEDYNAMIC);
		this.name = name;
		this.descriptor = descriptor;
		this.bootstrapMethod = bootstrapMethod;
		this.bootstrapArguments = bootstrapArguments;
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

	/**
	 * @return Handle to the bootstrap method.
	 */
	@Nonnull
	public Handle getBootstrapMethod() {
		return bootstrapMethod;
	}

	/**
	 * @param bootstrapMethod
	 * 		Handle to the bootstrap method.
	 */
	public void setBootstrapMethod(@Nonnull Handle bootstrapMethod) {
		this.bootstrapMethod = bootstrapMethod;
	}

	/**
	 * @return Zero or more arguments for the bootstrap method.
	 */
	@Nonnull
	public List<Constant> getBootstrapArguments() {
		return bootstrapArguments;
	}

	/**
	 * @param bootstrapArguments
	 * 		Zero or more extra arguments for the bootstrap method.
	 */
	public void setBootstrapArguments(@Nonnull List<Constant> bootstrapArguments) {
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
