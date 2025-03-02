package software.coley.cafedude.classfile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Descriptor parsing utility.
 *
 * @author Matt Coley
 */
public class Descriptor {
	/** Descriptor of a {@code void} primitive. */
	public static final Descriptor VOID = new Descriptor(Kind.PRIMITIVE, "V");
	/** Descriptor of a {@code boolean} primitive. */
	public static final Descriptor BOOLEAN = new Descriptor(Kind.PRIMITIVE, "Z");
	/** Descriptor of a {@code byte} primitive. */
	public static final Descriptor BYTE = new Descriptor(Kind.PRIMITIVE, "B");
	/** Descriptor of a {@code char} primitive. */
	public static final Descriptor CHAR = new Descriptor(Kind.PRIMITIVE, "C");
	/** Descriptor of a {@code short} primitive. */
	public static final Descriptor SHORT = new Descriptor(Kind.PRIMITIVE, "S");
	/** Descriptor of an {@code int} primitive. */
	public static final Descriptor INT = new Descriptor(Kind.PRIMITIVE, "I");
	/** Descriptor of a {@code float} primitive. */
	public static final Descriptor FLOAT = new Descriptor(Kind.PRIMITIVE, "F");
	/** Descriptor of a {@code double} primitive. */
	public static final Descriptor DOUBLE = new Descriptor(Kind.PRIMITIVE, "D");
	/** Descriptor of a {@code long} primitive. */
	public static final Descriptor LONG = new Descriptor(Kind.PRIMITIVE, "J");
	/** Descriptor of an {@code Object}. */
	public static final Descriptor OBJECT = new Descriptor(Kind.OBJECT, "Ljava/lang/Object;");
	//
	private final Kind kind;
	private final String desc;
	private final int arrayLevel;

	private Descriptor(Kind kind, String desc) {
		this(kind, desc, 0);
	}

	private Descriptor(Kind kind, String desc, int arrayLevel) {
		this.kind = kind;
		this.desc = desc;
		this.arrayLevel = arrayLevel;
	}

	/**
	 * Example: {@code [I} would yield {@code I}.
	 *
	 * @return Element desc of an {@link Kind#ARRAY array} descriptor.
	 * Otherwise, self.
	 */
	public Descriptor getElementDesc() {
		if (arrayLevel == 0)
			return this;
		else
			return from(desc.substring(arrayLevel));
	}

	/**
	 * @return Return desc of a {@link Kind#METHOD} descriptor.
	 * Otherwise, singleton list of self.
	 */
	public List<Descriptor> getParameters() {
		if (kind == Kind.METHOD) {
			int current = 1;
			int max = desc.indexOf(')');
			if (max == 1)
				return Collections.emptyList();
			List<Descriptor> list = new ArrayList<>();
			while (current < max) {
				char c = desc.charAt(current);
				if (isPrimitive(c)) {
					list.add(Descriptor.from(c));
					current++;
				} else if (c == 'L') {
					int end = desc.indexOf(';', current + 2);
					if (end < 0)
						// Error: No end to the object descriptor
						return list;
					list.add(Descriptor.from(desc.substring(current, end + 1)));
					current = end + 1;
				} else if (c == '[') {
					int start = current;
					while ((c = desc.charAt(++current)) == '[') ;
					if (isPrimitive(c)) {
						current++;
					} else if (c == 'L') {
						int end = desc.indexOf(';', current + 2);
						if (end < 0)
							// Error: No end to the object descriptor
							return list;
						current = end + 1;
					} else {
						// Array element type is not a primitive, object start 'L', or array start '['
						return list;
					}
					list.add(Descriptor.from(desc.substring(start, current)));
				} else {
					// Not a primitive, object start 'L', or array start '['
					return list;
				}
			}
			return list;
		}
		return Collections.singletonList(this);
	}

