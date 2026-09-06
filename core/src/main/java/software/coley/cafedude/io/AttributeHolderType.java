package software.coley.cafedude.io;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.cafedude.classfile.annotation.AnnotationConstants;

/**
 * Indicates where an attribute is applied to.
 *
 * @author Matt Coley
 * @see AttributeContext Wrapper type.
 */
public enum AttributeHolderType {
	/**
	 * Attribute appears on classes.
	 */
	CLASS,
	/**
	 * Attribute appears on fields.
	 */
	FIELD,
	/**
	 * Attribute appears on methods.
	 */
	METHOD,
	/**
	 * Attribute appears on record components.
	 */
	RECORD_COMPONENT,
	/**
	 * Attribute appears on other attributes.
	 */
	ATTRIBUTE;

	private static final Logger logger = LoggerFactory.getLogger(AttributeHolderType.class);

	/**
	 * @param targetType
	 * 		Type annotation type
	 *
	 * @return Where the type annotation <i>(That contains the given target type value)</i> is located.
	 */
	@Nullable
	public static AttributeHolderType fromAnnotationTargetType(int targetType) {
		return switch (targetType) {
			case AnnotationConstants.PARAMETER_OF_CLASS_OR_INTERFACE,
			     AnnotationConstants.SUPERTYPE,
			     AnnotationConstants.BOUND_TYPE_PARAMETER_OF_CLASS -> AttributeHolderType.CLASS;
			case AnnotationConstants.PARAMETER_OF_METHOD,
			     AnnotationConstants.BOUND_TYPE_PARAMETER_OF_METHOD,
			     AnnotationConstants.METHOD_RETURN_TYPE,
			     AnnotationConstants.METHOD_RECEIVER_TYPE,
			     AnnotationConstants.METHOD_PARAMETER,
			     AnnotationConstants.METHOD_THROWS -> AttributeHolderType.METHOD;
			case AnnotationConstants.FIELD -> AttributeHolderType.FIELD;
			case AnnotationConstants.LOCAL_VARIABLE_DECLARATION,
			     AnnotationConstants.RESOURCE_VARIABLE_DECLARATION,
			     AnnotationConstants.EXCEPTION_PARAMETER_DECLARATION,
			     AnnotationConstants.INSTANCEOF_EXPRESSION,
			     AnnotationConstants.NEW_EXPRESSION,
			     AnnotationConstants.LAMBDA_NEW_EXPRESSION,
			     AnnotationConstants.LAMBDA_METHOD_REF_EXPRESSION,
			     AnnotationConstants.CAST_EXPRESSION,
			     AnnotationConstants.TYPE_ARGUMENT_OF_NEW_GENERIC_EXPRESSION,
			     AnnotationConstants.TYPE_ARGUMENT_OF_GENERIC_NEW_METHOD_REF_EXPRESSION,
			     AnnotationConstants.TYPE_ARGUMENT_OF_GENERIC_NEW_LAMBDA_CONSTRUCTOR_EXPRESSION,
			     AnnotationConstants.TYPE_ARGUMENT_OF_GENERIC_METHOD_REF_EXPRESSION -> AttributeHolderType.ATTRIBUTE;
			default -> {
				logger.debug("Unknown target type, cannot determine attribute context for: {}", targetType);
				yield null;
			}
		};
	}

	/**
	 * @param targetType
	 * 		Type annotation target type.
	 * @param holderType
	 * 		Holder the type annotation appears in.
	 *
	 * @return {@code true} when the target type may appear in the given holder.
	 */
	public static boolean isAnnotationTargetTypeValidIn(int targetType, @Nonnull AttributeHolderType holderType) {
		// Target type 0x13 (FIELD) is used for both fields and record components.
		if (targetType == AnnotationConstants.FIELD && holderType == RECORD_COMPONENT)
			return true;

		// Other types are stricter, and only appear in one holder type.
		AttributeHolderType expected = fromAnnotationTargetType(targetType);
		return expected == holderType;
	}
}
