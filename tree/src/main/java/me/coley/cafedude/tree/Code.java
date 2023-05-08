package me.coley.cafedude.tree;

import me.coley.cafedude.tree.insn.Insn;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Tree model of a method's code.
 *
 * @author Justus Garbe
 */
public class Code {
	private final List<Insn> instructions;
	private final List<Local> locals;
	private final List<ExceptionHandler> handlers;
	private final int maxStack;
	private final int maxLocals;

	/**
	 * @param instructions
	 * 		List of instructions.
	 * @param locals
	 * 		List of local variables.
	 * @param handlers
	 * 		List of exception handlers.
	 * @param maxStack
	 *		Maximum number of stack slots
	 * @param maxLocals
	 * 		Maximum number of local variables.
	 */
	public Code(@Nonnull List<Insn> instructions, @Nonnull List<Local> locals,
				@Nonnull List<ExceptionHandler> handlers, int maxStack, int maxLocals) {
		this.instructions = instructions;
		this.locals = locals;
		this.handlers = handlers;
		this.maxStack = maxStack;
		this.maxLocals = maxLocals;
	}

	/**
	 * @return list of instructions.
	 */
	@Nonnull
	public List<Insn> getInstructions() {
		return instructions;
	}

	/**
	 * @return list of local variables.
	 */
	@Nonnull
	public List<Local> getLocals() {
		return locals;
	}

	/**
	 * @return list of exception handlers.
	 */
	@Nonnull
	public List<ExceptionHandler> getHandlers() {
		return handlers;
	}

	/**
	 * @return Maximum number of values on the operand stack.
	 */
	public int getMaxStack() {
		return maxStack;
	}

	/**
	 * @return Maximum number of local variables.
	 */
	public int getMaxLocals() {
		return maxLocals;
	}
}