	/**
	 * @return Number of parameters the descriptor has, assuming it is a {@link Kind#METHOD}.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	public int getParameterCount() {
		if (kind == Kind.METHOD) {
			int count = 0;
			int current = 1;
			int max = desc.indexOf(')');
			while (current < max) {
				char c = desc.charAt(current);
				if (isPrimitive(c)) {
					count++;
					current++;
				} else if (c == 'L') {
					count++;
					int end = desc.indexOf(';', current + 2);
					if (end < 0)
						return -1;
					current = end + 1;
				} else if (c == '[') {
					while ((c = desc.charAt(++current)) == '[') ;
					if (isPrimitive(c)) {
						current++;
						count++;
					} else if (c == 'L') {
						count++;
						int end = desc.indexOf(';', current + 2);
						if (end < 0)
							return -1;
						current = end + 1;
					} else {
						return -1;
					}
				} else {
					// Should not happen
					return -1;
				}
			}
			return count;
		}
		return -1;
	}

	/**
	 * @return Size of parameters the descriptor has, assuming it is a {@link Kind#METHOD}.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	public int getParameterSize() {
		if (kind == Kind.METHOD) {
			int size = 0;
			int current = 1;
			int max = desc.indexOf(')');
			while (current < max) {
				char c = desc.charAt(current);
				if (isPrimitive(c)) {
					size += (c == 'J' || c == 'D') ? 2 : 1;
					current++;
				} else if (c == 'L') {
					size++;
					int end = desc.indexOf(';', current + 2);
					if (end < 0)
						return -1;
					current = end + 1;
				} else if (c == '[') {
					while ((c = desc.charAt(++current)) == '[') ;
					if (isPrimitive(c)) {
						current++;
						size++;
					} else if (c == 'L') {
						size++;
						int end = desc.indexOf(';', current + 2);
						if (end < 0)
							return -1;
						current = end + 1;
					} else {
						return -1;
					}
				} else {
					// Should not happen
					return -1;
				}
			}
			return size;
		}
		return -1;
	}

	/**
	 * @return Return desc of a {@link Kind#METHOD} descriptor.
	 * Otherwise, self.
	 */
	public Descriptor getReturnDesc() {
		if (kind == Kind.METHOD)
			return from(desc.substring(desc.indexOf(')') + 1));
		return this;
	}

	/**
	 * @return {@code true} when the descriptor denotes a wide primitive desc <i>(double/long)</i>.
	 */
	public boolean isWide() {
		if (kind == Kind.PRIMITIVE) {
			char c = desc.charAt(0);
			return c == 'J' || c == 'D';
		}
		return false;
	}

	/**
	 * @return String contents of this desc object.
	 */
	public String getDescriptor() {
		return desc;
	}

	/**
	 * @return Descriptor kind.
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * @return Array level.
	 */
	public int getArrayLevel() {
		return arrayLevel;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Descriptor that = (Descriptor) o;

		if (arrayLevel != that.arrayLevel) return false;
		if (kind != that.kind) return false;
		return desc.equals(that.desc);
	}

	@Override
	public int hashCode() {
		int result = kind.hashCode();
		result = 31 * result + desc.hashCode();
		result = 31 * result + arrayLevel;
		return result;
	}

	@Override
	public String toString() {
		return "Descriptor{" +
				"kind=" + kind +
				", desc='" + desc + '\'' +
				", arrayLevel=" + arrayLevel +
				'}';
	}

