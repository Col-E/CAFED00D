package me.coley.cafedude.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class Optional {

	public static <T, R> @Nullable R orNull(T value, Function<T, R> orElse) {
		return value == null ? null : orElse.apply(value);
	}

	public static <T, R> R orElse(T value, Function<T, R> orElse, R defaultValue) {
		return value == null ? defaultValue : orElse.apply(value);
	}

}
