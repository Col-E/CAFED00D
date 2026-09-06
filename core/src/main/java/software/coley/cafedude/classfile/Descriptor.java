package software.coley.cafedude.classfile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	private Descriptor(@Nonnull Kind kind, @Nonnull String desc) {
		this(kind, desc, 0);
	}

	private Descriptor(@Nonnull Kind kind, @Nonnull String desc, int arrayLevel) {
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
	@Nonnull
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
	@Nonnull
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
	@Nonnull
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
	 * Can be empty {@code ""} if the {@link #getKind() kind} is {@link Kind#ILLEGAL}.
	 */
	@Nonnull
	public String getDescriptor() {
		return desc;
	}

	/**
	 * @return Descriptor kind.
	 */
	@Nonnull
	public Kind getKind() {
		return kind;
	}

	/**
	 * @return Array level. Will be {@code 0} for any non-array descriptor.
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
	@Nonnull
	public static Descriptor from(@Nullable String desc) {
		if (desc == null)
			return new Descriptor(Kind.ILLEGAL, "");
		if (desc.isEmpty())
			return new Descriptor(Kind.ILLEGAL, desc);

		// 'V' is the complete void return descriptor.
		if (desc.equals("V"))
			return VOID;
		if (desc.charAt(0) == '(')
			return parseMethod(desc);

		// Otherwise it must be a field type: primitive, object, or array.
		int[] cursor = {0};
		Descriptor parsed = parseFieldType(desc, cursor);
		if (parsed == null || cursor[0] != desc.length())
			return new Descriptor(Kind.ILLEGAL, desc);
		return parsed;
	}

	/**
	 * Parses a complete method descriptor {@code (parameters)return}.
	 *
	 * @param desc
	 * 		Descriptor to parse. Must start with {@code (}.
	 *
	 * @return Method descriptor, or {@link Kind#ILLEGAL} when malformed.
	 */
	@Nonnull
	private static Descriptor parseMethod(@Nonnull String desc) {
		int[] cursor = {1};  // skip '('
		int units = 0;

		// Parse parameters until the closing ')'.
		while (true) {
			if (cursor[0] >= desc.length())
				return new Descriptor(Kind.ILLEGAL, desc);  // missing ')'
			char c = desc.charAt(cursor[0]);
			if (c == ')') {
				cursor[0]++;
				break;
			}
			Descriptor param = parseFieldType(desc, cursor);
			if (param == null)
				return new Descriptor(Kind.ILLEGAL, desc);
			units += param.isWide() ? 2 : 1;
			if (units > 255)
				return new Descriptor(Kind.ILLEGAL, desc); // JVMS limits parameter units to 255
		}

		// Parse the return descriptor: 'V' or a field type.
		if (cursor[0] >= desc.length())
			return new Descriptor(Kind.ILLEGAL, desc); // missing return
		if (desc.charAt(cursor[0]) == 'V')
			cursor[0]++;
		else if (parseFieldType(desc, cursor) == null)
			return new Descriptor(Kind.ILLEGAL, desc);

		// The whole string must have been consumed.
		if (cursor[0] != desc.length())
			return new Descriptor(Kind.ILLEGAL, desc); // trailing data

		return new Descriptor(Kind.METHOD, desc);
	}

	/**
	 * Parses a field type (primitive, object, or array) at the cursor, advancing it past the type.
	 * Void ({@code V}) is not a field type and is rejected here.
	 *
	 * @param desc
	 * 		Descriptor being parsed.
	 * @param cursor
	 * 		Parse position. Advanced past the field type on success.
	 *
	 * @return Parsed descriptor, or {@code null} when the input is not a field type.
	 */
	@Nullable
	private static Descriptor parseFieldType(@Nonnull String desc, @Nonnull int[] cursor) {
		int i = cursor[0];
		if (i >= desc.length())
			return null;
		char c = desc.charAt(i);
		if (c == 'L') {
			int end = desc.indexOf(';', i + 1);
			if (end < 0 || end == i + 1)
				return null; // missing terminator or empty class name
			if (!isValidClassName(desc.substring(i + 1, end)))
				return null; // illegal characters in class name
			cursor[0] = end + 1;
			return new Descriptor(Kind.OBJECT, desc.substring(i, end + 1));
		}
		if (c == '[') {
			int start = i;
			int depth = 0;
			while (i < desc.length() && desc.charAt(i) == '[') {
				depth++;
				i++;
			}
			if (depth > 255)
				return null; // JVMS limits array descriptors to 255 dimensions
			cursor[0] = i;
			Descriptor component = parseFieldType(desc, cursor);
			if (component == null)
				return null;
			return new Descriptor(Kind.ARRAY, desc.substring(start, cursor[0]), depth);
		}
		if (c == 'V')
			return null; // void is only legal as a return descriptor
		Descriptor primitive = from(c);
		if (primitive.getKind() == Kind.ILLEGAL)
			return null;
		cursor[0] = i + 1;
		return primitive;
	}

	/**
	 * @param name
	 * 		Binary class name in internal form, without the surrounding {@code L...;}.
	 *
	 * @return {@code true} when the name contains none of the source/generic/array characters
	 * that cannot appear in an internal binary name.
	 */
	private static boolean isValidClassName(@Nonnull String name) {
		for (int i = 0; i < name.length(); i++) {
			switch (name.charAt(i)) {
				case '.', '[', '<', '>':
					return false;
				default:
					break;
			}
		}
		return true;
	}

	/**
	 * @param desc
	 * 		Descriptor to parse.
	 *
	 * @return Primitive descriptor instance or and {@link Kind#ILLEGAL} if the char is not a recognized primitive type.
	 */
	@Nonnull
	public static Descriptor from(char desc) {
		return switch (desc) {
			case 'V' -> VOID;
			case 'Z' -> BOOLEAN;
			case 'B' -> BYTE;
			case 'C' -> CHAR;
			case 'S' -> SHORT;
			case 'I' -> INT;
			case 'F' -> FLOAT;
			case 'D' -> DOUBLE;
			case 'J' -> LONG;
			default -> new Descriptor(Kind.ILLEGAL, String.valueOf(desc));
		};
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
		String name = clazz.getName();
		if (clazz.isArray()) {
			// Arrays are almost in the correct form as-is, like: [Ljava.lang.String;
			return from(name.replace('.', '/'));
		} else if (clazz.isPrimitive()) {
			return switch (name) {
				case "void" -> VOID;
				case "boolean" -> BOOLEAN;
				case "byte" -> BYTE;
				case "char" -> CHAR;
				case "short" -> SHORT;
				case "int" -> INT;
				case "float" -> FLOAT;
				case "double" -> DOUBLE;
				case "long" -> LONG;
				default -> throw new IllegalArgumentException("Unknown primitive type: " + name);
			};
		} else {
			return from("L" + name.replace('.', '/') + ";");
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
		return switch (desc) {
			case 'V', 'Z', 'B', 'C', 'S', 'I', 'F', 'D', 'J' -> true;
			default -> false;
		};
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
