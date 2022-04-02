package me.coley.cafedude.io;

import me.coley.cafedude.instruction.Instruction;
import me.coley.cafedude.util.GrowingByteBuffer;

/**
 * Fallback writer that handles unknown instructions.
 *
 * @author xDark
 */
public interface FallbackInstructionWriter {

	/**
	 * @param instruction
	 * 		Instruction to write.
	 * @param buffer
	 * 		Buffer containing instruction data.
	 */
	void write(Instruction instruction, GrowingByteBuffer buffer);

	/**
	 * @return Default fail-fast fallback reader.
	 */
	static FallbackInstructionWriter fail() {
		return (opcode, buffer) -> {
			throw new IllegalStateException("Unable to writer: " + opcode);
		};
	}
}
