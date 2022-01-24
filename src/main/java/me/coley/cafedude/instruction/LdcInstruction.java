package me.coley.cafedude.instruction;

import me.coley.cafedude.constant.ConstPoolEntry;

/**
 * Instruction holding constant pool value.
 *
 * @author xDark
 */
public class LdcInstruction extends BasicInstruction {

	private ConstPoolEntry entry;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param entry
	 * 		Constant pool entry.
	 */
	public LdcInstruction(int opcode, ConstPoolEntry entry) {
		super(opcode);
		this.entry = entry;
	}

	/**
	 * @return constant pool entry.
	 */
	public ConstPoolEntry getEntry() {
		return entry;
	}

	/**
	 * Sets constant pool entry.
	 *
	 * @param entry
	 * 		New entry.
	 */
	public void setEntry(ConstPoolEntry entry) {
		this.entry = entry;
	}
}
