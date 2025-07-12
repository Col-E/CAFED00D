package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

/**
 * Base dynamic value pool entry. Points to a {@link CpNameType NameType} constant
 * and a bootstrap method index in the class's bootstrap-methods attribute.
 *
 * @author Matt Coley
 * @author Wolfie / win32kbase
 */
public abstract sealed class ConstDynamic extends CpEntry implements CrossCpReferencing permits CpDynamic, CpInvokeDynamic {
	private int bsmIndex;
	private CpNameType nameType;

	/**
	 * @param type
	 * 		Dynamic pool entry type.
	 * @param bsmIndex
	 * 		Index in the class's bootstrap method attribute-table.
	 * @param nameType
	 * 		Constant pool entry holding the dynamic reference's {@link CpNameType name and descriptor}.
	 */
	public ConstDynamic(int type, int bsmIndex, @Nonnull CpNameType nameType) {
		super(type);
		// TODO: Instead of BsmIndex, create a BsmEntry type and use that?
		this.bsmIndex = bsmIndex;
		this.nameType = nameType;
	}

	/**
	 * @return Index in the class's bootstrap method attribute-table.
	 */
	public int getBsmIndex() {
		return bsmIndex;
	}

	/**
	 * @param bsmIndex
	 * 		New index in the class's bootstrap method attribute-table.
	 */
	public void setBsmIndex(int bsmIndex) {
		this.bsmIndex = bsmIndex;
	}

	/**
	 * @return Constant pool entry holding the dynamic reference's {@link CpNameType name and descriptor}.
	 */
	@Nonnull
	public CpNameType getNameType() {
		return nameType;
	}

	/**
	 * @param nameType
	 * 		New constant pool entry holding the dynamic reference's {@link CpNameType name and descriptor}.
	 */
	public void setNameType(@Nonnull CpNameType nameType) {
		this.nameType = nameType;
	}

	@Nonnull
	@Override
	public Collection<CpEntry> getReferences() {
		return Collections.singletonList(nameType);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConstDynamic that = (ConstDynamic) o;
		return bsmIndex == that.bsmIndex && nameType == that.nameType;
	}

	@Override
	public int hashCode() {
		int result = bsmIndex;
		result = 31 * result + nameType.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "dynamic-bsm=" + bsmIndex +
				", dynamic-sig=" + nameType.getName().getText() + "." + nameType.getType().getText();
	}
}
