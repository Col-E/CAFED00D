package me.coley.cafedude.instruction;

import java.util.Arrays;
import java.util.Objects;

/**
 * Table Switch instruction.
 *
 * @author xDark
 */
public class TableSwitchInstruction extends BasicInstruction {

	private int dflt;
	private int low;
	private int high;
	private int[] offsets;

	/**
	 * @param dflt
	 * 		Default branch offset.
	 * @param low
	 * 		Minimmum value.
	 * @param high
	 * 		Maximum value.
	 * @param offsets
	 * 		Branch offsets.
	 */
	public TableSwitchInstruction(int dflt, int low, int high, int[] offsets) {
		super(Opcodes.TABLESWITCH);
		this.dflt = dflt;
		this.low = low;
		this.high = high;
		this.offsets = offsets;
	}

	/**
	 * @return default branch offset.
	 */
	public int getDefault() {
		return dflt;
	}

	/**
	 * Sets default branch offset.
	 *
	 * @param dflt
	 * 		New offset.
	 */
	public void setDefault(int dflt) {
		this.dflt = dflt;
	}

	/**
	 * @return minimum value.
	 */
	public int getLow() {
		return low;
	}

	/**
	 * Sets minimum value.
	 *
	 * @param low
	 * 		New value.
	 */
	public void setLow(int low) {
		this.low = low;
	}

	/**
	 * @return maximum value.
	 */
	public int getHigh() {
		return high;
	}

	/**
	 * Sets maximum value.
	 *
	 * @param high
	 * 		New value.
	 */
	public void setHigh(int high) {
		this.high = high;
	}

	/**
	 * @return branch offsets.
	 */
	public int[] getOffsets() {
		return offsets;
	}

	/**
	 * Sets branch offsets.
	 *
	 * @param offsets
	 * 		New offsets.
	 */
	public void setOffsets(int[] offsets) {
		this.offsets = offsets;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TableSwitchInstruction)) return false;
		if (!super.equals(o)) return false;

		TableSwitchInstruction that = (TableSwitchInstruction) o;

		if (dflt != that.dflt) return false;
		if (low != that.low) return false;
		if (high != that.high) return false;
		return Arrays.equals(offsets, that.offsets);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + dflt;
		result = 31 * result + low;
		result = 31 * result + high;
		result = 31 * result + Arrays.hashCode(offsets);
		return result;
	}
}
