package me.coley.cafedude.tree;

import me.coley.cafedude.tree.insn.Insn;

import java.util.List;

public class Code {

	private final List<Insn> instructions;
	private final List<Local> locals;
	private final List<ExceptionHandler> handlers;
	private final int maxStack;
	private final int maxLocals;

	public Code(List<Insn> instructions, List<Local> locals, List<ExceptionHandler> handlers, int maxStack, int maxLocals) {
		this.instructions = instructions;
		this.locals = locals;
		this.handlers = handlers;
		this.maxStack = maxStack;
		this.maxLocals = maxLocals;
	}

	public List<Insn> getInstructions() {
		return instructions;
	}

	public List<Local> getLocals() {
		return locals;
	}

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
