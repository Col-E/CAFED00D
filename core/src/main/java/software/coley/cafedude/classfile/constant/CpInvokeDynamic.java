package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;

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
	 * @param nameType
	 * 		Constant pool entry holding the dynamic reference's {@link CpNameType name and type}.
	 */
	public CpInvokeDynamic(int bsmIndex, @Nonnull CpNameType nameType) {
		super(INVOKE_DYNAMIC, bsmIndex, nameType);
	}
}
