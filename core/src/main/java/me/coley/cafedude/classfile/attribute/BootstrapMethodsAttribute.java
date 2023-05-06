package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpMethodHandle;
import me.coley.cafedude.classfile.constant.CpUtf8;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bootstrap methods attribute.
 *
 * @author Matt Coley
 */
public class BootstrapMethodsAttribute extends Attribute {
	private List<BootstrapMethod> bootstrapMethods;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param bootstrapMethods
	 * 		List of boostrap methods <i>(ref + args)</i>.
	 */
	public BootstrapMethodsAttribute(CpUtf8 name, List<BootstrapMethod> bootstrapMethods) {
		super(name);
		this.bootstrapMethods = bootstrapMethods;
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		for (BootstrapMethod bsm : bootstrapMethods)
			set.addAll(bsm.cpAccesses());
		return set;
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
	public static class BootstrapMethod implements CpAccessor {
		private CpMethodHandle bsmMethodref;
		private List<CpEntry> args;

		/**
		 * @param bsmMethodref
		 * 		Constant pool entry of method reference, {@link CpMethodHandle}.
		 * @param args
		 * 		List of arguments as indices of constant pool items.
		 */
		public BootstrapMethod(CpMethodHandle bsmMethodref, List<CpEntry> args) {
			this.bsmMethodref = bsmMethodref;
			this.args = args;
		}

		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(getBsmMethodref());
			set.addAll(getArgs());
			return set;
		}

		/**
		 * @return Constant pool index of method reference, {@link CpMethodHandle}.
		 */
		public CpMethodHandle getBsmMethodref() {
			return bsmMethodref;
		}

		/**
		 * @param bsmMethodref
		 * 		New constant pool index of method reference, {@link CpMethodHandle}.
		 */
		public void setBsmMethodref(CpMethodHandle bsmMethodref) {
			this.bsmMethodref = bsmMethodref;
		}

		/**
		 * @return List of arguments to the {@link #getBsmMethodref() bootstrap method}
		 * as indices of constant pool items.
		 */
		public List<CpEntry> getArgs() {
			return args;
		}

		/**
		 * @param args
		 * 		New list of arguments to the {@link #getBsmMethodref() bootstrap method}.
		 */
		public void setArgs(List<CpEntry> args) {
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
