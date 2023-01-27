package me.coley.cafedude.classfile.behavior;

import me.coley.cafedude.classfile.constant.CpEntry;

import java.util.Set;

/**
 * Applied to a data type that requires access to the constant pool.
 *
 * @author Matt Coley
 */
public interface CpAccessor {
	/**
	 * @return Indices accessed.
	 */
	Set<CpEntry> cpAccesses();
}
