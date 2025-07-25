package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;
import software.coley.cafedude.io.ClassFileReader;

/**
 * Placeholders to be used in {@link ClassFileReader} when constructing {@link CpEntry} values
 * with temporally unknown values.
 *
 * @author Matt Coley
 */
public class Placeholders {
	private static final String EDIT_MESSAGE = "Should not manipulate placeholder CP entries";

	/**
	 * @param entry
	 * 		Entry to check.
	 *
	 * @return {@code true} when the entry is a placeholder, or references a placeholder.
	 */
	public static boolean isOrContainsPlaceholder(@Nonnull CpEntry entry) {
		return isPlaceholder(entry) || containsPlaceholder(entry);
	}

	/**
	 * @param entry
	 * 		Entry to check.
	 *
	 * @return {@code true} when it contains a reference to a placeholder entry.
	 * {@code false} when it contains only valid/non-placeholder references.
	 */
	public static boolean containsPlaceholder(@Nonnull CpEntry entry) {
		if (entry instanceof CrossCpReferencing referencing)
			for (CpEntry referenced : referencing.getReferences())
				if (isPlaceholder(referenced))
					return true;
		return false;
	}

	/**
	 * @param entry
	 * 		Entry to check.
	 *
	 * @return {@code true} when it is a placeholder entry, otherwise {@code false}.
	 */
	public static boolean isPlaceholder(@Nonnull CpEntry entry) {
		return entry == UTF8 || entry == CLASS || entry == NAME_TYPE || entry == CONST_REF;
	}

	/**
	 * Placeholder for {@link CpUtf8} entries.
	 */
	public static final CpUtf8 UTF8 = new CpUtf8("") {
		@Override
		public void setText(@Nonnull String text) {
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
		public void setName(@Nonnull CpUtf8 name) {
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
		public void setName(@Nonnull CpUtf8 name) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}

		@Override
		public void setType(@Nonnull CpUtf8 type) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}
	};
	/**
	 * Placeholder for {@link ConstRef} entries.
	 */
	public static final ConstRef CONST_REF = new ConstRefInternal(CLASS, NAME_TYPE) {
		@Override
		public String toString() {
			return "Placeholder: ConstRef";
		}

		@Override
		public void setClassRef(@Nonnull CpClass classRef) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}

		@Override
		public void setNameType(@Nonnull CpNameType nameType) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}

		@Override
		public void setIndex(int index) {
			throw new IllegalStateException(EDIT_MESSAGE);
		}
	};
}
