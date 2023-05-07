package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param module
	 * 		Constant pool entry holding the {@link CpModule module name}.
	 * @param flags
	 * 		Module flags, see
	 *        {@code ACC_OPEN / 0x0020},
	 *        {@code ACC_SYNTHETIC / 0x1000}, and
	 *        {@code ACC_MANDATED / 0x8000}
	 * @param version
	 * 		Constant pool entry holding the module version utf8, or {@code null} if no version info.
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
	public ModuleAttribute(@Nonnull CpUtf8 name, @Nonnull CpModule module, int flags, @Nullable CpUtf8 version,
						   @Nonnull List<Requires> requires, @Nonnull List<Exports> exports,
						   @Nonnull List<Opens> opens, @Nonnull List<CpClass> uses,
						   @Nonnull List<Provides> provides) {
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
	 * @return Constant pool entry holding the {@link CpModule module name}.
	 */
	@Nonnull
	public CpModule getModule() {
		return module;
	}

	/**
	 * @param module
	 * 		Constant pool entry holding the {@link CpModule module name}.
	 */
	public void setModule(@Nonnull CpModule module) {
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
	 * @return Constant pool entry holding the module version utf8, or {@code null} if no version info.
	 */
	@Nullable
	public CpUtf8 getVersion() {
		return version;
	}

	/**
	 * @param version
	 * 		New constant pool entry holding the module version utf8, or {@code null} if no version info.
	 */
	public void setVersion(@Nonnull CpUtf8 version) {
		this.version = version;
	}

	/**
	 * @return The {@link Requires} items.
	 */
	@Nonnull
	public List<Requires> getRequires() {
		return requires;
	}

	/**
	 * @param requires
	 * 		New require items.
	 */
	public void setRequires(@Nonnull List<Requires> requires) {
		this.requires = requires;
	}

	/**
	 * @return The {@link Exports} items.
	 */
	@Nonnull
	public List<Exports> getExports() {
		return exports;
	}

	/**
	 * @param exports
	 * 		New exports items.
	 */
	public void setExports(@Nonnull List<Exports> exports) {
		this.exports = exports;
	}

	/**
	 * @return The {@link Opens} items.
	 */
	@Nonnull
	public List<Opens> getOpens() {
		return opens;
	}

	/**
	 * @param opens
	 * 		New opens items.
	 */
	public void setOpens(@Nonnull List<Opens> opens) {
		this.opens = opens;
	}

	/**
	 * @return The uses list. Constant pool indices of {@link CpClass service interfaces classes} discoverable
	 * by using {@code ServiceLoader}.
	 */
	@Nonnull
	public List<CpClass> getUses() {
		return uses;
	}

	/**
	 * @param uses
	 * 		New uses list.
	 */
	public void setUses(@Nonnull List<CpClass> uses) {
		this.uses = uses;
	}

	/**
	 * @return The {@link Provides} items.
	 */
	@Nonnull
	public List<Provides> getProvides() {
		return provides;
	}

	/**
	 * @param provides
	 * 		The {@link Provides} items.
	 */
	public void setProvides(@Nonnull List<Provides> provides) {
		this.provides = provides;
	}

	@Nonnull
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
		 * 		Constant pool entry holding the {@link CpModule required module}.
		 * @param flags
		 * 		Require flags, see {@link #getFlags()} for more info.
		 * @param version
		 * 		Constant pool entry holding the module version utf8, or {@code null} if no version info.
		 */
		public Requires(@Nonnull CpModule module, int flags, @Nullable CpUtf8 version) {
			this.module = module;
			this.flags = flags;
			this.version = version;
		}

		/**
		 * @return Constant pool entry holding the {@link CpModule required module}.
		 */
		@Nonnull
		public CpModule getModule() {
			return module;
		}

		/**
		 * @param module
		 * 		New constant pool entry holding the {@link CpModule required module}.
		 */
		public void setModule(@Nonnull CpModule module) {
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
		 * @return Constant pool entry holding the module version utf8, or {@code null} if no version info.
		 */
		@Nullable
		public CpUtf8 getVersion() {
			return version;
		}

		/**
		 * @param version
		 * 		New constant pool entry holding the module version utf8, or {@code null} if no version info.
		 */
		public void setVersion(@Nullable CpUtf8 version) {
			this.version = version;
		}

		@Nonnull
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
		 * 		Constant pool entry holding the {@link CpPackage package name}.
		 * @param flags
		 * 		Export flags,
		 *        {@code ACC_SYNTHETIC} if it was not explicitly/implicitly declared in the module source code.
		 *        {@code ACC_MANDATED} if it was implicitly declared in the module source code.
		 * @param to
		 * 		Constant pool entries holding the {@link CpModule modules} the
		 *        {@link #getPackageEntry() package} exports to.
		 */
		public Exports(@Nonnull CpPackage packageEntry, int flags, @Nonnull List<CpModule> to) {
			this.packageEntry = packageEntry;
			this.flags = flags;
			this.to = to;
		}

		/**
		 * @return Constant pool entry holding the {@link CpPackage package name}.
		 */
		@Nonnull
		public CpPackage getPackageEntry() {
			return packageEntry;
		}

		/**
		 * @param packageEntry
		 * 		New constant pool entry holding the {@link CpPackage package name}.
		 */
		public void setPackageEntry(@Nonnull CpPackage packageEntry) {
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
		 * @return Constant pool entries holding the {@link CpModule modules} the
		 * {@link #getPackageEntry() package} exports to.
		 */
		@Nonnull
		public List<CpModule> getTo() {
			return to;
		}

		/**
		 * @param toIndex
		 * 		New constant pool entries holding the {@link CpModule modules} the
		 *        {@link #getPackageEntry() package} exports to.
		 */
		public void setTo(@Nonnull List<CpModule> toIndex) {
			this.to = toIndex;
		}

		/**
		 * @return Length of the item.
		 */
		public int length() {
			// 6 = index + flags + list.size()
			return 6 + 2 * to.size();
		}

		@Nonnull
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
		 * 		Constant pool entry holding the {@link CpPackage package name}.
		 * @param flags
		 * 		Open flags,
		 *        {@code ACC_SYNTHETIC} if it was not explicitly/implicitly declared in the module source code.
		 *        {@code ACC_MANDATED} if it was implicitly declared in the module source code.
		 * @param to
		 * 		Constant pool entries holding the {@link CpModule modules} the
		 *        {@link #getPackageEntry() package} exports to.
		 */
		public Opens(@Nonnull CpPackage packageEntry, int flags, @Nonnull List<CpModule> to) {
			this.packageEntry = packageEntry;
			this.flags = flags;
			this.to = to;
		}

		/**
		 * @return Constant pool entry holding the {@link CpPackage package name}.
		 */
		@Nonnull
		public CpPackage getPackageEntry() {
			return packageEntry;
		}

		/**
		 * @param packageEntry
		 * 		New constant pool entry holding the {@link CpPackage package name}.
		 */
		public void setPackageEntry(@Nonnull CpPackage packageEntry) {
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
		 * @return Constant pool entries holding the {@link CpModule modules} the
		 * {@link #getPackageEntry() package} exports to.
		 */
		@Nonnull
		public List<CpModule> getTo() {
			return to;
		}

		/**
		 * @param toIndex
		 * 		New constant pool entries holding the {@link CpModule modules} the
		 *        {@link #getPackageEntry() package} exports to.
		 */
		public void setTo(@Nonnull List<CpModule> toIndex) {
			this.to = toIndex;
		}

		/**
		 * @return Length of the item.
		 */
		public int length() {
			// 6 = index + flags + list.size()
			return 6 + 2 * to.size();
		}

		@Nonnull
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
		 * 		Constant pool entry holding the {@link CpClass class} of a service interface.
		 * @param with
		 * 		Constant pool entries of {@link CpClass classes} that are implementations of
		 *        {@link #getModule() the service interface}.
		 */
		public Provides(@Nonnull CpClass module, @Nonnull List<CpClass> with) {
			this.module = module;
			this.with = with;
		}

		/**
		 * @return Constant pool entry holding the {@link CpClass class} of a service interface.
		 */
		@Nonnull
		public CpClass getModule() {
			return module;
		}

		/**
		 * @param module
		 * 		New constant pool entry holding the {@link CpClass class} of a service interface.
		 */
		public void setModule(@Nonnull CpClass module) {
			this.module = module;
		}

		/**
		 * @return Constant pool entries of {@link CpClass classes} that are implementations of
		 * {@link #getModule() the service interface}.
		 */
		@Nonnull
		public List<CpClass> getWith() {
			return with;
		}

		/**
		 * @param with
		 * 		New implementation indices.
		 */
		public void setWith(@Nonnull List<CpClass> with) {
			this.with = with;
		}

		/**
		 * @return Length of the item.
		 */
		public int length() {
			// 4 = index + list.size()
			return 4 + 2 * with.size();
		}

		@Nonnull
		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(getModule());
			set.addAll(getWith());
			return set;
		}
	}
}
