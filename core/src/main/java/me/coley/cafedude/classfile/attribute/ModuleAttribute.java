package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Module attribute.
 *
 * @author Matt Coley
 */
public class ModuleAttribute extends Attribute {
	private CpModule module;
	private int flags;
	private CpUtf8 version;
	private List<Requires> requires;
	private List<Exports> exports;
	private List<Opens> opens;
	private List<CpClass> uses;
	private List<Provides> provides;

	/**
	 * @param attrname
	 * 		Name index in constant pool of attribute.
	 * @param module
	 * 		Constant pool index of {@link CpModule module name}.
	 * @param flags
	 * 		Module flags, see
	 *        {@code ACC_OPEN / 0x0020},
	 *        {@code ACC_SYNTHETIC / 0x1000}, and
	 *        {@code ACC_MANDATED / 0x8000}
	 * @param version
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
	public ModuleAttribute(CpUtf8 name, CpModule module, int flags, CpUtf8 version,
						   List<Requires> requires, List<Exports> exports,
						   List<Opens> opens, List<CpClass> uses,
						   List<Provides> provides) {
		super(name);
		this.module = module;
		this.flags = flags;
		this.version = version;
		this.requires = requires;
		this.exports = exports;
		this.opens = opens;
		this.uses = uses;
		this.provides = provides;
	}

	/**
	 * @return Constant pool index of {@link CpModule module}.
	 */
	public CpModule getModule() {
		return module;
	}

