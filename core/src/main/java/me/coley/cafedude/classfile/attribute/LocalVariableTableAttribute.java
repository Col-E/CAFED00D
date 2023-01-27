package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

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
	 * 		Name index in constant pool.
	 * @param entries
	 * 		Variable table entries.
	 */
	public LocalVariableTableAttribute(CpUtf8 name, List<VarEntry> entries) {
		super(name);
		this.entries = entries;
	}

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
	public List<VarEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 * 		New table entries.
	 */
	public void setEntries(List<VarEntry> entries) {
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
		public VarEntry(int startPc, int length, CpUtf8 name, CpUtf8 desc, int index) {
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
		public CpUtf8 getName() {
			return name;
		}

		/**
		 * @return CP UTF8 desc index.
		 */
		public CpUtf8 getDesc() {
			return desc;
		}

		/**
		 * @return Variable index.
		 */
		public int getIndex() {
			return index;
		}

		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(getName());
			set.add(getDesc());
			return set;
		}
	}
}
