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
	 * Create a constant from a {@code float} value.
	 * @param value
	 * 			Float value.
	 * @return Constant.
	 */
	public static Constant of(float value) {
		return new Constant(Type.FLOAT, value);
	}

	/**
	 * Create a constant from a {@code double} value.
	 * @param value
	 * 			Double value.
	 * @return Constant.
	 */
	public static Constant of(double value) {
		return new Constant(Type.DOUBLE, value);
	}

	/**
	 * Create a constant from a {@code long} value.
	 * @param value
	 * 			Long value.
	 * @return Constant.
	 */
	public static Constant of(long value) {
		return new Constant(Type.LONG, value);
	}

	/**
	 * Create a constant from a {@code int} value.
	 * @param value
	 * 			Integer value.
	 * @return Constant.
	 */
	public static Constant of(int value) {
		return new Constant(Type.INT, value);
	}

	/**
	 * Create a constant from a {@code String} value.
	 * @param value
	 * 			String value.
	 * @return Constant.
	 */
	public static Constant of(String value) {
		return new Constant(Type.STRING, value);
	}

	/**
	 * Create a constant from a {@link Descriptor} value.
	 * @param value
	 * 			Descriptor value.
	 * @return Constant.
	 */
	public static Constant of(Descriptor value) {
		return new Constant(Type.DESCRIPTOR, value);
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
		 * {@link Descriptor}
		 */
		DESCRIPTOR,

	}

}
