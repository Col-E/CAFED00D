package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpModule;
import me.coley.cafedude.classfile.constant.CpPackage;
import me.coley.cafedude.classfile.constant.CpUtf8;

import java.util.List;

/**
 * Module attribute.
 *
 * @author Matt Coley
 */
public class ModuleAttribute extends Attribute {
	private int moduleIndex;
	private int flags;
	private int versionIndex;
	private List<Requires> requires;
	private List<Exports> exports;
	private List<Opens> opens;
	private List<Integer> uses;
	private List<Provides> provides;

	/**
	 * @param attrNameIndex
	 * 		Name index in constant pool of attribute.
	 * @param moduleIndex
	 * 		Constant pool index of {@link CpModule module name}.
	 * @param flags
	 * 		Module flags, see
	 *        {@code ACC_OPEN / 0x0020},
	 *        {@code ACC_SYNTHETIC / 0x1000}, and
	 *        {@code ACC_MANDATED / 0x8000}
	 * @param versionIndex
	 * 		Index in constant pool of module version utf8, or 0 if no version info.
	 * @param requires
	 * 		The {@link Requires} items.
	 * @param exports
	 * 		The {@link Exports} items.
	 * @param opens
	 * 		The {@link Opens} items.
	 * @param uses
	 * 		The uses list.
	 * @param provides
	 * 		The {@link Provides} items.
	 */
	public ModuleAttribute(int attrNameIndex, int moduleIndex, int flags, int versionIndex,
						   List<Requires> requires, List<Exports> exports,
						   List<Opens> opens, List<Integer> uses,
						   List<Provides> provides) {
		super(attrNameIndex);
		this.moduleIndex = moduleIndex;
		this.flags = flags;
		this.versionIndex = versionIndex;
		this.requires = requires;
		this.exports = exports;
		this.opens = opens;
		this.uses = uses;
		this.provides = provides;
	}

	/**
	 * @return Constant pool index of {@link CpModule module}.
	 */
	public int getModuleIndex() {
		return moduleIndex;
	}

	/**
	 * @param moduleIndex
	 * 		New module index.
	 */
	public void setModuleIndex(int moduleIndex) {
		this.moduleIndex = moduleIndex;
	}

	/**
	 * @return Module flags, see
	 * {@code ACC_OPEN / 0x0020},
	 * {@code ACC_SYNTHETIC / 0x1000}, and
	 * {@code ACC_MANDATED / 0x8000}
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * @param flags
	 * 		New module flags.
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}

	/**
	 * @return Index in constant pool of module {@link CpUtf8 version string}, or {@code 0} if no version info.
	 */
	public int getVersionIndex() {
		return versionIndex;
	}

	/**
	 * @param versionIndex
	 * 		New version index.
	 */
	public void setVersionIndex(int versionIndex) {
		this.versionIndex = versionIndex;
	}

	/**
	 * @return The {@link Requires} items.
	 */
	public List<Requires> getRequires() {
		return requires;
	}

	/**
	 * @param requires
	 * 		New require items.
	 */
	public void setRequires(List<Requires> requires) {
		this.requires = requires;
	}

	/**
	 * @return The {@link Exports} items.
	 */
	public List<Exports> getExports() {
		return exports;
	}

	/**
	 * @param exports
	 * 		New exports items.
	 */
	public void setExports(List<Exports> exports) {
		this.exports = exports;
	}

	/**
	 * @return The {@link Opens} items.
	 */
	public List<Opens> getOpens() {
		return opens;
	}

	/**
	 * @param opens
	 * 		New opens items.
	 */
	public void setOpens(List<Opens> opens) {
		this.opens = opens;
	}

	/**
	 * @return The uses list. Constant pool indices of {@link CpClass service interfaces classes} discoverable
	 * by using {@code ServiceLoader}.
	 */
	public List<Integer> getUses() {
		return uses;
	}

	/**
	 * @param uses
	 * 		New uses list.
	 */
	public void setUses(List<Integer> uses) {
		this.uses = uses;
	}

