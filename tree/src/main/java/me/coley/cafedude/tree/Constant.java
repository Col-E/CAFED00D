package me.coley.cafedude.tree;

import me.coley.cafedude.classfile.Descriptor;

/**
 * Constant object with type and value.
 */
public class Constant {

	private final Type type;
	private final Object value;

	/**
	 * Construct a constant.
	 * @param type
	 * 			Type of constant.
	 * @param value
	 * 			Value of constant.
	 */
	public Constant(Type type, Object value) {
		this.type = type;
		this.value = value;
	}

	/**
	 * Create a constant from a {@code boolean} value.
	 * <p>
	 * Corresponds to {@code CpInt}.
	 *
	 * @param value
	 * 		Boolean value.
	 * @return Constant.
	 */
	public static Constant of(boolean value) {
		return new Constant(Type.BOOLEAN, value);
	}

	/**
	 * Create a constant from a {@code byte} value.
	 * <p>
	 * Corresponds to {@code CpInt}.
	 *
	 * @param value
	 * 		Byte value.
	 * @return Constant.
	 */
	public static Constant of(byte value) {
		return new Constant(Type.BYTE, value);
	}

	/**
	 * Create a constant from a {@code char} value.
	 * <p>
	 * Corresponds to {@code CpInt}.
	 *
	 * @param value
	 * 		Char value.
	 * @return Constant.
	 */
	public static Constant of(char value) {
		return new Constant(Type.CHAR, value);
	}

	/**
	 * Create a constant from a {@code short} value.
	 * <p>
	 * Corresponds to {@code CpInt}.
	 *
	 * @param value
	 * 		Short value.
	 * @return Constant.
	 */
	public static Constant of(short value) {
		return new Constant(Type.SHORT, value);
	}

	/**
	 * Create a constant from a {@code float} value.
	 * <p>
	 * Corresponds to {@code CpFloat}.
	 *
	 * @param value
	 * 			Float value.
	 * @return Constant.
	 */
	public static Constant of(float value) {
		return new Constant(Type.FLOAT, value);
	}

	/**
	 * Create a constant from a {@code double} value.
	 * <p>
	 * Corresponds to {@code CpDouble}.
	 *
	 * @param value
	 * 			Double value.
	 * @return Constant.
	 */
	public static Constant of(double value) {
		return new Constant(Type.DOUBLE, value);
	}

	/**
	 * Create a constant from a {@code long} value.
	 * <p>
	 * Corresponds to {@code CpLong}.
	 *
	 * @param value
	 * 			Long value.
	 * @return Constant.
	 */
	public static Constant of(long value) {
		return new Constant(Type.LONG, value);
	}

	/**
	 * Create a constant from a {@code int} value.
	 * <p>
	 * Corresponds to {@code CpInt}.
	 *
	 * @param value
	 * 			Integer value.
	 * @return Constant.
	 */
	public static Constant of(int value) {
		return new Constant(Type.INT, value);
	}

	/**
	 * Create a constant from a {@code String} value.
	 * <p>
	 * Corresponds to {@code CpUtf8}.
	 *
	 * @param value
	 * 			String value.
	 * @return Constant.
	 */
	public static Constant of(String value) {
		return new Constant(Type.STRING, value);
	}

	/**
	 * Create a constant from a {@link Descriptor} value.
	 * Can have 3 different types:
	 * <ul>
	 * 		<li>{@link Descriptor.Kind#OBJECT} - Corresponds to {@code CpClass}</li>
	 * 		<li>{@link Descriptor.Kind#ARRAY} - Corresponds to {@code CpClass}</li>
	 * 		<li>{@link Descriptor.Kind#METHOD} - Corresponds to {@code CpMethodType}</li>
	 * </ul>
	 * <p>
	 * Any other kind does not have a corresponding constant type.
	 *
	 * @param value
	 * 			Descriptor value.
	 * @return Constant.
	 * @throws IllegalArgumentException If the descriptor kind is not {@link Descriptor.Kind#OBJECT},
	 * 			{@link Descriptor.Kind#ARRAY} or {@link Descriptor.Kind#METHOD}.
	 */
	public static Constant of(Descriptor value) {
		if(value.getKind() == Descriptor.Kind.OBJECT
				|| value.getKind() == Descriptor.Kind.ARRAY
				|| value.getKind() == Descriptor.Kind.METHOD) {
			return new Constant(Type.DESCRIPTOR, value);
		} else {
			throw new IllegalArgumentException("Invalid descriptor kind: " + value.getKind());
		}
	}

	/**
	 * @return Type of constant.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return Value of constant.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Constant type.
	 */
	public enum Type {
		/**
		 * {@link String}
		 */
		STRING,
		/**
		 * {@code boolean}
		 */
		BOOLEAN,
		/**
		 * {@code byte}
		 */
		BYTE,
		/**
		 * {@code char}
		 */
		CHAR,
		/**
		 * {@code short}
		 */
		SHORT,
		/**
		 * {@code int}
		 */
		INT,
		/**
		 * {@code float}
		 */
		FLOAT,
		/**
		 * {@code long}
		 */
		LONG,
		/**
		 * {@code double}
		 */
		DOUBLE,
		/**
		 * {@link Descriptor} ({@link Descriptor.Kind#OBJECT}, {@link Descriptor.Kind#ARRAY}
		 * or {@link Descriptor.Kind#METHOD})
		 */
		DESCRIPTOR,

	}

}
