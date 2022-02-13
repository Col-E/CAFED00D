package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpMethodHandle;

import java.util.List;

/**
 * Bootstrap methods attribute.
 *
 * @author Matt Coley
 */
public class BootstrapMethodsAttribute extends Attribute {
	private List<BootstrapMethod> bootstrapMethods;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param bootstrapMethods
	 * 		List of boostrap methods <i>(ref + args)</i>.
	 */
	public BootstrapMethodsAttribute(int nameIndex, List<BootstrapMethod> bootstrapMethods) {
		super(nameIndex);
		this.bootstrapMethods = bootstrapMethods;
	}

	@Override
	public int computeInternalLength() {
		return 2 + bootstrapMethods.stream().mapToInt(BootstrapMethod::computeLength).sum();
	}

	/**
	 * @return List of boostrap methods.
	 */
	public List<BootstrapMethod> getBootstrapMethods() {
		return bootstrapMethods;
	}

	/**
	 * @param bootstrapMethods
	 * 		New list of boostrap methods.
	 */
	public void setBootstrapMethods(List<BootstrapMethod> bootstrapMethods) {
		this.bootstrapMethods = bootstrapMethods;
	}

	/**
	 * Bootstrap method representation.
	 *
	 * @author Matt Coley
	 */
	public static class BootstrapMethod {
		private int bsmMethodref;
		private List<Integer> args;

		/**
		 * @param bsmMethodref
		 * 		Constant pool index of method reference, {@link CpMethodHandle}.
		 * @param args
		 * 		List of arguments as indices of constant pool items.
		 */
		public BootstrapMethod(int bsmMethodref, List<Integer> args) {
			this.bsmMethodref = bsmMethodref;
			this.args = args;
		}

		/**
		 * @return Constant pool index of method reference, {@link CpMethodHandle}.
		 */
		public int getBsmMethodref() {
			return bsmMethodref;
		}

		/**
		 * @param bsmMethodref
		 * 		New constant pool index of method reference, {@link CpMethodHandle}.
		 */
		public void setBsmMethodref(int bsmMethodref) {
			this.bsmMethodref = bsmMethodref;
		}

		/**
		 * @return List of arguments to the {@link #getBsmMethodref() bootstrap method}
		 * as indices of constant pool items.
		 */
		public List<Integer> getArgs() {
			return args;
		}

		/**
		 * @param args
		 * 		New list of arguments to the {@link #getBsmMethodref() bootstrap method}.
		 */
		public void setArgs(List<Integer> args) {
			this.args = args;
		}

		/**
		 * @return Length of bsm item.
		 */
		public int computeLength() {
			// u2 bootstrap_method_ref;
			// u2 num_bootstrap_arguments;
			// u2 bootstrap_arguments[num_bootstrap_arguments];
			return 4 + 2 * args.size();
		}
	}
}
