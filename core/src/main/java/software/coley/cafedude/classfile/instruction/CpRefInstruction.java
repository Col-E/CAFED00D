package software.coley.cafedude.classfile.instruction;

import jakarta.annotation.Nonnull;
import software.coley.cafedude.classfile.behavior.CpAccessor;
import software.coley.cafedude.classfile.constant.CpEntry;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Instruction that references a constant pool entry.
 *
 * @author Justus Garbe
 */
public non-sealed class CpRefInstruction extends Instruction implements CpAccessor {
	private CpEntry entry;

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param entry
	 * 		Constant pool entry to reference.
	 */
	public CpRefInstruction(int opcode, @Nonnull CpEntry entry) {
		super(opcode);
		this.entry = entry;
	}

	/**
	 * @return Constant pool entry to reference.
	 */
	@Nonnull
	public CpEntry getEntry() {
		return entry;
	}

	/**
	 * @param entry
	 * 		New constant pool entry to reference.
	 */
	public void setEntry(@Nonnull CpEntry entry) {
		this.entry = entry;
	}

	@Override
	public int computeSize() {
		int opcode = getOpcode();

		// LDC only has one byte as its argument size
		// But LDC_W and all other constant pool referencing instructions use shorts (two bytes)
		if (opcode == Opcodes.LDC)
			return 1 + 1; // 1 byte opcode + 1 byte index

		// InvokeDynamic and Invokespecial have two bytes of padding after them for some reason.
		// See: jvms-6.5.invokedynamic / jvms-6.5.invokeinterface
		if (opcode == Opcodes.INVOKEDYNAMIC || opcode == Opcodes.INVOKEINTERFACE)
			return 1 + 2 + 2; // 1 byte opcode + 2 byte index + 2 byte padding

		return 1 + 2; // 1 byte opcode + 2 byte index
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CpRefInstruction that)) return false;
		if (!super.equals(o)) return false;

		return Objects.equals(entry, that.entry);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + entry.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return super.toString() + " entry=" + entry;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		return Collections.singleton(entry);
	}
}
