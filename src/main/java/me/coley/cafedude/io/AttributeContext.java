package me.coley.cafedude.io;

import me.coley.cafedude.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.coley.cafedude.Constants.Annotations.*;

/**
 * Indicates where attribute is applied to.
 *
 * @author Matt Coley
 */
public enum AttributeContext {
	CLASS, FIELD, METHOD, ATTRIBUTE;

	private static final Logger logger = LoggerFactory.getLogger(AttributeContext.class);

	/**
	 * @param targetType
	 * 		Type annotation type
	 *
	 * @return Where the type annotation <i>(That contains the given target type value)</i> is located.
	 */
	public static AttributeContext fromAnnotationTargetType(int targetType) {
		switch (targetType) {
			case PARAMETER_OF_CLASS_OR_INTERFACE:
			case SUPERTYPE:
			case BOUND_TYPE_PARAMETER_OF_CLASS:
				return AttributeContext.CLASS;
			case PARAMETER_OF_METHOD:
			case BOUND_TYPE_PARAMETER_OF_METHOD:
			case METHOD_RETURN_TYPE:
			case METHOD_RECEIVER_TYPE:
			case METHOD_PARAMETER:
			case METHOD_THROWS:
				return AttributeContext.METHOD;
			case Constants.Annotations.FIELD:
				return AttributeContext.FIELD;
			case LOCAL_VARIABLE_DECLARATION:
			case RESOURCE_VARIABLE_DECLARATION:
			case EXCEPTION_PARAMETER_DECLARATION:
			case INSTANCEOF_EXPRESSION:
			case NEW_EXPRESSION:
			case LAMBDA_NEW_EXPRESSION:
			case LAMBDA_METHOD_REF_EXPRESSION:
			case CAST_EXPRESSION:
			case TYPE_ARGUMENT_OF_NEW_GENERIC_EXPRESSION:
			case TYPE_ARGUMENT_OF_GENERIC_NEW_METHOD_REF_EXPRESSION:
			case TYPE_ARGUMENT_OF_GENERIC_NEW_LAMBDA_CONSTRUCTOR_EXPRESSION:
			case TYPE_ARGUMENT_OF_GENERIC_METHOD_REF_EXPRESSION:
				return AttributeContext.ATTRIBUTE;
			default:
				logger.debug("Unknown target type, cannot determine attribute context for: {}", targetType);
				return null;
		}
	}
}
