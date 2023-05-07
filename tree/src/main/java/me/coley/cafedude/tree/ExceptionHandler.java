package me.coley.cafedude.tree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exception handler object.
 *
 * @author Justus Garbe
 */
public class ExceptionHandler {
	@Nullable
	private final String type;
	private final Label start;
	private final Label end;
	private final Label handler;

	/**
	 * @param type
	 * 		Exception type.
	 * @param start
	 * 		Start of handled region.
	 * @param end
	 * 		End of handled region.
	 * @param handler
	 * 		Handler label.
	 */
	public ExceptionHandler(@Nullable String type, @Nonnull Label start, @Nonnull Label end, @Nonnull Label handler) {
		this.type = type;
		this.start = start;
		this.end = end;
		this.handler = handler;
	}

	/**
	 * @return Exception type.
	 */
	@Nullable
	public String getType() {
		return type;
	}

	/**
	 * @return Start of handled region.
	 */
	@Nonnull
	public Label getStart() {
		return start;
	}

	/**
	 * @return End of handled region.
	 */
	@Nonnull
	public Label getEnd() {
		return end;
	}

	/**
	 * @return Handler label.
	 */
	@Nonnull
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
