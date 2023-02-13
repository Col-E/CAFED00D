package me.coley.cafedude.classfile.instruction;

import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpEntry;

import java.util.Collections;
import java.util.Set;

/**
 * Instruction that references a constant pool entry.
 *
 * @author Justus Garbe
 */
public class CpRefInstruction extends BasicInstruction implements CpAccessor {

	private CpEntry entry;

	public CpRefInstruction(int opcode, CpEntry entry) {
		super(opcode);
		this.entry = entry;
	}

	public CpEntry getEntry() {
		return entry;
	}

	public void setEntry(CpEntry entry) {
		this.entry = entry;
	}

	@Override
	public int computeSize() {
		int opcode = getOpcode();
		if(opcode == Opcodes.LDC) return 1 + 1; // 1 byte opcode + 1 byte index
		else return 1 + 2; // 1 byte opcode + 2 byte index
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CpRefInstruction)) return false;
		if (!super.equals(o)) return false;

		CpRefInstruction that = (CpRefInstruction) o;

		return entry.equals(that.entry);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + entry.hashCode();
		return result;
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		return Collections.singleton(entry);
	}
}
