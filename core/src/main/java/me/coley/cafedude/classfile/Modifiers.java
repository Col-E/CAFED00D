package me.coley.cafedude.classfile;

/**
 * Modifiers for flags on classes/fields/methods/attributes.
 * Additionally, contains a few utility methods for managing masks.
 *
 * @author Matt Coley
 */
public interface Modifiers {
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * <li>field</li>
	 * <li>method</li>
	 * </ul>
	 */
	int ACC_PUBLIC = 0x0001;
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * <li>field</li>
	 * <li>method</li>
	 * </ul>
	 */
	int ACC_PRIVATE = 0x0002;
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * <li>field</li>
	 * <li>method</li>
	 * </ul>
	 */
	int ACC_PROTECTED = 0x0004;
	/**
	 * Applies to:
	 * <ul>
	 * <li>field</li>
	 * <li>method</li>
	 * </ul>
	 */
	int ACC_STATIC = 0x0008;
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * <li>field</li>
	 * <li>method</li>
	 * <li>parameter</li>
	 * </ul>
	 */
	int ACC_FINAL = 0x0010;
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * </ul>
	 */
	int ACC_SUPER = 0x0020;
	/**
	 * Applies to:
	 * <ul>
	 * <li>method</li>
	 * </ul>
	 */
	int ACC_SYNCHRONIZED = 0x0020;
	/**
	 * Applies to:
	 * <ul>
	 * <li>module</li>
	 * </ul>
	 */
	int ACC_OPEN = 0x0020;
	/**
	 * Applies to:
	 * <ul>
	 * <li>module requires</li>
	 * </ul>
	 */
	int ACC_TRANSITIVE = 0x0020;
	/**
	 * Applies to:
	 * <ul>
	 * <li>field</li>
	 * </ul>
	 */
	int ACC_VOLATILE = 0x0040;
	/**
	 * Applies to:
	 * <ul>
	 * <li>method</li>
	 * </ul>
	 */
	int ACC_BRIDGE = 0x0040;
	/**
	 * Applies to:
	 * <ul>
	 * <li>module requires</li>
	 * </ul>
	 */
	int ACC_STATIC_PHASE = 0x0040;
	/**
	 * Applies to:
	 * <ul>
	 * <li>method</li>
	 * </ul>
	 */
	int ACC_VARARGS = 0x0080;
	/**
	 * Applies to:
	 * <ul>
	 * <li>field</li>
	 * </ul>
	 */
	int ACC_TRANSIENT = 0x0080;
	/**
	 * Applies to:
	 * <ul>
	 * <li>method</li>
	 * </ul>
	 */
	int ACC_NATIVE = 0x0100;
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * </ul>
	 */
	int ACC_INTERFACE = 0x0200;
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * <li>method</li>
	 * </ul>
	 */
	int ACC_ABSTRACT = 0x0400;
	/**
	 * Applies to:
	 * <ul>
	 * <li>method</li>
	 * </ul>
	 */
	int ACC_STRICT = 0x0800;
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * <li>field</li>
	 * <li>method</li>
	 * <li>parameter</li>
	 * <li>module</li>
	 * </ul>
	 */
	int ACC_SYNTHETIC = 0x1000;
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * </ul>
	 */
	int ACC_ANNOTATION = 0x2000;
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * <li>field</li>
	 * <li>inner class</li>
	 * </ul>
	 */
	int ACC_ENUM = 0x4000;
	/**
	 * Applies to:
	 * <ul>
	 * <li>field</li>
	 * <li>method</li>
	 * <li>parameter</li>
	 * <li>module</li>
	 * <li>module</li>
	 * </ul>
	 */
	int ACC_MANDATED = 0x8000;
	/**
	 * Applies to:
	 * <ul>
	 * <li>class</li>
	 * </ul>
	 */
	int ACC_MODULE = 0x8000;

	/**
	 * @param mask
	 * 		Modifier mask.
	 * @param flag
	 * 		Flag to check.
	 *
	 * @return {@code true} when the mask contains the flag.
	 */
	static boolean has(int mask, int flag) {
		return (mask & flag) != 0;
	}

	/**
	 * @param mask
	 * 		Modifier mask.
	 * @param flag
	 * 		Flag to flip in the mask.
	 *
	 * @return The mask with the flag value flipped.
	 */
	static int flip(int mask, int flag) {
		return has(mask, flag) ? remove(mask, flag) : mask | flag;
	}

	/**
	 * @param mask
	 * 		Modifier mask.
	 * @param flag
	 * 		Flag to remove from the mask.
	 *
	 * @return The mask without the flag.
	 */
	static int remove(int mask, int flag) {
		return mask & ~flag;
	}

	/**
	 * @param mask
	 * 		Modifier mask.
	 * @param flags
	 * 		Flags to check.
	 *
	 * @return {@code true} when the mask contains each flag.
	 */
	static boolean hasAll(int mask, int... flags) {
		for (int flag : flags)
			if ((mask & flag) == 0)
				return false;
		return true;
	}

	/**
	 * @param mask
	 * 		Modifier mask.
	 * @param flags
	 * 		Flags to check.
	 *
	 * @return {@code true} when the mask contains any of the flags.
	 */
	static boolean hasAny(int mask, int... flags) {
		for (int flag : flags)
			if ((mask & flag) != 0)
				return true;
		return false;
	}

	/**
	 * @param flags
	 * 		Flags to use.
	 *
	 * @return Mask containing the each flag.
	 */
	static int createMask(int... flags) {
		int acc = 0;
		for (int flag : flags) acc |= flag;
		return acc;
	}
}