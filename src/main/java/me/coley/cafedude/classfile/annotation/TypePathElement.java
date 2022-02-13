package me.coley.cafedude.classfile.annotation;

/**
 * Type path item.
 *
 * @author Matt Coley
 */
public class TypePathElement {
	private final TypePathKind kind;
	private final int argIndex;

	/**
	 * @param kind
	 * 		Indicator of purpose of the element.
	 * @param argIndex
	 * 		Which type argument of a parameterized type is annotated.
	 */
	public TypePathElement(TypePathKind kind, int argIndex) {
		this.kind = kind;
		// Argument indices only allowed for type argument kinds
		if (kind != TypePathKind.TYPE_ARGUMENT && argIndex != 0)
			throw new IllegalArgumentException("Type path kind was not TYPE_ARGUMENT " +
					"but gave a non-zero type_argument_index");
		this.argIndex = argIndex;
	}

	/**
	 * @return Indicator of purpose of the element.
	 */
	public TypePathKind getKind() {
		return kind;
	}

	/**
	 * @return Which type argument of a parameterized type is annotated.
	 */
	public int getArgIndex() {
		return argIndex;
	}
}
