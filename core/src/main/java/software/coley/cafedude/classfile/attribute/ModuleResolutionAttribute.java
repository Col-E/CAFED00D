package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;

/**
 * Module target attribute, will hold values like <i>"linux-x86"</i>, <i>"win-x64"</i>, etc.
 *
 * @author Matt Coley
 */
public non-sealed class ModuleResolutionAttribute extends Attribute {
	/**
	 * Optional flag to disable aspects of module bootstrapping when consuming this module.
	 * <br>
	 * See {@code jdk.internal.module.ModuleBootstrap}
	 */
	public static final int DO_NOT_RESOLVE_BY_DEFAULT = 0x0001;
	/** Flag to indicate to consuming modules this is deprecated. */
	public static final int WARN_DEPRECATED = 0x0002;
	/** Flag to indicate to consuming modules this is deprecated for removal. */
	public static final int WARN_DEPRECATED_FOR_REMOVAL = 0x0004;
	/** Flag to indicate to consuming modules this is an incubating API. */
	public static final int WARN_INCUBATING = 0x0008;
	private int flags;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param flags
	 * 		Flags to inform how resolution of this attribute should be modified.
	 */
	public ModuleResolutionAttribute(@Nonnull CpUtf8 name, int flags) {
		super(name);
		this.flags = flags;
	}

	/**
	 * Format of mask:
	 * <pre>{@code
	 *   // Optional
	 *   0x0001 (DO_NOT_RESOLVE_BY_DEFAULT)
	 *
	 *   // At most one of:
	 *   0x0002 (WARN_DEPRECATED)
	 *   0x0004 (WARN_DEPRECATED_FOR_REMOVAL)
	 *   0x0008 (WARN_INCUBATING)
	 * }</pre>
	 *
	 * @return Flags to inform how resolution of this attribute should be modified.
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * @param flags
	 * 		Flags to inform how resolution of this attribute should be modified.
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}

	@Override
	public int computeInternalLength() {
		// U2: resolution_flags
		return 2;
	}
}
