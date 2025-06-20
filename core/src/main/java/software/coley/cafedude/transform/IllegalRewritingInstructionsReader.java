package software.coley.cafedude.transform;

import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.constant.CpDynamic;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpMethodHandle;
import software.coley.cafedude.classfile.constant.CpMethodType;
import software.coley.cafedude.classfile.constant.CpString;
import software.coley.cafedude.classfile.instruction.BasicInstruction;
import software.coley.cafedude.classfile.instruction.Instruction;
import software.coley.cafedude.classfile.instruction.IntOperandInstruction;
import software.coley.cafedude.classfile.instruction.ReservedOpcodes;
import software.coley.cafedude.io.FallbackInstructionReader;
import software.coley.cafedude.io.IndexableByteStream;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static software.coley.cafedude.classfile.instruction.Opcodes.*;
import static software.coley.cafedude.classfile.instruction.ReservedOpcodes.*;

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

	@Nonnull
	@Override
	public List<Instruction> read(int opcode, @Nonnull IndexableByteStream is) throws IOException {
		switch (opcode) {
			case breakpoint:
				rewritten = true;
				is.readByte(); // Breakpoint occupies two bytes.
				return Arrays.asList(NOP_INSN, NOP_INSN); // Emit two nops
			case fast_aload_0:
				rewritten = true;
				return Collections.singletonList(ALOAD_0_INSN);
			case fast_aldc:
				rewritten = true;
				return Collections.singletonList(new IntOperandInstruction(LDC,
						rewriteIndex(is.readByte() & 0xFF)));
			case fast_aldc_w:
				rewritten = true;
				short idx = is.readShort();
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
			for (CpEntry item : cp) {
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
