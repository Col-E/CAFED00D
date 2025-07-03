package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;

import java.util.Collection;

/**
 * Outline of a constant pool entry that references other entries in the constant pool.
 *
 * @author Matt Coley
 */
public sealed interface CrossCpReferencing permits ConstDynamic, ConstRef, CpClass, CpMethodHandle, CpMethodType,
		CpModule, CpNameType, CpPackage, CpString {
	/**
	 * @return Collection of constant pool entries this entry references.
	 */
	@Nonnull
	Collection<CpEntry> getReferences();
}
