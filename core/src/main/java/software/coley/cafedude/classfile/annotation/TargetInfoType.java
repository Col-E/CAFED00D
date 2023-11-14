package software.coley.cafedude.classfile.annotation;

import software.coley.cafedude.classfile.AnnotationConstants;

import javax.annotation.Nonnull;

/**
 * Target information denoting which type in a declaration or expression is annotated.
 *
 * @author Matt Coley
 */
public enum TargetInfoType implements AnnotationConstants {
	/**
	 * Type parameter declaration of generic class, interface, method, or constructor.
	 */
	TYPE_PARAMETER_TARGET,
	/**
	 * Type in {@code extends} or {@code implements} clause of class declaration (including the direct superclass or direct
	 * superinterface of an anonymous class declaration), or in {@code extends} clause of interface declaration.
	 */
	SUPERTYPE_TARGET,
	/**
	 * Type in bound of type parameter declaration of generic class, interface, method, or constructor.
	 */
	TYPE_PARAMETER_BOUND_TARGET,
	/**
	 * Potential uses:
	 * <ul>
	 * <li>Type in field or record component declaration.</li>
	 * <li>Type of method, or type of newly constructed object.</li>
	 * <li>Receiver type of method or constructor.</li>
	 * </ul>
	 */
	EMPTY_TARGET,
	/**
	 * Type in formal parameter declaration of method, constructor, or lambda expression.
	 */
	FORMAL_PARAMETER_TARGET,
	/**
	 * Type in {@code throws} clause of method or constructor.
	 */
	THROWS_TARGET,
	/**
	 * Type in local variable declaration.
	 */
	LOCALVAR_TARGET,
	/**
	 * Type in exception parameter declaration.
	 */
	CATCH_TARGET,
	/**
	 * Potential uses:
	 * <ul>
	 *    <li>type in {@code instanceof} expression.</li>
	 *    <li>type in {@code new} expression.</li>
	 *    <li>type in method reference expression using {@code ::new}.</li>
	 *    <li>type in method reference expression using {@code ::Identifier}.</li>
	 * </ul>
	 */
	OFFSET_TARGET,
	/**
	 * Potential uses:
	 * <ul>
	 *     <li>Type in cast expression</li>
	 *     <li>Type argument for generic constructor in new expression or explicit constructor invocation statement.</li>
	 *     <li>Type argument for generic method in method invocation expression.</li>
	 *     <li>Type argument for generic constructor in method reference expression using {@code ::new}.</li>
	 *     <li>Type argument for generic method in method reference expression using {@code ::Identifier}.</li>
	 * </ul>
	 */
	TYPE_ARGUMENT_TARGET;

	/**
	 * Get the associated info type from the type value.
	 *
	 * @param type
	 * 		Target type value.
	 *
	 * @return Target info type based on the value.
	 */
	@Nonnull
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
