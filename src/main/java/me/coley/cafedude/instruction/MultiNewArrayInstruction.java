package me.coley.cafedude.instruction;

/**
 * Multidimensional new array instruction.
 *
 * @author xDark
 */
public class MultiNewArrayInstruction extends BasicInstruction {

	private String type;
	private int dimensions;

	/**
	 * @param type
	 * 		Array type.
	 * @param dimensions
	 * 		Array dimensions.
	 */
	public MultiNewArrayInstruction(String type, int dimensions) {
		super(Opcodes.MULTIANEWARRAY);
		this.type = type;
		this.dimensions = dimensions;
	}

	/**
	 * @return array type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets array type.
	 *
	 * @param type
	 * 		New type.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the amount of array dimensions.
	 */
	public int getDimensions() {
		return dimensions;
	}

	/**
	 * Sets the amount of array dimensions.
	 *
	 * @param dimensions
	 * 		New amount.
	 */
	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MultiNewArrayInstruction)) return false;
		if (!super.equals(o)) return false;

		MultiNewArrayInstruction that = (MultiNewArrayInstruction) o;

		if (dimensions != that.dimensions) return false;
		return type.equals(that.type);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + dimensions;
		return result;
	}

	@Override
	public String toString() {
		return "multinewarray(" + type + ", " + dimensions + ")";
	}
}
