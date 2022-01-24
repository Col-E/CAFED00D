package me.coley.cafedude.instruction;

import java.util.Arrays;

/**
 * Lookup Switch instruction.
 *
 * @author xDark
 */
public class LookupSwitchInstruction extends BasicInstruction {

	private int dflt;
	private int[] keys;
	private int[] offsets;

	/**
	 * @param dflt
	 * 		Default branch offset.
	 * @param keys
	 * 		Lookup keys in a sorted order.
	 * @param offsets
	 * 		Branch offsets.
	 */
	public LookupSwitchInstruction(int dflt, int[] keys, int[] offsets) {
		super(Opcodes.LOOKUPSWITCH);
		this.dflt = dflt;
		this.keys = keys;
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
	 * @return lookup keys.
	 */
	public int[] getKeys() {
		return keys;
	}

	/**
	 * Sets lookup keys.
	 *
	 * @param keys
	 * 		New keys.
	 */
	public void setKeys(int[] keys) {
		this.keys = keys;
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
		if (!(o instanceof LookupSwitchInstruction)) return false;
		if (!super.equals(o)) return false;

		LookupSwitchInstruction that = (LookupSwitchInstruction) o;

		if (dflt != that.dflt) return false;
		if (!Arrays.equals(keys, that.keys)) return false;
		return Arrays.equals(offsets, that.offsets);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + dflt;
		result = 31 * result + Arrays.hashCode(keys);
		result = 31 * result + Arrays.hashCode(offsets);
		return result;
	}
}
