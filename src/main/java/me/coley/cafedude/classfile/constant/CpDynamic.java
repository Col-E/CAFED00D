package me.coley.cafedude.classfile.constant;

/**
 * Dynamic value pool entry. Points to a {@link CpNameType NameType} constant
 * and a bootstrap method index in the class's bootstrap-methods attribute.
 *
 * @author Matt Coley
 * @author Wolfie / win32kbase
 */
public class CpDynamic extends ConstDynamic {
	/**
	 * @param bsmIndex
	 * 		Index in the class's bootstrap method attribute-table.
	 * @param nameTypeIndex
	 * 		Index of {@link CpNameType} in pool.
	 */
	public CpDynamic(int bsmIndex, int nameTypeIndex) {
		super(DYNAMIC, bsmIndex, nameTypeIndex);
	}
}
