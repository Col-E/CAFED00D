package software.coley.cafedude.classfile.constant;

/**
 * Integer pool entry.
 *
 * @author Matt Coley
 */
public non-sealed class CpInt extends CpEntry implements LoadableConstant {
	private int value;

	/**
	 * @param value
	 * 		Constant value.
	 */
	public CpInt(int value) {
		super(INTEGER);
		this.value = value;
	}

	/**
	 * @return Constant value.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value
	 * 		New constant value.
	 */
	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpInt cpInt = (CpInt) o;
		return value == cpInt.value;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(value);
	}

	@Override
	public String toString() {
		return "int=" + value;
	}
}
