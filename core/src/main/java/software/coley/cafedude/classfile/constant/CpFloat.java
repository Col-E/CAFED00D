package software.coley.cafedude.classfile.constant;

/**
 * Float pool entry.
 * <p>
 * The value is held as its raw IEEE 754 bit pattern so that NaN sign and payload survive a round trip.
 *
 * @author Matt Coley
 */
public non-sealed class CpFloat extends CpEntry implements LoadableConstant {
	private int bits;

	/**
	 * @param value
	 * 		Constant value.
	 */
	public CpFloat(float value) {
		this(Float.floatToRawIntBits(value));
	}

	private CpFloat(int rawBits) {
		super(FLOAT);
		this.bits = rawBits;
	}

	/**
	 * Creates an entry with exactly the given bit pattern, including NaN sign and payload.
	 *
	 * @param rawBits
	 * 		Raw IEEE 754 bits of the constant, see {@link Float#floatToRawIntBits(float)}.
	 *
	 * @return Entry holding the given bits.
	 */
	public static CpFloat fromRawBits(int rawBits) {
		return new CpFloat(rawBits);
	}

	/**
	 * Converting NaN bit patterns to a {@code float} is not guaranteed to preserve their payload on every platform.
	 * Use {@link #getRawBits()} when the exact encoding matters.
	 *
	 * @return Constant value.
	 */
	public float getValue() {
		return Float.intBitsToFloat(bits);
	}

	/**
	 * @return Raw IEEE 754 bits of the constant, see {@link Float#floatToRawIntBits(float)}.
	 */
	public int getRawBits() {
		return bits;
	}

	/**
	 * @param value
	 * 		New constant value.
	 */
	public void setValue(float value) {
		this.bits = Float.floatToRawIntBits(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpFloat cpFloat = (CpFloat) o;
		return bits == cpFloat.bits;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(bits);
	}

	@Override
	public String toString() {
		return "float=" + getValue();
	}
}
