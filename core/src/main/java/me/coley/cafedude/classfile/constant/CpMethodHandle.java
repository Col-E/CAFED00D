package me.coley.cafedude.classfile.constant;

/**
 * MethodHandle pool entry. Holds a byte to indicate behavior and points to a relevant reference constant
 * based on the byte's value.
 *
 * @author Matt Coley
 */
public class CpMethodHandle extends ConstPoolEntry {
	private int referenceIndex;
	private byte kind;

	/**
	 * @param kind
	 * 		Byte indicating handle behavior.
	 * @param referenceIndex
	 * 		Index of handle's {@link ConstRef reference} in pool.
	 * 		Reference type depends on the byte value.
	 */
	public CpMethodHandle(byte kind, int referenceIndex) {
		super(METHOD_HANDLE);
		this.kind = kind;
		this.referenceIndex = referenceIndex;
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
	 * </tbody></table>
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
	 * @return Index of handle's {@link ConstRef reference} in pool.
	 * Reference type depends on the byte value.
	 *
	 * @see #getKind()
	 */
	public int getReferenceIndex() {
		return referenceIndex;
	}

	/**
	 * @param referenceIndex
	 * 		New index of handle's {@link ConstRef reference} in pool.
	 * 		Reference type depends on the byte value.
	 *
	 * @see #getKind()
	 */
	public void setReferenceIndex(int referenceIndex) {
		this.referenceIndex = referenceIndex;
	}
}
