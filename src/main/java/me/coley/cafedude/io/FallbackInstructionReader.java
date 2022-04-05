package me.coley.cafedude.io;

import me.coley.cafedude.classfile.instruction.Instruction;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Fallback reader that handles unknown instructions.
 *
 * @author xDark
 */
public interface FallbackInstructionReader {

	/**
	 * @param opcode
	 * 		Instruction opcode.
	 * @param buffer
	 * 		Buffer containing instruction data.
	 *
	 * @return Read instruction.
	 */
	List<Instruction> read(int opcode, ByteBuffer buffer);

	/**
	 * @return Default fail-fast fallback reader.
	 */
	static FallbackInstructionReader fail() {
		return (opcode, buffer) -> {
			throw new IllegalStateException("Unable to read: " + opcode);
		};
	}
}
