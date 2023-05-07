package me.coley.cafedude.tree.insn;

/**
 * Types of instructions.
 *
 * @author Justus Garbe
 */
public enum InsnKind {
	ARITHMETIC,
	ARRAY,
	CONSTANT,
	FIELD,
	FLOW,
	IINC,
	INT,
	INVOKE_DYNAMIC,
	LDC,
	LOOKUP_SWITCH,
	METHOD,
	MULTI_ANEWARRAY,
	MONITOR,
	NOP,
	RETURN,
	STACK,
	THROW,
	TABLE_SWITCH,
	TYPE,
	VAR,
	LABEL,
	UNKNOWN;

}
