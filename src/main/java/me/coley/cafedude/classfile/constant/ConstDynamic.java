package me.coley.cafedude.classfile.constant;

/**
 * Base dynamic value pool entry. Points to a {@link CpNameType NameType} constant
 * and a bootstrap method index in the class's bootstrap-methods attribute.
 *
 * @author Matt Coley
 * @author Wolfie / win32kbase
 */
public abstract class ConstDynamic extends ConstPoolEntry {
    private int bsmIndex;
    private int nameTypeIndex;

    /**
     * @param bsmIndex
     * 		Index in the class's bootstrap method attribute-table.
     * @param nameTypeIndex
     * 		Index of {@link CpNameType} in pool.
     */
    public ConstDynamic(int type, int bsmIndex, int nameTypeIndex) {
        super(type);
        this.bsmIndex = bsmIndex;
        this.nameTypeIndex = nameTypeIndex;
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
    public int getNameTypeIndex() {
        return nameTypeIndex;
    }

    /**
     * @param nameTypeIndex
     * 		New index of {@link CpNameType} in pool.
     */
    public void setNameTypeIndex(int nameTypeIndex) {
        this.nameTypeIndex = nameTypeIndex;
    }
}
