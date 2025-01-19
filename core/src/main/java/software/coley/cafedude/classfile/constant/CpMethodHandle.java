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
	 * Behavior as of Java 14.
	 * <table>
	 * <thead>
	 * <tr>
	 * <th>Value</th>
	 * <th>Description</th>
	 * <th>Interpretation</th>
	 * </tr>
	 * </thead>
	 * <tbody><tr>
	 * <td>1</td>
	 * <td>REF_getField</td>
	 * <td>getfield C.f:T</td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td>REF_getStatic</td>
	 * <td>getstatic C.f:T</td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td>REF_putField</td>
	 * <td>putfield C.f:T</td>
	 * </tr>
	 * <tr>
	 * <td>4</td>
	 * <td>REF_putStatic</td>
	 * <td>putstatic C.f:T</td>
	 * </tr>
	 * <tr>
	 * <td>5</td>
	 * <td>REF_invokeVirtual</td>
	 * <td>invokevirtual C.m:(A*)T</td>
	 * </tr>
	 * <tr>
	 * <td>6</td>
	 * <td>REF_invokeStatic</td>
	 * <td>invokestatic C.m:(A*)T</td>
	 * </tr>
	 * <tr>
	 * <td>7</td>
	 * <td>REF_invokeSpecial</td>
	 * <td>invokespecial C.m:(A*)T</td>
	 * </tr>
	 * <tr>
	 * <td>8</td>
	 * <td>REF_newInvokeSpecial</td>
	 * <td>new C; dup; invokespecial C.:(A*)V</td>
	 * </tr>
	 * <tr>
	 * <td>9</td>
	 * <td>REF_invokeInterface</td>
	 * <td>invokeinterface C.m:(A*)T</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @return Byte indicating handle behavior.
	 */
	public byte getKind() {
		return kind;
	}

	/**
	 * @param kind
	 * 		New behavior indicating byte.
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
