package software.coley.cafedude.classfile.constant;

/**
 * Double pool entry.
 * <p>
 * The value is held as its raw IEEE 754 bit pattern so that NaN sign and payload survive a round trip.
 *
 * @author Matt Coley
 */
public non-sealed class CpDouble extends CpEntry implements LoadableConstant {
	private long bits;

	/**
	 * @param value
	 * 		Constant value.
	 */
	public CpDouble(double value) {
		this(Double.doubleToRawLongBits(value));
	}

	private CpDouble(long rawBits) {
		super(DOUBLE);
		this.bits = rawBits;
	}

	/**
	 * Creates an entry with exactly the given bit pattern, including NaN sign and payload.
	 *
	 * @param rawBits
	 * 		Raw IEEE 754 bits of the constant, see {@link Double#doubleToRawLongBits(double)}.
	 *
	 * @return Entry holding the given bits.
	 */
	public static CpDouble fromRawBits(long rawBits) {
		return new CpDouble(rawBits);
	}

	/**
	 * Converting NaN bit patterns to a {@code double} is not guaranteed to preserve their payload on every platform.
	 * Use {@link #getRawBits()} when the exact encoding matters.
	 *
	 * @return Constant value.
	 */
	public double getValue() {
		return Double.longBitsToDouble(bits);
	}

	/**
	 * @return Raw IEEE 754 bits of the constant, see {@link Double#doubleToRawLongBits(double)}.
	 */
	public long getRawBits() {
		return bits;
	}

	/**
	 * @param value
	 * 		New constant value.
	 */
	public void setValue(double value) {
		this.bits = Double.doubleToRawLongBits(value);
	}

	@Override
	public boolean isWide() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpDouble cpDouble = (CpDouble) o;
		return bits == cpDouble.bits;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(bits);
	}

	@Override
	public String toString() {
		return "double=" + getValue();
	}
}
