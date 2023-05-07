package me.coley.cafedude.classfile.instruction;

import java.util.List;

/**
 * Lookup Switch instruction.
 *
 * @author xDark
 */
public class LookupSwitchInstruction extends BasicInstruction {
	private int padding;
	private int dflt;
	private List<Integer> keys;
	private List<Integer> offsets;

	/**
	 * @param padding
	 * 		Switches in the JVM must be aligned, this is the number of bytes in pattern required to be aligned.
	 * @param dflt
	 * 		Default branch offset.
	 * @param keys
	 * 		Lookup keys in a sorted order.
	 * @param offsets
	 * 		Branch offsets.
	 */
	public LookupSwitchInstruction(int padding, int dflt, List<Integer> keys, List<Integer> offsets) {
		super(Opcodes.LOOKUPSWITCH);
		this.dflt = dflt;
		this.keys = keys;
		this.offsets = offsets;
		this.padding = padding;
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
	public List<Integer> getKeys() {
		return keys;
	}

	/**
	 * Sets lookup keys.
	 *
	 * @param keys
	 * 		New keys.
	 */
	public void setKeys(List<Integer> keys) {
		this.keys = keys;
	}

	/**
	 * @return branch offsets.
	 */
	public List<Integer> getOffsets() {
		return offsets;
	}

	/**
	 * Sets branch offsets.
	 *
	 * @param offsets
	 * 		New offsets.
	 */
	public void setOffsets(List<Integer> offsets) {
		this.offsets = offsets;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LookupSwitchInstruction)) return false;
		if (!super.equals(o)) return false;

		LookupSwitchInstruction that = (LookupSwitchInstruction) o;

		if (dflt != that.dflt) return false;
		if (!keys.equals(that.keys)) return false;
		return offsets.equals(that.offsets);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + dflt;
		result = 31 * result + keys.hashCode();
		result = 31 * result + offsets.hashCode();
		return result;
	}

	@Override
	public int computeSize() {
		// u1: opcode
		// ??: padding
		// u4: default
		// u4: npairs
		// u4[]: keys
		// u4[]: offsets
		return 1 + padding + 4 + 4 + 4 * keys.size() + 4 * offsets.size();
	}
}
