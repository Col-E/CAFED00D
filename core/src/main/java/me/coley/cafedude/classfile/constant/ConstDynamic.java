package me.coley.cafedude.classfile.constant;

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
     *      Dynamic pool entry type.
     * @param bsmIndex
     * 		Index in the class's bootstrap method attribute-table.
     * @param nameType
     * 		Index of {@link CpNameType} in pool.
     */
    public ConstDynamic(int type, int bsmIndex, CpNameType nameType) {
        super(type);
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
     * @return Index of {@link CpNameType} in pool.
     */
    public CpNameType getNameType() {
        return nameType;
    }

    /**
     * @param nameType
     * 		New index of {@link CpNameType} in pool.
     */
    public void setNameType(CpNameType nameType) {
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
