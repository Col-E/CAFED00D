package me.coley.cafedude.classfile.annotation;

/**
 * Indicates purpose of the associated {@link TypePathElement}.
 *
 * @author Matt Coley
 */
public enum TypePathKind {
	ARRAY_DEEPER(0),
	NESTED_DEEPER(1),
	WILDCARD_BOUND(2),
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