	/**
	 * @return The {@link Provides} items.
	 */
	public List<Provides> getProvides() {
		return provides;
	}

	/**
	 * @param provides
	 * 		The {@link Provides} items.
	 */
	public void setProvides(List<Provides> provides) {
		this.provides = provides;
	}

	@Override
	public int computeInternalLength() {
		// 6 = module_name_index + module_flags + module_version_index
		int len = 6;
		// requires = count + requires(u2 * 3)
		len += 2 + requires.size() * 6;
		// exports = count + exports(u2 * 3 + list[u2])
		len += 2 + exports.stream().mapToInt(Exports::length).sum();
		// opens = count + opens(u2 * 3 + list[u2])
		len += 2 + opens.stream().mapToInt(Opens::length).sum();
		// uses = uses_count + list[u2]
		len += 2 + uses.size() * 2;
		// provides = count + provides*(u2 * 2 + list[u2])
		len += 2 + provides.stream().mapToInt(Provides::length).sum();
		return len;
	}

	/**
	 * Module dependencies.
	 *
	 * @author Matt Coley
	 */
	public static class Requires {
		private int index;
		private int flags;
		private int versionIndex;

		/**
		 * @param index
		 * 		Constant pool index of {@link CpModule required module}.
		 * @param flags
		 * 		Require flags, see {@link #getFlags()} for more info.
		 * @param versionIndex
		 * 		Index in constant pool of required module {@link CpUtf8 version string},
		 * 		or {@code 0} if no version info.
		 */
		public Requires(int index, int flags, int versionIndex) {
			this.index = index;
			this.flags = flags;
			this.versionIndex = versionIndex;
		}

		/**
		 * @return Constant pool index of {@link CpModule required module}.
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * @param index
		 * 		New required module index.
		 */
		public void setIndex(int index) {
			this.index = index;
		}

		/**
		 * @return Require flags, see
		 * <ul>
		 *     <li>{@code ACC_TRANSITIVE} if any module depending on the current module also depends on
		 *     {@link #getIndex() this required module}</li>
		 *     <li>{@code ACC_STATIC_PHASE} if the dependency is only required at compile time.</li>
		 *     <li>{@code ACC_SYNTHETIC} if the dependency was not explicitly or implicitly defined
		 *     in the source of the module.</li>
		 *     <li>{@code ACC_MANDATED} if the dependency was implicitly defined in the source of the module.</li>
		 * </ul>
		 */
		public int getFlags() {
			return flags;
		}

		/**
		 * @param flags
		 * 		New require flags.
		 */
		public void setFlags(int flags) {
			this.flags = flags;
		}

		/**
		 * @return Index in constant pool of required module {@link CpUtf8 version string},
		 * or {@code 0} if no version info.
		 */
		public int getVersionIndex() {
			return versionIndex;
		}

		/**
		 * @param versionIndex
		 * 		New required module version index.
		 */
		public void setVersionIndex(int versionIndex) {
			this.versionIndex = versionIndex;
		}
	}

	/**
	 * Package export exposure for general usage.
	 *
	 * @author Matt Coley
	 */
	public static class Exports {
		private int index;
		private int flags;
		private List<Integer> toIndices;

		/**
		 * @param index
		 * 		Constant pool index of a {@link CpPackage package}.
		 * @param flags
		 * 		Export flags,
		 *        {@code ACC_SYNTHETIC} if it was not explicitly/implicitly declared in the module source code.
		 *        {@code ACC_MANDATED} if it was implicitly declared in the module source code.
		 * @param toIndices
		 * 		Constant pool indices of {@link CpModule modules} the {@link #getIndex() package} exports to.
		 */
		public Exports(int index, int flags, List<Integer> toIndices) {
			this.index = index;
			this.flags = flags;
			this.toIndices = toIndices;
		}

		/**
		 * @return Constant pool index of a {@link CpPackage package}.
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * @param index
		 * 		New package cp index.
		 */
		public void setIndex(int index) {
			this.index = index;
		}

