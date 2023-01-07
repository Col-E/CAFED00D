package me.coley.cafedude.classfile.constant;

/**
 * Method reference pool entry. Points to a reference's {@link CpClass defining class} in pool
 * and the reference's {@link CpNameType name and descriptor} in pool.
 *
 * @author Matt Coley
 */
public class CpMethodRef extends ConstRef {
	/**
	 * @param classIndex
	 * 		Index of method's {@link CpClass defining class} in pool.
	 * @param nameTypeIndex
	 * 		Index of method's {@link CpNameType name and descriptor} in pool.
	 */
	public CpMethodRef(int classIndex, int nameTypeIndex) {
		super(METHOD_REF, classIndex, nameTypeIndex);
	}
}
