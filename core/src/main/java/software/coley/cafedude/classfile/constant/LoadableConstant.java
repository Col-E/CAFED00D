package software.coley.cafedude.classfile.constant;

import software.coley.cafedude.classfile.instruction.Opcodes;

/**
 * Outline of a constant pool entry that can be loaded via
 * {@link Opcodes#LDC}/{@link Opcodes#LDC_W}/{@link Opcodes#LDC2_W}.
 *
 * @author Matt Coley
 */
public sealed interface LoadableConstant permits CpClass, CpDouble, CpDynamic, CpFloat, CpInt, CpLong,
		CpMethodHandle, CpMethodType, CpString {
}
