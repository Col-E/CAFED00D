package software.coley.cafedude.classfile.constant;

import software.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Constants for method references, per JVMS 5.4.3.5-A.
 *
 * @author Matt Coley
 * @see CpMethodHandle#getKind()
 */
public interface CpMethodHandleConstants {
	/** Interpreted as {@link Opcodes#GETFIELD}. */
	int REF_GET_FIELD = 1;
	/** Interpreted as {@link Opcodes#GETSTATIC}. */
	int REF_GET_STATIC = 2;
	/** Interpreted as {@link Opcodes#PUTFIELD}. */
	int REF_PUT_FIELD = 3;
	/** Interpreted as {@link Opcodes#PUTSTATIC}. */
	int REF_PUT_STATIC = 4;
	/** Interpreted as {@link Opcodes#INVOKEVIRTUAL}. */
	int REF_INVOKE_VIRTUAL = 5;
	/** Interpreted as {@link Opcodes#INVOKESTATIC}. */
	int REF_INVOKE_STATIC = 6;
	/** Interpreted as {@link Opcodes#INVOKESPECIAL}. */
	int REF_INVOKE_SPECIAL = 7;
	/** Interpreted as {@link Opcodes#INVOKESPECIAL} following {@link Opcodes#NEW}. */
	int REF_NEW_INVOKE_SPECIAL = 8;
	/** Interpreted as {@link Opcodes#INVOKEINTERFACE}. */
	int REF_INVOKE_INTERFACE = 9;
}
