package me.coley.cafedude.annotation;

import me.coley.cafedude.Constants;

/**
 * Target information denoting which type in a declaration or expression is annotated.
 *
 * @author Matt Coley
 */
public enum TargetInfoType implements Constants.Annotations {
	TYPE_PARAMETER_TARGET,
	SUPERTYPE_TARGET,
	TYPE_PARAMETER_BOUND_TARGET,
	EMPTY_TARGET,
	FORMAL_PARAMETER_TARGET,
	THROWS_TARGET,
	LOCALVAR_TARGET,
	CATCH_TARGET,
	OFFSET_TARGET,
	TYPE_ARGUMENT_TARGET;

	/**
	 * Get the associated info type from the type value.
	 *
	 * @param type
	 * 		Target type value.
	 *
	 * @return Target info type based on the value.
	 */
	public static TargetInfoType fromTargetType(int type) {
		switch (type) {
			case PARAMETER_OF_CLASS_OR_INTERFACE:
			case PARAMETER_OF_METHOD:
				return TYPE_PARAMETER_TARGET;
			case SUPERTYPE:
				return SUPERTYPE_TARGET;
			case BOUND_TYPE_PARAMETER_OF_CLASS:
			case BOUND_TYPE_PARAMETER_OF_METHOD:
				return TYPE_PARAMETER_BOUND_TARGET;
			case FIELD:
			case METHOD_RETURN_TYPE:
			case METHOD_RECEIVER_TYPE:
				return EMPTY_TARGET;
			case METHOD_PARAMETER:
				return FORMAL_PARAMETER_TARGET;
			case METHOD_THROWS:
				return THROWS_TARGET;
			case LOCAL_VARIABLE_DECLARATION:
			case RESOURCE_VARIABLE_DECLARATION:
				return LOCALVAR_TARGET;
			case EXCEPTION_PARAMETER_DECLARATION:
				return CATCH_TARGET;
			case INSTANCEOF_EXPRESSION:
			case NEW_EXPRESSION:
			case LAMBDA_NEW_EXPRESSION:
			case LAMBDA_METHOD_REF_EXPRESSION:
				return OFFSET_TARGET;
			case CAST_EXPRESSION:
			case TYPE_ARGUMENT_OF_NEW_GENERIC_EXPRESSION:
			case TYPE_ARGUMENT_OF_GENERIC_NEW_METHOD_REF_EXPRESSION:
			case TYPE_ARGUMENT_OF_GENERIC_NEW_LAMBDA_CONSTRUCTOR_EXPRESSION:
			case TYPE_ARGUMENT_OF_GENERIC_METHOD_REF_EXPRESSION:
				return TYPE_ARGUMENT_TARGET;
			default:
				throw new IllegalArgumentException("Invalid type annotation target_type value");
		}
	}
}
