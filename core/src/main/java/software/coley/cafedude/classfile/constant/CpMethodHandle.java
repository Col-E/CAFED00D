package software.coley.cafedude.classfile.constant;

import javax.annotation.Nonnull;

/**
 * MethodHandle pool entry. Holds a byte to indicate behavior and points to a relevant reference constant
 * based on the byte's value.
 *
 * @author Matt Coley
 */
public class CpMethodHandle extends CpEntry {
	private ConstRef reference;
	private byte kind;

	/**
	 * @param kind
	 * 		Byte indicating handle behavior.
	 * @param reference
	 * 		Constant pool entry holding the method handle's {@link ConstRef reference}.
	 * 		Reference type depends on the byte value.
	 */
	public CpMethodHandle(byte kind, @Nonnull ConstRef reference) {
		super(METHOD_HANDLE);
		this.kind = kind;
		this.reference = reference;
	}

	/**
	 * @return Byte indicating handle behavior.
	 *
	 * @see CpMethodHandleConstants
	 */
	public byte getKind() {
		return kind;
	}

	/**
	 * @param kind
	 * 		New behavior indicating byte.
	 *
	 * @see CpMethodHandleConstants
	 */
	public void setKind(byte kind) {
		this.kind = kind;
	}

	/**
	 * @return Constant pool entry holding the method handle's {@link ConstRef reference}.
	 * Reference type depends on the byte value.
	 *
	 * @see #getKind()
	 */
	@Nonnull
	public ConstRef getReference() {
		return reference;
	}

	/**
	 * @param reference
	 * 		New constant pool entry holding the method handle's {@link ConstRef reference}.
	 *
	 * @see #getKind()
	 */
	public void setReference(@Nonnull ConstRef reference) {
		this.reference = reference;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CpMethodHandle)) return false;

		CpMethodHandle that = (CpMethodHandle) o;

		if (kind != that.kind) return false;
		return reference.equals(that.reference);
	}

	@Override
	public int hashCode() {
		int result = reference.hashCode();
		result = 31 * result + (int) kind;
		return result;
	}
}
