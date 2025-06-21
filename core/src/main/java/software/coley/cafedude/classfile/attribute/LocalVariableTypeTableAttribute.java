package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.behavior.CpAccessor;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Variable generic/type table attribute.
 *
 * @author Matt Coley
 */
public class LocalVariableTypeTableAttribute extends Attribute {
	private List<VarTypeEntry> entries;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param entries
	 * 		Variable type table entries.
	 */
	public LocalVariableTypeTableAttribute(@Nonnull CpUtf8 name, @Nonnull List<VarTypeEntry> entries) {
		super(name);
		this.entries = entries;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		for (VarTypeEntry entry : getEntries())
			set.addAll(entry.cpAccesses());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// u2: local_variable_type_table_length
		// entry[
		//   u2 start_pc;
		//   u2 length;
		//   u2 name_index;
		//   u2 signature_index;
		//   u2 index;
		// ]
		return 2 + (10 * entries.size());
	}

	/**
	 * @return Table entries.
	 */
	@Nonnull
	public List<VarTypeEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 * 		New table entries.
	 */
	public void setEntries(@Nonnull List<VarTypeEntry> entries) {
		this.entries = entries;
	}

	/**
	 * Variable table entry.
	 */
	public static class VarTypeEntry implements CpAccessor {
		private final int startPc;
		private final int length;
		private final CpUtf8 name;
		private final CpUtf8 signature;
		private final int index;

		/**
		 * @param startPc
		 * 		Bytecode offset var starts at.
		 * @param length
		 * 		Bytecode length var spans across.
		 * @param name
		 * 		Constant pool entry holding the variable name.
		 * @param signature
		 * 		Constant pool entry holding the variable signature.
		 * @param index
		 * 		Variable index.
		 */
		public VarTypeEntry(int startPc, int length, @Nonnull CpUtf8 name, @Nonnull CpUtf8 signature, int index) {
			this.startPc = startPc;
			this.length = length;
			this.name = name;
			this.signature = signature;
			this.index = index;
		}

		/**
		 * @return Bytecode offset var starts at.
		 */
		public int getStartPc() {
			return startPc;
		}

		/**
		 * @return Bytecode length var spans across.
		 */
		public int getLength() {
			return length;
		}

		/**
		 * @return Constant pool entry holding the variable name.
		 */
		@Nonnull
		public CpUtf8 getName() {
			return name;
		}

		/**
		 * @return Constant pool entry holding the variable signature.
		 */
		@Nonnull
		public CpUtf8 getSignature() {
			return signature;
		}

		/**
		 * @return Variable index.
		 */
		public int getIndex() {
			return index;
		}

		@Nonnull
		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(name);
			set.add(signature);
			return set;
		}
	}
}
