package me.coley.cafedude.classfile.annotation;

/**
 * Enum element value.
 *
 * @author Matt Coley
 */
public class EnumElementValue extends ElementValue {
	private int typeIndex;
	private int nameIndex;

	/**
	 * @param tag
	 * 		ASCII tag representation, must be {@code e}.
	 * @param typeIndex
	 * 		Index of enum type descriptor constant.
	 * @param nameIndex
	 * 		Index of enum value name constant.
	 */
	public EnumElementValue(char tag, int typeIndex, int nameIndex) {
		super(tag);
		if (tag != 'e')
			throw new IllegalArgumentException("UTF8 element value must have 'e' tag");
		this.typeIndex = typeIndex;
		this.nameIndex = nameIndex;
	}

	/**
	 * @return Index of enum type descriptor constant.
	 */
	public int getTypeIndex() {
		return typeIndex;
	}

	/**
	 * @param typeIndex
	 * 		Index of enum type descriptor constant.
	 */
	public void setTypeIndex(int typeIndex) {
		this.typeIndex = typeIndex;
	}

	/**
	 * @return Index of enum value name constant.
	 */
	public int getNameIndex() {
		return nameIndex;
	}

	/**
	 * @param nameIndex
	 * 		Index of enum value name constant.
	 */
	public void setNameIndex(int nameIndex) {
		this.nameIndex = nameIndex;
	}

	/**
	 * @return ASCII tag representation of an enum, {@code e}.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

	@Override
	public int computeLength() {
		// u1: tag
		// u2: enum_type_index
		// u2: enum_name_index
		return 5;
	}
}
