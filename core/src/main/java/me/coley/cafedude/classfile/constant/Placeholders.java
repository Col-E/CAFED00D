package me.coley.cafedude.classfile.constant;

import me.coley.cafedude.io.ClassFileReader;
import org.jetbrains.annotations.NotNull;

/**
 * Placeholders to be used in {@link ClassFileReader} when constructing {@link CpEntry} values
 * with temporally unknown values.
 *
 * @author Matt Coley
 */
public class Placeholders {
	private static final String EDIT_MESSAGE = "Should not manipulate placeholder CP entries";

	/**
	 * Placeholder for {@link CpUtf8} entries.
	 */
	public static final CpUtf8 UTF8 = new CpUtf8("") {
		@Override
		public void setText(@NotNull String text) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}

		@Override
		public void setIndex(int index) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}

		@Override
		public String toString() {
			return "Placeholder: CpUtf8";
		}
	};

	/**
	 * Placeholder for {@link CpClass} entries.
	 */
	public static final CpClass CLASS = new CpClass(UTF8) {
		@Override
		public String toString() {
			return "Placeholder: CpClass";
		}

		@Override
		public void setName(@NotNull CpUtf8 name) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}

		@Override
		public void setIndex(int index) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}
	};
	/**
	 * Placeholder for {@link CpNameType} entries.
	 */
	public static final CpNameType NAME_TYPE = new CpNameType(UTF8, UTF8) {
		@Override
		public String toString() {
			return "Placeholder: CpNameType";
		}

		@Override
		public void setIndex(int index) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}

		@Override
		public void setName(@NotNull CpUtf8 name) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}

		@Override
		public void setType(@NotNull CpUtf8 type) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}
	};
	/**
	 * Placeholder for {@link ConstRef} entries.
	 */
	public static final ConstRef CONST_REF = new ConstRef(-1, CLASS, NAME_TYPE) {
		@Override
		public String toString() {
			return "Placeholder: ConstRef";
		}

		@Override
		public void setClassRef(@NotNull CpClass classRef) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}

		@Override
		public void setNameType(@NotNull CpNameType nameType) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}

		@Override
		public void setIndex(int index) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}
	};
}
