package me.coley.cafedude.instruction;

import me.coley.cafedude.constant.ConstPoolEntry;

import java.util.Arrays;

/**
 * InvokeDynamic instruction.
 *
 * @author xDark
 */
public class InvokeDynamicInstruction extends BasicInstruction {
	private String name;
	private String desc;
	private MethodHandle methodHandle;
	private ConstPoolEntry[] bootstrapArguments;

	/**
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method desc.
	 * @param methodHandle
	 * 		Bootstrap method.
	 * @param bootstrapArguments
	 * 		Bootstrap arguments.
	 */
	public InvokeDynamicInstruction(String name, String desc,
								MethodHandle methodHandle, ConstPoolEntry[] bootstrapArguments) {
		super(Opcodes.INVOKEDYNAMIC);
		this.name = name;
		this.desc = desc;
		this.methodHandle = methodHandle;
		this.bootstrapArguments = bootstrapArguments;
	}

	/**
	 * @return method name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets method name.
	 *
	 * @param name
	 * 		New name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return method desc.
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Sets method descriptor.
	 *
	 * @param desc
	 * 		New descriptor.
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * @return bootstrap handle.
	 */
	public MethodHandle getMethodHandle() {
		return methodHandle;
	}

	/**
	 * Sets bootstrap handle.
	 *
	 * @param methodHandle
	 * 		New method handle.
	 */
	public void setMethodHandle(MethodHandle methodHandle) {
		this.methodHandle = methodHandle;
	}

	/**
	 * @return bootstrap arguments.
	 */
	public ConstPoolEntry[] getBootstrapArguments() {
		return bootstrapArguments;
	}

	/**
	 * Sets bootstrap arguments.
	 *
	 * @param bootstrapArguments
	 * 		New arguments.
	 */
	public void setBootstrapArguments(ConstPoolEntry[] bootstrapArguments) {
		this.bootstrapArguments = bootstrapArguments;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof InvokeDynamicInstruction)) return false;
		if (!super.equals(o)) return false;

		InvokeDynamicInstruction that = (InvokeDynamicInstruction) o;

		if (!name.equals(that.name)) return false;
		if (!desc.equals(that.desc)) return false;
		if (!methodHandle.equals(that.methodHandle)) return false;
		return Arrays.equals(bootstrapArguments, that.bootstrapArguments);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + desc.hashCode();
		result = 31 * result + methodHandle.hashCode();
		result = 31 * result + Arrays.hashCode(bootstrapArguments);
		return result;
	}

	@Override
	public String toString() {
		return "invokedynamic(" + name + ", " + desc + ", " + methodHandle + ", " + Arrays.toString(bootstrapArguments) + ')';
	}
}
