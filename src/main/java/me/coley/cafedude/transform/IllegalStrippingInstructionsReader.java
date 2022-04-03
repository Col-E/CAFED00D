package me.coley.cafedude.transform;

import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.constant.*;
import me.coley.cafedude.instruction.BasicInstruction;
import me.coley.cafedude.instruction.Instruction;
import me.coley.cafedude.instruction.IntOperandInstruction;
import me.coley.cafedude.io.FallbackInstructionReader;

import java.nio.ByteBuffer;
import java.util.*;

import static me.coley.cafedude.transform.ReservedBytecodes.*;
import static me.coley.cafedude.instruction.Opcodes.*;

/**
 * Illegal instructions stripper.
 * 
 * @author xDark
 */
final class IllegalStrippingInstructionsReader implements FallbackInstructionReader {

	private static final Instruction NOP_INSN = new BasicInstruction(NOP);
	private static final Instruction ALOAD_0_INSN = new BasicInstruction(ALOAD_0);
	private static final Instruction RETURN_INSN = new BasicInstruction(RETURN);
	
	private final ConstPool cp;
	private Map<Integer, Integer> cpMap;
	boolean rewritten;

	IllegalStrippingInstructionsReader(ConstPool cp) {
		this.cp = cp;
	}

	@Override
	public List<Instruction> read(int opcode, ByteBuffer buffer) {
		switch (opcode) {
			case breakpoint:
				rewritten = true;
				return Arrays.asList(NOP_INSN, NOP_INSN); // Emit two nops
			case fast_aload_0:
				rewritten = true;
				return Collections.singletonList(ALOAD_0_INSN);
			case fast_aldc:
				rewritten = true;
				return Collections.singletonList(new IntOperandInstruction(LDC, 
						rewriteIndex(buffer.get() & 0xff)));
			case fast_aldc_w:
				rewritten = true;
				return Collections.singletonList(new IntOperandInstruction(LDC_W, 
						rewriteIndex(buffer.getShort() & 0xff)));
			case return_register_finalizer:
				rewritten = true;
				return Collections.singletonList(RETURN_INSN);
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
				if (item instanceof CpString || item instanceof CpMethodHandle || item instanceof CpMethodType) {
					cpMap.put(index++, cpIndex);
				}
				cpIndex++;
				if (item.isWide()) {
					cpIndex++;
				}
			}
			this.cpMap = cpMap;
		}
		return cpMap.get(idx);
	}
}
