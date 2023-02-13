package me.coley.cafedude.tree;

import org.jetbrains.annotations.Nullable;

/**
 * Exception handler object.
 */
public class ExceptionHandler {

	private final @Nullable String type;
	private final Label start;
	private final Label end;
	private final Label handler;

	/**
	 * @param type
	 * 		Exception type.
	 * @param start
	 *      Start of handled region.
	 * @param end
	 * 		End of handled region.
	 * @param handler
	 * 		Handler label.
	 */
	public ExceptionHandler(@Nullable String type, Label start, Label end, Label handler) {
		this.type = type;
		this.start = start;
		this.end = end;
		this.handler = handler;
	}

	/**
	 * @return Exception type.
	 */
	public @Nullable String getType() {
		return type;
	}

	/**
	 * @return Start of handled region.
	 */
	public Label getStart() {
		return start;
	}

	/**
	 * @return End of handled region.
	 */
	public Label getEnd() {
		return end;
	}

	/**
	 * @return Handler label.
	 */
	public Label getHandler() {
		return handler;
	}

	/**
	 * @return {@code true} if the exception handler is a catch-all.
	 */
	public boolean isCatchAll() {
		return type == null;
	}

}
