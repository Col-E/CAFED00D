package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;

/**
 * Dynamic value pool entry. Points to a {@link CpNameType NameType} constant
 * and a bootstrap method index in the class's bootstrap-methods attribute.
 *
 * @author Matt Coley
 * @author Wolfie / win32kbase
 */
public non-sealed class CpDynamic extends ConstDynamic {
	/**
	 * @param bsmIndex
	 * 		Index in the class's bootstrap method attribute-table.
	 * @param nameType
	 * 		Constant pool entry holding the name and type of the dynamic reference.
	 */
	public CpDynamic(int bsmIndex, @Nonnull CpNameType nameType) {
		super(DYNAMIC, bsmIndex, nameType);
	}
}