		/**
		 * @return Export flags,
		 * {@code ACC_SYNTHETIC} if it was not explicitly/implicitly declared in the module source code.
		 * {@code ACC_MANDATED} if it was implicitly declared in the module source code.
		 */
		public int getFlags() {
			return flags;
		}

		/**
		 * @param flags
		 * 		New export flags.
		 */
		public void setFlags(int flags) {
			this.flags = flags;
		}

		/**
		 * @return Constant pool indices of {@link CpModule modules} the {@link #getIndex() package} exports to.
		 */
		public List<Integer> getToIndices() {
			return toIndices;
		}

		/**
		 * @param toIndex
		 * 		New opened module indices.
		 */
		public void setToIndices(List<Integer> toIndex) {
			this.toIndices = toIndex;
		}

		/**
		 * @return Length of the item.
		 */
		public int length() {
			// 6 = index + flags + list.size()
			return 6 + 2 * toIndices.size();
		}
	}

	/**
	 * Package open exposure for reflection.
	 *
	 * @author Matt Coley
	 */
	public static class Opens {
		private int index;
		private int flags;
		private List<Integer> toIndices;

		/**
		 * @param index
		 * 		Constant pool index of a {@link CpPackage package}.
		 * @param flags
		 * 		Open flags,
		 *        {@code ACC_SYNTHETIC} if it was not explicitly/implicitly declared in the module source code.
		 *        {@code ACC_MANDATED} if it was implicitly declared in the module source code.
		 * @param toIndices
		 * 		Constant pool indices of {@link CpModule modules} the {@link #getIndex()} is open to.
		 */
		public Opens(int index, int flags, List<Integer> toIndices) {
			this.index = index;
			this.flags = flags;
			this.toIndices = toIndices;
		}

		/**
		 * @return Constant pool index of a {@link CpPackage package}.
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * @param index
		 * 		New package cp index.
		 */
		public void setIndex(int index) {
			this.index = index;
		}

		/**
		 * @return Open flags,
		 * {@code ACC_SYNTHETIC} if it was not explicitly/implicitly declared in the module source code.
		 * {@code ACC_MANDATED} if it was implicitly declared in the module source code.
		 */
		public int getFlags() {
			return flags;
		}

		/**
		 * @param flags
		 * 		New open flags.
		 */
		public void setFlags(int flags) {
			this.flags = flags;
		}

		/**
		 * @return Constant pool indices of {@link CpModule modules} the {@link #getIndex() package} is open to.
		 */
		public List<Integer> getToIndices() {
			return toIndices;
		}

		/**
		 * @param toIndex
		 * 		New opened module indices.
		 */
		public void setToIndices(List<Integer> toIndex) {
			this.toIndices = toIndex;
		}

		/**
		 * @return Length of the item.
		 */
		public int length() {
			// 6 = index + flags + list.size()
			return 6 + 2 * toIndices.size();
		}
	}

	/**
	 * Provided interfaces with implementations.
	 *
	 * @author Matt Coley
	 */
	public static class Provides {
		private int index;
		private List<Integer> withIndex;

		/**
		 * @param index
		 * 		Constant pool index of {@link CpClass class} of a service interface.
		 * @param withIndex
		 * 		Constant pool indices of {@link CpClass classes} that are implementations of
		 *        {@link #getIndex() the service interface}.
		 */
		public Provides(int index, List<Integer> withIndex) {
			this.index = index;
			this.withIndex = withIndex;
		}

		/**
		 * @return Constant pool index of {@link CpClass class} of a service interface.
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * @param index
		 * 		New service interface index.
		 */
		public void setIndex(int index) {
			this.index = index;
		}

		/**
		 * @return Constant pool indices of {@link CpClass classes} that are implementations of
		 * {@link #getIndex() the service interface}.
		 */
		public List<Integer> getWithIndices() {
			return withIndex;
		}

		/**
		 * @param withIndex
		 * 		New implementation indices.
		 */
		public void setWithIndex(List<Integer> withIndex) {
			this.withIndex = withIndex;
		}

		/**
		 * @return Length of the item.
		 */
		public int length() {
			// 4 = index + list.size()
			return 4 + 2 * withIndex.size();
		}
	}
}
