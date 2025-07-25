package software.coley.cafedude.io;

import jakarta.annotation.Nonnull;

/**
 * Wrapper to hold context details for attribute parsing.
 * Some attribute parsing logic <i>(and verification)</i> is dependent on context.
 *
 * @param type
 * 		Holder type of this attribute.
 * @param memberAccess
 * 		If the holder type is a {@link AttributeHolderType#FIELD} or {@link AttributeHolderType#METHOD}
 * 		then the member access modifiers. Otherwise {@code 0}.
 *
 * @author Matt Coley
 */
public record AttributeContext(@Nonnull AttributeHolderType type, int memberAccess) {
	@Nonnull
	public String name() {
		return type.name();
	}

	public boolean isClass() {
		return type == AttributeHolderType.CLASS;
	}

	public boolean isField() {
		return type == AttributeHolderType.FIELD;
	}

	public boolean isRecordComponent() {
		return type == AttributeHolderType.RECORD_COMPONENT;
	}

	public boolean isMethod() {
		return type == AttributeHolderType.METHOD;
	}

	public boolean isAttribute() {
		return type == AttributeHolderType.ATTRIBUTE || isRecordComponent();
	}
}
