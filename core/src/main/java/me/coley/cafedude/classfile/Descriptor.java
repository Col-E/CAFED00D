package me.coley.cafedude.classfile;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Descriptor parsing utility.
 *
 * @author Matt Coley
 */
public class Descriptor {
	public static final Descriptor VOID = new Descriptor(Kind.PRIMITIVE, "V");
	public static final Descriptor BOOLEAN = new Descriptor(Kind.PRIMITIVE, "Z");
	public static final Descriptor BYTE = new Descriptor(Kind.PRIMITIVE, "B");
	public static final Descriptor CHAR = new Descriptor(Kind.PRIMITIVE, "C");
	public static final Descriptor INT = new Descriptor(Kind.PRIMITIVE, "I");
	public static final Descriptor FLOAT = new Descriptor(Kind.PRIMITIVE, "F");
	public static final Descriptor DOUBLE = new Descriptor(Kind.PRIMITIVE, "D");
	public static final Descriptor LONG = new Descriptor(Kind.PRIMITIVE, "J");
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
			int start = 1;
			int stop = 1;
			int max = desc.indexOf(')');
			List<Descriptor> list = new ArrayList<>();
			while (start < max) {
				stop++;
				String section = desc.substring(start, stop);
				if (isPrimitive(desc)) {
					list.add(from(section));
				} else {
					char first = section.charAt(0);
					if (first == '[') {
						int i = 1;
						while (i < stop && section.charAt(i) == '[')
							i++;
						char elementStart = desc.charAt(start + i);
						if (isPrimitive(elementStart))
							stop = start + i;
						else
							stop = desc.indexOf(';', start + 1) + 1;
						section = desc.substring(start, stop);
						list.add(new Descriptor(Kind.ARRAY, section, i));
					} else if (first == 'L' && (start + 1 < max)) {
						stop = desc.indexOf(';', start + 1) + 1;
						section = desc.substring(start, stop);
						list.add(new Descriptor(Kind.OBJECT, section));
					} else {
						list.add(new Descriptor(Kind.ILLEGAL, section));
					}
				}
				start = stop;
			}
			return list;
		}
		return Collections.singletonList(this);
	}

	/**
	 * @return Number of parameters the descriptor as, assuming it is a {@link Kind#METHOD}.
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
					while ((c = desc.charAt(current++)) == '[') ;
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

	/**
	 * @param desc
	 * 		Descriptor to parse.
	 *
	 * @return Descriptor object instance.
	 */
	public static Descriptor from(String desc) {
		if (desc == null || desc.trim().isEmpty())
			return null;
		switch (desc) {
			case "V":
				return VOID;
			case "Z":
				return BOOLEAN;
			case "B":
				return BYTE;
			case "C":
				return CHAR;
			case "I":
				return INT;
			case "F":
				return FLOAT;
			case "D":
				return DOUBLE;
			case "J":
				return LONG;
			default:
				char first = desc.charAt(0);
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
					if (from(d.getReturnDesc().desc).kind == Kind.ILLEGAL)
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

	public static Descriptor from(@NotNull Class<?> clazz) {
		String descriptor = clazz.toGenericString().replace('.', '/');
		if (clazz.isArray()) {
			return from(descriptor);
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
			return from("L" + descriptor + ";");
		}
	}

	/**
	 * @param desc
	 * 		Descriptor to check.
	 *
	 * @return {@code true} if it denotes a primitive.
	 */
	public static boolean isPrimitive(String desc) {
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
		PRIMITIVE,
		OBJECT,
		ARRAY,
		METHOD,
		ILLEGAL
	}
}
