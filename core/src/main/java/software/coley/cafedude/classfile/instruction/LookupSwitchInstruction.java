package software.coley.cafedude.classfile.instruction;

import java.util.List;
import java.util.Objects;

/**
 * Lookup Switch instruction.
 *
 * @author xDark
 */
public non-sealed class LookupSwitchInstruction extends Instruction {
	private int padding = -1;
	private int dflt;
	private List<Integer> keys;
	private List<Integer> offsets;

	/**
	 * @param dflt
	 * 		Default branch offset.
	 * @param keys
	 * 		Lookup keys in a sorted order.
	 * @param offsets
	 * 		Branch offsets.
	 */
	public LookupSwitchInstruction(int dflt, List<Integer> keys, List<Integer> offsets) {
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

	/**
	 * @return Padding of the switch.
	 *
	 * @see #notifyStartPosition(int)
	 */
	public int getPadding() {
		return padding;
	}

	/**
	 * Called to update the padding.
	 *
	 * @param position
	 * 		The position where this instruction <i>(the opcode)</i> starts in the method code.
	 */
	public void notifyStartPosition(int position) {
		// Padding must be updated such that the dflt offset starts at a multiple of 4
		//
		// 1: opcode
		// 2: pad      2: opcode
		// 3: pad      3: pad      3: opcode
		// 4: pad      4: pad      4: pad     4: opcode
		// 5: def      5: def      5: def     5: def
		this.padding = 3 - ((position) & 3);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LookupSwitchInstruction that)) return false;
		if (!super.equals(o)) return false;

		if (padding != that.padding) return false;
		if (dflt != that.dflt) return false;
		if (!Objects.equals(keys, that.keys)) return false;
		return Objects.equals(offsets, that.offsets);
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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.size(); i++) {
			if (!sb.isEmpty()) sb.append(", ");
			sb.append(keys.get(i)).append("=").append(offsets.get(i));
		}
		sb.append(" default=").append(dflt);
		return super.toString() + " " + sb;
	}

	@Override
	public int computeSize() {
		if (padding < 0)
			throw new IllegalStateException("Padding size not computed!");
		// u1: opcode
		// ??: padding
		// u4: default
		// u4: npairs
		// u4[]: keys
		// u4[]: offsets
		return 1 + padding + 4 + 4 + 4 * keys.size() + 4 * offsets.size();
	}
}
