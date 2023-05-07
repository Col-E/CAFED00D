package me.coley.cafedude.classfile.constant;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Base dynamic value pool entry. Points to a {@link CpNameType NameType} constant
 * and a bootstrap method index in the class's bootstrap-methods attribute.
 *
 * @author Matt Coley
 * @author Wolfie / win32kbase
 */
public abstract class ConstDynamic extends CpEntry {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConstDynamic that = (ConstDynamic) o;
		return bsmIndex == that.bsmIndex && nameType == that.nameType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bsmIndex, nameType);
	}
}
