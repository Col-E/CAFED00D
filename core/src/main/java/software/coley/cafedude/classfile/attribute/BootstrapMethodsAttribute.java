package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.behavior.CpAccessor;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpMethodHandle;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
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
	 * 		Constant pool entry holding the attribute name.
	 * @param bootstrapMethods
	 * 		List of boostrap methods <i>(ref + args)</i>.
	 */
	public BootstrapMethodsAttribute(@Nonnull CpUtf8 name, @Nonnull List<BootstrapMethod> bootstrapMethods) {
		super(name);
		this.bootstrapMethods = bootstrapMethods;
	}

	@Nonnull
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
	@Nonnull
	public List<BootstrapMethod> getBootstrapMethods() {
		return bootstrapMethods;
	}

	/**
	 * @param bootstrapMethods
	 * 		New list of boostrap methods.
	 */
	public void setBootstrapMethods(@Nonnull List<BootstrapMethod> bootstrapMethods) {
		this.bootstrapMethods = bootstrapMethods;
	}

	/**
	 * Bootstrap method representation.
	 *
	 * @author Matt Coley
	 */
	public static class BootstrapMethod implements CpAccessor {
		private CpMethodHandle bsmMethodRef;
		private List<CpEntry> args;

		/**
		 * @param bsmMethodRef
		 * 		Constant pool entry of the method reference.
		 * @param args
		 * 		List of arguments as indices of constant pool items.
		 */
		public BootstrapMethod(@Nonnull CpMethodHandle bsmMethodRef, @Nonnull List<CpEntry> args) {
			this.bsmMethodRef = bsmMethodRef;
			this.args = args;
		}

		@Nonnull
		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(getBsmMethodRef());
			set.addAll(getArgs());
			return set;
		}

		/**
		 * @return Constant pool entry of the method reference.
		 */
		@Nonnull
		public CpMethodHandle getBsmMethodRef() {
			return bsmMethodRef;
		}

		/**
		 * @param bsmMethodRef
		 * 		New constant pool entry of the method reference.
		 */
		public void setBsmMethodRef(@Nonnull CpMethodHandle bsmMethodRef) {
			this.bsmMethodRef = bsmMethodRef;
		}

		/**
		 * @return List of arguments to the {@link #getBsmMethodRef() bootstrap method}
		 * as indices of constant pool items.
		 */
		@Nonnull
		public List<CpEntry> getArgs() {
			return args;
		}

		/**
		 * @param args
		 * 		New list of arguments to the {@link #getBsmMethodRef() bootstrap method}.
		 */
		public void setArgs(@Nonnull List<CpEntry> args) {
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
