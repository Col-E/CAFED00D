package me.coley.cafedude.util;


import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Optional utilities.
 *
 * @author Justus Garbe
 */
public class Optional {
	/**
	 * @param value
	 * 		Value to use in the function lookup.
	 * @param orElse
	 * 		Function to map the value into another type.
	 * @param <T>
	 * 		Value type.
	 * @param <R>
	 * 		Converted type.
	 *
	 * @return The function's return value, or {@code null} if the provided value was {@code null}.
	 */
	public static <T, R> @Nullable R orNull(T value, Function<T, R> orElse) {
		return value == null ? null : orElse.apply(value);
	}

	/**
	 * @param value
	 * 		Value to use in the function lookup.
	 * @param orElse
	 * 		Function to map the value into another type.
	 * @param defaultValue
	 * 		Fallback value when the passed value is {@code null}.
	 * @param <T>
	 * 		Value type.
	 * @param <R>
	 * 		Converted type.
	 *
	 * @return The function's return value, or the default-value if the provided value was {@code null}.
	 */
	public static <T, R> R orElse(T value, Function<T, R> orElse, R defaultValue) {
		return value == null ? defaultValue : orElse.apply(value);
	}
}
