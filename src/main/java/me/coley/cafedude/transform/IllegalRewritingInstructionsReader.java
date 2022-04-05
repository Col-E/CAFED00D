package me.coley.cafedude.transform;

import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.constant.ConstPoolEntry;
import me.coley.cafedude.classfile.constant.CpDynamic;
import me.coley.cafedude.classfile.constant.CpMethodHandle;
import me.coley.cafedude.classfile.constant.CpMethodType;
import me.coley.cafedude.classfile.constant.CpString;
import me.coley.cafedude.classfile.instruction.BasicInstruction;
import me.coley.cafedude.classfile.instruction.Instruction;
import me.coley.cafedude.classfile.instruction.IntOperandInstruction;
import me.coley.cafedude.classfile.instruction.ReservedOpcodes;
import me.coley.cafedude.io.FallbackInstructionReader;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.coley.cafedude.classfile.instruction.Opcodes.*;
import static me.coley.cafedude.classfile.instruction.ReservedOpcodes.*;

/**
 * Illegal instruction rewriter.
 *
 * @author xDark
 * @see ReservedOpcodes Opcodes of reserved instructions.
 * @see IllegalStrippingTransformer Example usage.
 */
final class IllegalRewritingInstructionsReader implements FallbackInstructionReader {
	private static final Instruction NOP_INSN = new BasicInstruction(NOP);
	private static final Instruction ALOAD_0_INSN = new BasicInstruction(ALOAD_0);
	private static final Instruction RETURN_INSN = new BasicInstruction(RETURN);

	private final ConstPool cp;
	private Map<Integer, Integer> cpMap;
	boolean rewritten;

	/**
	 * @param cp
	 * 		Constant pool used to pull references from.
	 */
	public IllegalRewritingInstructionsReader(ConstPool cp) {
		this.cp = cp;
	}

	@Override
	public List<Instruction> read(int opcode, ByteBuffer buffer) {
		switch (opcode) {
			case breakpoint:
				rewritten = true;
				buffer.get(); // Breakpoint occupies two bytes.
				return Arrays.asList(NOP_INSN, NOP_INSN); // Emit two nops
			case fast_aload_0:
				rewritten = true;
				return Collections.singletonList(ALOAD_0_INSN);
			case fast_aldc:
				rewritten = true;
				return Collections.singletonList(new IntOperandInstruction(LDC,
						rewriteIndex(buffer.get() & 0xFF)));
			case fast_aldc_w:
				rewritten = true;
				short idx = buffer.getShort();
				int newIndex = rewriteIndex(idx & 0xFFFF);
				if (newIndex == -1)
					newIndex = rewriteIndex(swap(idx) & 0xFFFF);
				if (newIndex == -1)
					throw new IllegalStateException("Failed to rewrite fast_aldc_w: " + idx);
				return Collections.singletonList(new IntOperandInstruction(LDC_W, newIndex));
			case return_register_finalizer:
				rewritten = true;
				return Collections.singletonList(RETURN_INSN);
			case shouldnotreachhere:
				rewritten = true;
				return Collections.singletonList(NOP_INSN);
			default:
				throw new IllegalStateException("Don't know how to rewrite " + opcode);
		}
	}

	private int rewriteIndex(int idx) {
		Map<Integer, Integer> cpMap = this.cpMap;
		if (cpMap == null) {
			cpMap = new HashMap<>();
			int index = 0;
			int cpIndex = 1;
			for (ConstPoolEntry item : cp) {
				if (item instanceof CpString || item instanceof CpMethodHandle
						|| item instanceof CpMethodType || item instanceof CpDynamic) {
					cpMap.put(index++, cpIndex);
				}
				cpIndex++;
				if (item.isWide()) {
					cpIndex++;
				}
			}
			this.cpMap = cpMap;
		}
		Integer v = cpMap.get(idx);
		return v == null ? -1 : v;
	}

	private static short swap(short x) {
		return (short) (((x >> 8) & 0xFF) | (x << 8));
	}
}
