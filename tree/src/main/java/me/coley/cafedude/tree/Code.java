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

	public Code(@Nonnull List<Insn> instructions, @Nonnull List<Local> locals,
				@Nonnull List<ExceptionHandler> handlers, int maxStack, int maxLocals) {
		this.instructions = instructions;
		this.locals = locals;
		this.handlers = handlers;
		this.maxStack = maxStack;
		this.maxLocals = maxLocals;
	}

	@Nonnull
	public List<Insn> getInstructions() {
		return instructions;
	}

	@Nonnull
	public List<Local> getLocals() {
		return locals;
	}

	@Nonnull
	public List<ExceptionHandler> getHandlers() {
		return handlers;
	}

	public int getMaxStack() {
		return maxStack;
	}

	public int getMaxLocals() {
		return maxLocals;
	}
}