	/**
	 * @param desc
	 * 		Descriptor to parse.
	 *
	 * @return Descriptor object instance.
	 */
	@Nullable
	public static Descriptor from(@Nullable String desc) {
		if (desc == null || desc.trim().isEmpty())
			return null;
		char first = desc.charAt(0);
		switch (first) {
			case 'V':
				return VOID;
			case 'Z':
				return BOOLEAN;
			case 'B':
				return BYTE;
			case 'C':
				return CHAR;
			case 'S':
				return SHORT;
			case 'I':
				return INT;
			case 'F':
				return FLOAT;
			case 'D':
				return DOUBLE;
			case 'J':
				return LONG;
			default:
				if (first == '[') {
					int max = desc.length();
					int i = 1;
					while (i < max && desc.charAt(i) == '[')
						i++;

					// Validate the element type is legitimate
					Descriptor d = from(desc.substring(i));
					if (d == null || d.kind == Kind.ILLEGAL)
						return new Descriptor(Kind.ILLEGAL, desc);
					return new Descriptor(Kind.ARRAY, desc, i);
				} else if (first == '(') {
					// Validate closing ')' exists and isn't the last char
					int end = desc.indexOf(')');
					if (end < 0 || end == desc.length() - 1)
						return new Descriptor(Kind.ILLEGAL, desc);
					Descriptor d = new Descriptor(Kind.METHOD, desc);

					// Validate return type
					Descriptor returnDesc = d.getReturnDesc();
					if (returnDesc.kind == Kind.ILLEGAL)
						return new Descriptor(Kind.ILLEGAL, desc);

					// Validate parameter count
					if (d.getParameterCount() < 0)
						return new Descriptor(Kind.ILLEGAL, desc);
					return d;
				} else if (first == 'L') {
					int end = desc.indexOf(';');

					// Validate ';' exists and there is at least one char between 'L' and ';'
					if (end < 0 || end == 1)
						return new Descriptor(Kind.ILLEGAL, desc);
					return new Descriptor(Kind.OBJECT, desc);
				} else {
					return new Descriptor(Kind.ILLEGAL, desc);
				}
		}
	}

	/**
	 * @param desc
	 * 		Descriptor to parse.
	 *
	 * @return Primitive descriptor instance or and {@link Kind#ILLEGAL} if the char is not a recognized primitive type.
	 */
	@Nonnull
	public static Descriptor from(char desc) {
		switch (desc) {
			case 'V':
				return VOID;
			case 'Z':
				return BOOLEAN;
			case 'B':
				return BYTE;
			case 'C':
				return CHAR;
			case 'S':
				return SHORT;
			case 'I':
				return INT;
			case 'F':
				return FLOAT;
			case 'D':
				return DOUBLE;
			case 'J':
				return LONG;
			default:
				return new Descriptor(Kind.ILLEGAL, String.valueOf(desc));
		}
	}

	/**
	 * Get a descriptor from a java class.
	 *
	 * @param clazz
	 * 		Class to get descriptor from.
	 *
	 * @return Descriptor object instance.
	 */
	@Nonnull
	public static Descriptor from(@Nonnull Class<?> clazz) {
		String descriptor = clazz.toGenericString().replace('.', '/');
		if (clazz.isArray()) {
			return Objects.requireNonNull(from(descriptor), "Failed to parse array descriptor from class reference");
		} else if (clazz.isPrimitive()) {
			switch (descriptor) {
				case "void":
					return VOID;
				case "boolean":
					return BOOLEAN;
				case "byte":
					return BYTE;
				case "char":
					return CHAR;
				case "short":
					return SHORT;
				case "int":
					return INT;
				case "float":
					return FLOAT;
				case "double":
					return DOUBLE;
				case "long":
					return LONG;
				default:
					throw new IllegalArgumentException("Unknown primitive type: " + descriptor);
			}
		} else {
			return Objects.requireNonNull(from("L" + descriptor + ";"), "Failed to parse object descriptor from class reference");
		}
	}

	/**
	 * @param desc
	 * 		Descriptor to check.
	 *
	 * @return {@code true} if it denotes a primitive.
	 */
	public static boolean isPrimitive(@Nullable String desc) {
		if (desc == null || desc.length() != 1)
			return false;
		return isPrimitive(desc.charAt(0));
	}

	/**
	 * @param desc
	 * 		Descriptor to check.
	 *
	 * @return {@code true} if it denotes a primitive.
	 */
	public static boolean isPrimitive(char desc) {
		switch (desc) {
			case 'V':
			case 'Z':
			case 'B':
			case 'C':
			case 'S':
			case 'I':
			case 'F':
			case 'D':
			case 'J':
				return true;
			default:
				return false;
		}
	}

	/**
	 * Descriptor kind.
	 */
	public enum Kind {
		/** Descriptor is of a primitive type. */
		PRIMITIVE,
		/** Descriptor is of an object type. */
		OBJECT,
		/** Descriptor is of an array type. The component can be a primitive or object. */
		ARRAY,
		/** Descriptor is of a method type. */
		METHOD,
		/** Descriptor is malformed. */
		ILLEGAL
	}
}
