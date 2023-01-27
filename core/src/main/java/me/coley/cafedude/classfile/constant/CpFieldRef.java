package me.coley.cafedude.classfile.constant;

/**
 * Field reference pool entry. Points to a reference's {@link CpClass defining class} in pool
 * and the reference's {@link CpNameType name and descriptor} in pool.
 *
 * @author Matt Coley
 */
public class CpFieldRef extends ConstRef {
	/**
	 * @param classIndex
	 * 		Index of field's {@link CpClass defining class} in pool.
	 * @param nameTypeIndex
	 * 		Index of field's {@link CpNameType name and descriptor} in pool.
	 */
	public CpFieldRef(CpClass classRef, CpNameType nameType) {
		super(FIELD_REF, classRef, nameType);
	}
}
