package me.coley.cafedude.tree.insn;

import me.coley.cafedude.classfile.instruction.Opcodes;
import org.intellij.lang.annotations.MagicConstant;

/**
 * Ungrouped instruction. Instructions that use this are:
 * <ul>
 *     <li>{@link Opcodes#NOP}</li>
 *     <li>{@link Opcodes#MONITORENTER}</li>
 *     <li>{@link Opcodes#MONITOREXIT}</li>
 *     <li>{@link Opcodes#ATHROW}</li>
 * </ul>
 */
public class Insn {

	private final int opcode;
	private final InsnKind kind;

	/**
	 * @param kind
	 * 		Instruction kind.
	 * @param opcode
	 * 		Instruction opcode.
	 */
	public Insn(InsnKind kind, int opcode) {
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
		return "insn(" + opcode + ")";
	}

	public static Insn nop() {
		return new Insn(InsnKind.NOP, Opcodes.NOP);
	}

	@MagicConstant(intValues = {Opcodes.MONITORENTER, Opcodes.MONITOREXIT})
	public static Insn monitor(int opcode) {
		return new Insn(InsnKind.MONITOR, opcode);
	}

	public static Insn athrow() {
		return new Insn(InsnKind.THROW, Opcodes.ATHROW);
	}
}
