package software.coley.cafedude.classfile.attribute;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.cafedude.classfile.instruction.Instruction;

import java.util.List;

import static software.coley.cafedude.classfile.instruction.Opcodes.*;

/**
 * Common utilities for working with {@link Instruction} values.
 * <br>
 * Generally accessible via {@link CodeAttribute} but any sequence of {@link Instruction} values
 * can be wrapped into this utility via {@link #wrap(List)}.
 *
 * @author Matt Coley
 */
public interface CodeUtilities {
	@Nonnull
	List<Instruction> getInstructions();

	/**
	 * An instance matching index-of since the standard {@link List#indexOf(Object)} uses object equality.
	 *
	 * @param instruction
	 * 		A specific instruction in the code.
	 *
	 * @return Index in the instructions list where the code appears.
	 */
	default int indexOf(@Nonnull Instruction instruction) {
		// We have our own index-of because 'list.indexOf' uses 'item.equals(other)' which is not what we want.
		List<Instruction> instructions = getInstructions();
		for (int i = 0; i < instructions.size(); i++)
			if (instructions.get(i) == instruction)
				return i;
		return -1;
	}

	/**
	 * @param instruction
	 * 		A specific instruction in the code.
	 *
	 * @return Method bytecode offset of the instruction.
	 */
	default int computeOffsetOf(@Nonnull Instruction instruction) {
		int index = indexOf(instruction);
		if (index == 0) return 0;
		if (index < 0) return -1;

		List<Instruction> instructions = getInstructions();
		int offset = 0;
		for (int i = 0; i < index; i++)
			offset += instructions.get(i).computeSize();
		return offset;
	}

	/**
	 * @param offset
	 * 		Method bytecode offset of some instruction.
	 * 		This must be the offset of the instruction's opcode.
	 *
	 * @return The instruction at the given offset, or {@code null} if the offset does not map to an instruction.
	 */
	@Nullable
	default Instruction getInstructionAtOffset(int offset) {
		int current = 0;
		for (Instruction instruction : getInstructions()) {
			if (current == offset) return instruction;
			else if (current > offset) break;
			current += instruction.computeSize();
		}
		return null;
	}

	/**
	 * @param offset
	 * 		Method bytecode offset of some instruction.
	 * 		This can be any byte that makes up the instruction, including the start, or any operand bytes.
	 *
	 * @return The instruction at the given offset, or {@code null} if the offset is outside the bounds of the method.
	 */
	@Nullable
	default Instruction getContainingInstructionAtOffset(int offset) {
		int current = 0;
		for (Instruction instruction : getInstructions()) {
			int size = instruction.computeSize();
			if (offset >= current && offset < current + size)
				return instruction;
			else if (current > offset)
				break;
			current += size;
		}
		return null;
	}

	/**
	 * @return Byte count of this code block.
	 */
	default int computeSize() {
		Instruction last = getInstructions().get(getInstructions().size() - 1);
		return computeOffsetOf(last) + last.computeSize();
	}

	/**
	 * @param instruction
	 * 		Some instruction.
	 *
	 * @return {@code true} when it represents a branch instruction.
	 */
	static boolean isBranch(@Nonnull Instruction instruction) {
		int op = instruction.getOpcode();
		return (op >= IFEQ && op <= JSR) || op == GOTO_W || op == IFNULL || op == IFNONNULL;
	}

	/**
	 * @param instruction
	 * 		Some instruction.
	 *
	 * @return {@code true} when it represents an instruction that
	 * results in consistent control flow redirection or termination.
	 */
	static boolean isTerminalOrAlwaysTakeFlowControl(@Nonnull Instruction instruction) {
		int op = instruction.getOpcode();
		return switch (op) {
			case IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN,
					ATHROW, TABLESWITCH, LOOKUPSWITCH, GOTO, GOTO_W, JSR -> true;
			default -> false;
		};
	}

	/**
	 * @param instructions
	 * 		List of instructions assumed to be following one another sequentially.
	 *
	 * @return Utilities wrapper for the given instructions.
	 */
	@Nonnull
	static CodeUtilities wrap(@Nonnull List<Instruction> instructions) {
		return () -> instructions;
	}
}