	/**
	 * @param module
	 * 		New module index.
	 */
	public void setModule(CpModule module) {
		this.module = module;
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
	public CpUtf8 getVersion() {
		return version;
	}

	/**
	 * @param version
	 * 		New version index.
	 */
	public void setVersion(CpUtf8 version) {
		this.version = version;
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
	public List<CpClass> getUses() {
		return uses;
	}

	/**
	 * @param uses
	 * 		New uses list.
	 */
	public void setUses(List<CpClass> uses) {
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
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(getModule());
		set.add(getVersion());
		set.addAll(getUses());
		for (Requires requires : getRequires())
			set.addAll(requires.cpAccesses());
		for (Exports exports : getExports())
			set.addAll(exports.cpAccesses());
		for (Opens opens : getOpens())
			set.addAll(opens.cpAccesses());
		for (Provides provides : getProvides())
			set.addAll(provides.cpAccesses());
		return set;
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
	public static class Requires implements CpAccessor {
		private CpModule module;
		private int flags;
		private CpUtf8 version;

		/**
		 * @param module
		 * 		Constant pool index of {@link CpModule required module}.
		 * @param flags
		 * 		Require flags, see {@link #getFlags()} for more info.
		 * @param version
		 * 		Index in constant pool of required module {@link CpUtf8 version string},
		 * 		or {@code null} if no version info.
		 */
		public Requires(CpModule module, int flags, CpUtf8 version) {
			this.module = module;
			this.flags = flags;
			this.version = version;
		}

		/**
		 * @return Constant pool index of {@link CpModule required module}.
		 */
		public CpModule getModule() {
			return module;
		}

		/**
		 * @param module
		 * 		New required module index.
		 */
		public void setModule(CpModule module) {
			this.module = module;
		}

		/**
		 * @return Require flags, see
		 * <ul>
		 *     <li>{@code ACC_TRANSITIVE} if any module depending on the current module also depends on
		 *     {@link #getModule() this required module}</li>
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
		public CpUtf8 getVersion() {
			return version;
		}

		/**
		 * @param version
		 * 		New required module version index.
		 */
		public void setVersion(CpUtf8 version) {
			this.version = version;
		}

		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(getVersion());
			set.add(getModule());
			return set;
		}
	}

	/**
	 * Package export exposure for general usage.
	 *
	 * @author Matt Coley
	 */
	public static class Exports implements CpAccessor {
		private CpPackage packageEntry;
		private int flags;
		private List<CpModule> to;

		/**
		 * @param packageEntry
		 * 		Constant pool index of a {@link CpPackage package}.
		 * @param flags
		 * 		Export flags,
		 *        {@code ACC_SYNTHETIC} if it was not explicitly/implicitly declared in the module source code.
		 *        {@code ACC_MANDATED} if it was implicitly declared in the module source code.
		 * @param to
		 * 		Constant pool indices of {@link CpModule modules} the {@link #getPackageEntry() package} exports to.
		 */
		public Exports(CpPackage packageEntry, int flags, List<CpModule> to) {
			this.packageEntry = packageEntry;
			this.flags = flags;
			this.to = to;
		}

		/**
		 * @return Constant pool index of a {@link CpPackage package}.
		 */
		public CpPackage getPackageEntry() {
			return packageEntry;
		}

		/**
		 * @param packageEntry
		 * 		New package cp index.
		 */
		public void setPackageEntry(CpPackage packageEntry) {
			this.packageEntry = packageEntry;
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
		 * @return Constant pool indices of {@link CpModule modules} the {@link #getPackageEntry() package} exports to.
		 */
		public List<CpModule> getTo() {
			return to;
		}

		/**
		 * @param toIndex
		 * 		New opened module indices.
		 */
		public void setTo(List<CpModule> toIndex) {
			this.to = toIndex;
		}

		/**
		 * @return Length of the item.
		 */
		public int length() {
			// 6 = index + flags + list.size()
			return 6 + 2 * to.size();
		}

		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(getPackageEntry());
			set.addAll(getTo());
			return set;
		}
	}

	/**
	 * Package open exposure for reflection.
	 *
	 * @author Matt Coley
	 */
	public static class Opens implements CpAccessor {
		private CpPackage packageEntry;
		private int flags;
		private List<CpModule> to;

		/**
		 * @param packageEntry
		 * 		Constant pool index of a {@link CpPackage package}.
		 * @param flags
		 * 		Open flags,
		 *        {@code ACC_SYNTHETIC} if it was not explicitly/implicitly declared in the module source code.
		 *        {@code ACC_MANDATED} if it was implicitly declared in the module source code.
		 * @param to
		 * 		Constant pool indices of {@link CpModule modules} the {@link #getPackageEntry()} is open to.
		 */
		public Opens(CpPackage packageEntry, int flags, List<CpModule> to) {
			this.packageEntry = packageEntry;
			this.flags = flags;
			this.to = to;
		}

		/**
		 * @return Constant pool index of a {@link CpPackage package}.
		 */
		public CpPackage getPackageEntry() {
			return packageEntry;
		}

		/**
		 * @param packageEntry
		 * 		New package cp index.
		 */
		public void setPackageEntry(CpPackage packageEntry) {
			this.packageEntry = packageEntry;
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
		 * @return Constant pool indices of {@link CpModule modules} the {@link #getPackageEntry() package} is open to.
		 */
		public List<CpModule> getTo() {
			return to;
		}

		/**
		 * @param toIndex
		 * 		New opened module indices.
		 */
		public void setTo(List<CpModule> toIndex) {
			this.to = toIndex;
		}

		/**
		 * @return Length of the item.
		 */
		public int length() {
			// 6 = index + flags + list.size()
			return 6 + 2 * to.size();
		}

		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(getPackageEntry());
			set.addAll(getTo());
			return set;
		}
	}

	/**
	 * Provided interfaces with implementations.
	 *
	 * @author Matt Coley
	 */
	public static class Provides implements CpAccessor {
		private CpClass module;
		private List<CpClass> with;

		/**
		 * @param module
		 * 		Constant pool index of {@link CpClass class} of a service interface.
		 * @param with
		 * 		Constant pool indices of {@link CpClass classes} that are implementations of
		 *        {@link #getModule() the service interface}.
		 */
		public Provides(CpClass module, List<CpClass> with) {
			this.module = module;
			this.with = with;
		}

		/**
		 * @return Constant pool index of {@link CpClass class} of a service interface.
		 */
		public CpClass getModule() {
			return module;
		}

		/**
		 * @param module
		 * 		New service interface index.
		 */
		public void setModule(CpClass module) {
			this.module = module;
		}

		/**
		 * @return Constant pool indices of {@link CpClass classes} that are implementations of
		 * {@link #getModule() the service interface}.
		 */
		public List<CpClass> getWith() {
			return with;
		}

		/**
		 * @param with
		 * 		New implementation indices.
		 */
		public void setWith(List<CpClass> with) {
			this.with = with;
		}

		/**
		 * @return Length of the item.
		 */
		public int length() {
			// 4 = index + list.size()
			return 4 + 2 * with.size();
		}

		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(getModule());
			set.addAll(getWith());
			return set;
		}
	}
}
