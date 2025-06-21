package software.coley.cafedude.io;

import software.coley.cafedude.classfile.instruction.Instruction;

import jakarta.annotation.Nonnull;
import java.io.IOException;
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
	 * @param is
	 * 		Parent stream.
	 *
	 * @return Read instruction.
	 *
	 * @throws IOException
	 * 		When the stream is unexpectedly closed or ends.
	 */
	@Nonnull
	List<Instruction> read(int opcode, @Nonnull IndexableByteStream is) throws IOException;

	/**
	 * @return Default fail-fast fallback reader.
	 */
	@Nonnull
	static FallbackInstructionReader fail() {
		return (opcode, buffer) -> {
			throw new IllegalStateException("Unable to read: " + opcode);
		};
	}
}
