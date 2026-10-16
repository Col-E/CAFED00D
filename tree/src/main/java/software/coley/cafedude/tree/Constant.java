package software.coley.cafedude.tree;

import software.coley.cafedude.classfile.Descriptor;

import jakarta.annotation.Nonnull;

/**
 * Constant object with type and value.
 *
 * @author Justus Garbe
 */
public class Constant {
	private final Type type;
	private final Object value;

	/**
	 * Construct a constant.
	 *
	 * @param type
	 * 		Type of constant.
	 * @param value
	 * 		Value of constant.
	 */
	private Constant(@Nonnull Type type, @Nonnull Object value) {
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
	 *
	 * @return Constant.
	 */
	@Nonnull
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
	 *
	 * @return Constant.
	 */
	@Nonnull
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
	 *
	 * @return Constant.
	 */
	@Nonnull
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
	 *
	 * @return Constant.
	 */
	@Nonnull
	public static Constant of(short value) {
		return new Constant(Type.SHORT, value);
	}

	/**
	 * Create a constant from a {@code float} value.
	 * <p>
	 * Corresponds to {@code CpFloat}.
	 *
	 * @param value
	 * 		Float value.
	 *
	 * @return Constant.
	 */
	@Nonnull
	public static Constant of(float value) {
		return ofFloatBits(Float.floatToRawIntBits(value));
	}

	/**
	 * Create a constant from the raw IEEE 754 bits of a {@code float} value.
	 * Preserves NaN sign and payload.
	 * <p>
	 * Corresponds to {@code CpFloat}.
	 *
	 * @param rawBits
	 * 		Raw bits of the float value, see {@link Float#floatToRawIntBits(float)}.
	 *
	 * @return Constant.
	 */
	@Nonnull
	public static Constant ofFloatBits(int rawBits) {
		return new Constant(Type.FLOAT, rawBits);
	}

	/**
	 * Create a constant from a {@code double} value.
	 * <p>
	 * Corresponds to {@code CpDouble}.
	 *
	 * @param value
	 * 		Double value.
	 *
	 * @return Constant.
	 */
	@Nonnull
	public static Constant of(double value) {
		return ofDoubleBits(Double.doubleToRawLongBits(value));
	}

	/**
	 * Create a constant from the raw IEEE 754 bits of a {@code double} value.
	 * Preserves NaN sign and payload.
	 * <p>
	 * Corresponds to {@code CpDouble}.
	 *
	 * @param rawBits
	 * 		Raw bits of the double value, see {@link Double#doubleToRawLongBits(double)}.
	 *
	 * @return Constant.
	 */
	@Nonnull
	public static Constant ofDoubleBits(long rawBits) {
		return new Constant(Type.DOUBLE, rawBits);
	}

	/**
	 * Create a constant from a {@code long} value.
	 * <p>
	 * Corresponds to {@code CpLong}.
	 *
	 * @param value
	 * 		Long value.
	 *
	 * @return Constant.
	 */
	@Nonnull
	public static Constant of(long value) {
		return new Constant(Type.LONG, value);
	}

	/**
	 * Create a constant from a {@code int} value.
	 * <p>
	 * Corresponds to {@code CpInt}.
	 *
	 * @param value
	 * 		Integer value.
	 *
	 * @return Constant.
	 */
	@Nonnull
	public static Constant of(int value) {
		return new Constant(Type.INT, value);
	}

	/**
	 * Create a constant from a {@code String} value.
	 * <p>
	 * Corresponds to {@code CpUtf8}.
	 *
	 * @param value
	 * 		String value.
	 *
	 * @return Constant.
	 */
	@Nonnull
	public static Constant of(@Nonnull String value) {
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
	 * Any other kind does not have a corresponding constant type.
	 *
	 * @param value
	 * 		Descriptor value.
	 *
	 * @return Constant.
	 *
	 * @throws IllegalArgumentException
	 * 		If the descriptor kind is not {@link Descriptor.Kind#OBJECT},
	 *        {@link Descriptor.Kind#ARRAY} or {@link Descriptor.Kind#METHOD}.
	 */
	@Nonnull
	public static Constant of(@Nonnull Descriptor value) {
		if (value.getKind() == Descriptor.Kind.OBJECT
				|| value.getKind() == Descriptor.Kind.ARRAY
				|| value.getKind() == Descriptor.Kind.METHOD) {
			return new Constant(Type.DESCRIPTOR, value);
		} else {
			throw new IllegalArgumentException("Invalid descriptor kind: " + value.getKind());
		}
	}

	/**
	 * Create a constant from a {@link Handle} value.
	 * <p>
	 * Corresponds to {@code CpMethodHandle}.
	 *
	 * @param value
	 * 		Handle value.
	 *
	 * @return Constant.
	 */
	@Nonnull
	public static Constant of(@Nonnull Handle value) {
		return new Constant(Type.HANDLE, value);
	}

	/**
	 * @return Type of constant.
	 */
	@Nonnull
	public Type getType() {
		return type;
	}

	/**
	 * Converting NaN bit patterns to a {@code float} or {@code double} is not guaranteed to preserve their payload on
	 * every platform. Use {@link #getFloatBits()} or {@link #getDoubleBits()} when the exact encoding matters.
	 *
	 * @return Value of constant. A {@link Float} or {@link Double} for float and double constants.
	 */
	@Nonnull
	public Object getValue() {
		switch (type) {
			case FLOAT:
				return Float.intBitsToFloat(getFloatBits());
			case DOUBLE:
				return Double.longBitsToDouble(getDoubleBits());
			default:
				return value;
		}
	}

	/**
	 * @return Raw IEEE 754 bits of a float constant, see {@link Float#floatToRawIntBits(float)}.
	 *
	 * @throws IllegalStateException
	 * 		If this is not a float constant.
	 */
	public int getFloatBits() {
		if (type != Type.FLOAT)
			throw new IllegalStateException("Not a float constant: " + type);
		return (int) value;
	}

	/**
	 * @return Raw IEEE 754 bits of a double constant, see {@link Double#doubleToRawLongBits(double)}.
	 *
	 * @throws IllegalStateException
	 * 		If this is not a double constant.
	 */
	public long getDoubleBits() {
		if (type != Type.DOUBLE)
			throw new IllegalStateException("Not a double constant: " + type);
		return (long) value;
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
		/**
		 * {@link Handle}
		 */
		HANDLE
	}
}
