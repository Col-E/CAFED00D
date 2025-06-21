package software.coley.cafedude.tree.insn;

import software.coley.cafedude.classfile.instruction.Opcodes;
import software.coley.cafedude.tree.Label;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * Instruction for the {@link Opcodes#TABLESWITCH} instruction, which has a default offset and a list of offsets.
 *
 * @author Justus Garbe
 * @see Opcodes#TABLESWITCH
 */
public class TableSwitchInsn extends Insn {
	private int padding;
	private int min;
	private int max;
	private List<Label> labels;
	private Label defaultLabel;

	/**
	 * @param min
	 * 		Minimum value of the switch.
	 * @param max
	 * 		Maximum value of the switch.
	 * @param labels
	 * 		Offsets of the switch.
	 * @param defaultLabel
	 * 		Default offset of the switch.
	 */
	public TableSwitchInsn(int min, int max, @Nonnull List<Label> labels, @Nonnull Label defaultLabel) {
		super(InsnKind.TABLE_SWITCH, Opcodes.TABLESWITCH);
		this.labels = labels;
		this.defaultLabel = defaultLabel;
		this.min = min;
		this.max = max;
	}

	/**
	 * @return Minimum value of the switch.
	 */
	public List<Label> getLabels() {
		return labels;
	}

	/**
	 * @param labels
	 * 		Offsets of the switch.
	 */
	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}

	/**
	 * @return Default offset of the switch.
	 */
	public Label getDefaultLabel() {
		return defaultLabel;
	}

	/**
	 * @param defaultLabel
	 * 		Default offset of the switch.
	 */
	public void setDefaultLabel(Label defaultLabel) {
		this.defaultLabel = defaultLabel;
	}

	/**
	 * @return Minimum value of the switch.
	 */
	public int getMin() {
		return min;
	}

	/**
	 * @param min
	 * 		Minimum value of the switch.
	 */
	public void setMin(int min) {
		this.min = min;
	}

	/**
	 * @return Maximum value of the switch.
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @param max
	 * 		Maximum value of the switch.
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * @return Padding of the switch.
	 */
	public int getPadding() {
		return padding;
	}

	/**
	 * @param padding
	 * 		Padding of the switch.
	 */
	public void setPadding(int padding) {
		this.padding = padding;
	}

	@Override
	public int size() {
		// u1 opcode
		// u? padding
		// u4 default
		// u4 low
		// u4 high
		// u4 * (high - low + 1)
		return 1 + padding + 4 + 4 + 4 + labels.size() * 4;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + min + ", " + max + ", " + labels + ", " + defaultLabel + ")";
	}
}
