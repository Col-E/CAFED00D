package me.coley.cafedude.classfile.constant;

/**
 * Invoke-dynamic value pool entry. Points to a {@link CpNameType NameType} constant
 * and a bootstrap method index in the class's bootstrap-methods attribute.
 *
 * @author Matt Coley
 * @author Wolfie / win32kbase
 */
public class CpInvokeDynamic extends ConstDynamic {
	/**
	 * @param bsmIndex
	 * 		Index in the class's bootstrap method attribute-table.
	 * @param nameTypeIndex
	 * 		Index of {@link CpNameType} in pool.
	 */
	public CpInvokeDynamic(int bsmIndex, CpNameType nameType) {
		super(INVOKE_DYNAMIC, bsmIndex, nameType);
	}
}
