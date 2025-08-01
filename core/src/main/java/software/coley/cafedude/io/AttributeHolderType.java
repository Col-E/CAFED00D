package software.coley.cafedude.io;

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
		switch (targetType) {
			case AnnotationConstants.PARAMETER_OF_CLASS_OR_INTERFACE:
			case AnnotationConstants.SUPERTYPE:
			case AnnotationConstants.BOUND_TYPE_PARAMETER_OF_CLASS:
				return AttributeHolderType.CLASS;
			case AnnotationConstants.PARAMETER_OF_METHOD:
			case AnnotationConstants.BOUND_TYPE_PARAMETER_OF_METHOD:
			case AnnotationConstants.METHOD_RETURN_TYPE:
			case AnnotationConstants.METHOD_RECEIVER_TYPE:
			case AnnotationConstants.METHOD_PARAMETER:
			case AnnotationConstants.METHOD_THROWS:
				return AttributeHolderType.METHOD;
			case AnnotationConstants.FIELD:
				return AttributeHolderType.FIELD;
			case AnnotationConstants.LOCAL_VARIABLE_DECLARATION:
			case AnnotationConstants.RESOURCE_VARIABLE_DECLARATION:
			case AnnotationConstants.EXCEPTION_PARAMETER_DECLARATION:
			case AnnotationConstants.INSTANCEOF_EXPRESSION:
			case AnnotationConstants.NEW_EXPRESSION:
			case AnnotationConstants.LAMBDA_NEW_EXPRESSION:
			case AnnotationConstants.LAMBDA_METHOD_REF_EXPRESSION:
			case AnnotationConstants.CAST_EXPRESSION:
			case AnnotationConstants.TYPE_ARGUMENT_OF_NEW_GENERIC_EXPRESSION:
			case AnnotationConstants.TYPE_ARGUMENT_OF_GENERIC_NEW_METHOD_REF_EXPRESSION:
			case AnnotationConstants.TYPE_ARGUMENT_OF_GENERIC_NEW_LAMBDA_CONSTRUCTOR_EXPRESSION:
			case AnnotationConstants.TYPE_ARGUMENT_OF_GENERIC_METHOD_REF_EXPRESSION:
				return AttributeHolderType.ATTRIBUTE;
			default:
				logger.debug("Unknown target type, cannot determine attribute context for: {}", targetType);
				return null;
		}
	}
}
