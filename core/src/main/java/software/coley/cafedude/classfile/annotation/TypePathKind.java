package software.coley.cafedude.classfile.annotation;

import javax.annotation.Nonnull;

/**
 * Indicates purpose of the associated {@link TypePathElement}.
 *
 * @author Matt Coley
 */
public enum TypePathKind {
	/** Annotation is deeper in an array type. */
	ARRAY_DEEPER(0),
	/** Annotation is deeper in a nested type. */
	NESTED_DEEPER(1),
	/** Annotation is on the bound of a wildcard type argument of a parameterized type. */
	WILDCARD_BOUND(2),
	/** Annotation is on a type argument of a parameterized type. */
	TYPE_ARGUMENT(3);

	private final int value;

	TypePathKind(int value) {
		this.value = value;
	}

	/**
	 * @return Backing value.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value
	 * 		Backing value.
	 *
	 * @return Enum kind instance.
	 */
	@Nonnull
	public static TypePathKind fromValue(int value) {
		switch (value) {
			case 0:
				return ARRAY_DEEPER;
			case 1:
				return NESTED_DEEPER;
			case 2:
				return WILDCARD_BOUND;
			case 3:
				return TYPE_ARGUMENT;
			default:
				throw new IllegalArgumentException("Invalid type path kind: " + value);
		}
	}
}
