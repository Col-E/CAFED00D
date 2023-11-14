package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.behavior.CpAccessor;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Variable table attribute.
 *
 * @author Matt Coley
 */
public class LocalVariableTableAttribute extends Attribute {
	private List<VarEntry> entries;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param entries
	 * 		Variable table entries.
	 */
	public LocalVariableTableAttribute(@Nonnull CpUtf8 name, @Nonnull List<VarEntry> entries) {
		super(name);
		this.entries = entries;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		for (VarEntry entry : getEntries())
			set.addAll(entry.cpAccesses());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// u2: line_number_table_length
		// entry[
		//   u2 start_pc;
		//   u2 length;
		//   u2 name_index;
		//   u2 descriptor_index;
		//   u2 index;
		// ]
		return 2 + (10 * entries.size());
	}

	/**
	 * @return Table entries.
	 */
	@Nonnull
	public List<VarEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 * 		New table entries.
	 */
	public void setEntries(@Nonnull List<VarEntry> entries) {
		this.entries = entries;
	}

	/**
	 * Variable table entry.
	 */
	public static class VarEntry implements CpAccessor {
		private final int startPc;
		private final int length;
		private final CpUtf8 name;
		private final CpUtf8 desc;
		private final int index;

		/**
		 * @param startPc
		 * 		Bytecode offset var starts at.
		 * @param length
		 * 		Bytecode length var spans across.
		 * @param name
		 * 		CP UTF8 name index.
		 * @param desc
		 * 		CP UTF8 desc index.
		 * @param index
		 * 		Variable index.
		 */
		public VarEntry(int startPc, int length, @Nonnull CpUtf8 name, @Nonnull CpUtf8 desc, int index) {
			this.startPc = startPc;
			this.length = length;
			this.name = name;
			this.desc = desc;
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
		 * @return CP UTF8 name index.
		 */
		@Nonnull
		public CpUtf8 getName() {
			return name;
		}

		/**
		 * @return CP UTF8 desc index.
		 */
		@Nonnull
		public CpUtf8 getDesc() {
			return desc;
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
			set.add(getName());
			set.add(getDesc());
			return set;
		}
	}
}
