package software.coley.cafedude.classfile.constant;

import software.coley.cafedude.classfile.instruction.Opcodes;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

/**
 * MethodHandle pool entry. Holds a byte to indicate behavior and points to a relevant reference constant
 * based on the byte's value.
 *
 * @author Matt Coley
 */
public non-sealed class CpMethodHandle extends CpEntry implements CrossCpReferencing {
	/** Interpreted as {@link Opcodes#GETFIELD}. */
	public static final int REF_GET_FIELD = 1;
	/** Interpreted as {@link Opcodes#GETSTATIC}. */
	public static final int REF_GET_STATIC = 2;
	/** Interpreted as {@link Opcodes#PUTFIELD}. */
	public static final int REF_PUT_FIELD = 3;
	/** Interpreted as {@link Opcodes#PUTSTATIC}. */
	public static final int REF_PUT_STATIC = 4;
	/** Interpreted as {@link Opcodes#INVOKEVIRTUAL}. */
	public static final int REF_INVOKE_VIRTUAL = 5;
	/** Interpreted as {@link Opcodes#INVOKESTATIC}. */
	public static final int REF_INVOKE_STATIC = 6;
	/** Interpreted as {@link Opcodes#INVOKESPECIAL}. */
	public static final int REF_INVOKE_SPECIAL = 7;
	/** Interpreted as {@link Opcodes#INVOKESPECIAL} following {@link Opcodes#NEW}. */
	public static final int REF_NEW_INVOKE_SPECIAL = 8;
	/** Interpreted as {@link Opcodes#INVOKEINTERFACE}. */
	public static final int REF_INVOKE_INTERFACE = 9;
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

	@Nonnull
	@Override
	public Collection<CpEntry> getReferences() {
		return Collections.singletonList(reference);
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
