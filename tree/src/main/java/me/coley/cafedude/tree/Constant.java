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

	public static Constant of(float value) {
		return new Constant(Type.FLOAT, value);
	}

	public static Constant of(double value) {
		return new Constant(Type.DOUBLE, value);
	}

	public static Constant of(long value) {
		return new Constant(Type.LONG, value);
	}

	public static Constant of(int value) {
		return new Constant(Type.INT, value);
	}

	public static Constant of(String value) {
		return new Constant(Type.STRING, value);
	}

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
