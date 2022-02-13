package me.coley.cafedude.classfile.behavior;

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
	Set<Integer> cpAccesses();
}
