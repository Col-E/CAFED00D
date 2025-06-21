package software.coley.cafedude.tree.insn;

import software.coley.cafedude.classfile.instruction.Opcodes;
import software.coley.cafedude.util.OpcodeUtil;

import jakarta.annotation.Nonnull;

/**
 * Ungrouped instruction. Instructions that use this are:
 * <ul>
 *     <li>{@link Opcodes#NOP}</li>
 *     <li>{@link Opcodes#MONITORENTER}</li>
 *     <li>{@link Opcodes#MONITOREXIT}</li>
 *     <li>{@link Opcodes#ATHROW}</li>
 * </ul>
 *
 * @author Justus Garbe
 */
public class Insn {
	protected static final int LABEL_INSN_OPCODE = -1;
	private final int opcode;
	private final InsnKind kind;

	/**
	 * @param kind
	 * 		Instruction kind.
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public Insn(@Nonnull InsnKind kind, int opcode) {
		this.opcode = opcode;
		this.kind = kind;
	}

	/**
	 * @return Instruction opcode.
	 */
	public int getOpcode() {
		return opcode;
	}

	/**
	 * @return Instruction kind.
	 */
	@Nonnull
	public InsnKind getKind() {
		return kind;
	}

	/**
	 * @return the size that this instruction takes up in bytes.
	 */
	public int size() {
		// u1 opcode
		return 1;
	}

	@Override
	public String toString() {
		return OpcodeUtil.getOpcodeName(opcode);
	}

	/**
	 * @return a new {@link Insn} for {@link Opcodes#NOP}.
	 */
	@Nonnull
	public static Insn nop() {
		return new Insn(InsnKind.NOP, Opcodes.NOP);
	}

	/**
	 * @param opcode
	 * 		Opcode for the instruction.
	 *
	 * @return a new {@link Insn} for {@link Opcodes#MONITORENTER} or {@link Opcodes#MONITOREXIT}.
	 */
	@Nonnull
	public static Insn monitor(int opcode) {
		return new Insn(InsnKind.MONITOR, opcode);
	}

	/**
	 * @return a new {@link Insn} for {@link Opcodes#ATHROW}.
	 */
	@Nonnull
	public static Insn athrow() {
		return new Insn(InsnKind.THROW, Opcodes.ATHROW);
	}
}
