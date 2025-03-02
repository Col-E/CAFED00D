package software.coley.cafedude.classfile.annotation;

/**
 * Constants for annotation attributes.
 *
 * @author Matt Coley
 */
public interface AnnotationConstants {
	/**
	 * Type parameter declaration of generic class or interface.
	 * <br> Indicates type: {@link TargetInfoType#TYPE_PARAMETER_TARGET}
	 * <br> Location: Class
	 */
	int PARAMETER_OF_CLASS_OR_INTERFACE = 0x00;
	/**
	 * Type parameter declaration of generic method or constructor.
	 * <br> Indicates type: {@link TargetInfoType#TYPE_PARAMETER_TARGET}
	 * <br> Location: Method
	 */
	int PARAMETER_OF_METHOD = 0x01;
	/**
	 * Type in {@code extends} or {@code implements} clause of class declaration
	 * <i>(including the direct superclass or direct superinterface of an anonymous class declaration)</i>,
	 * or in extends clause of interface declaration.
	 * <br> Indicates type: {@link TargetInfoType#SUPERTYPE_TARGET}
	 * <br> Location: Class
	 */
	int SUPERTYPE = 0x10;
	/**
	 * Type in bound of type parameter declaration of generic class or interface.
	 * <br> Indicates type: {@link TargetInfoType#TYPE_PARAMETER_BOUND_TARGET}
	 * <br> Location: Class
	 */
	int BOUND_TYPE_PARAMETER_OF_CLASS = 0x11;
	/**
	 * Type in bound of type parameter declaration of generic method or constructor.
	 * <br> Indicates type: {@link TargetInfoType#TYPE_PARAMETER_BOUND_TARGET}
	 * <br> Location: Method
	 */
	int BOUND_TYPE_PARAMETER_OF_METHOD = 0x12;
	/**
	 * Type in field declaration.
	 * <br> Indicates type: {@link TargetInfoType#EMPTY_TARGET}
	 * <br> Location: Field
	 */
	int FIELD = 0x13;
	/**
	 * return type of method, or type of newly constructed object.
	 * <br> Indicates type: {@link TargetInfoType#EMPTY_TARGET}
	 * <br> Location: Method
	 */
	int METHOD_RETURN_TYPE = 0x14;
	/**
	 * receiver type of method or constructor.
	 * <br> Indicates type: {@link TargetInfoType#EMPTY_TARGET}
	 * <br> Location: Method
	 */
	int METHOD_RECEIVER_TYPE = 0x15;
	/**
	 * Type in formal parameter declaration of method, constructor, or lambda expression.
	 * <br> Indicates type: {@link TargetInfoType#FORMAL_PARAMETER_TARGET}
	 * <br> Location: Method
	 */
	int METHOD_PARAMETER = 0x16;
	/**
	 * Type in {@code throws} clause of method or constructor.
	 * <br> Indicates type: {@link TargetInfoType#THROWS_TARGET}
	 * <br> Location: Method
	 */
	int METHOD_THROWS = 0x17;
	/**
	 * Type in local variable declaration.
	 * <br> Indicates type: {@link TargetInfoType#LOCALVAR_TARGET}
	 * <br> Location: Code
	 */
	int LOCAL_VARIABLE_DECLARATION = 0x40;
	/**
	 * Type in resource variable declaration.
	 * <br> Indicates type: {@link TargetInfoType#LOCALVAR_TARGET}
	 * <br> Location: Code
	 */
	int RESOURCE_VARIABLE_DECLARATION = 0x41;
	/**
	 * Type in exception parameter declaration.
	 * <br> Indicates type: {@link TargetInfoType#CATCH_TARGET}
	 * <br> Location: Code
	 */
	int EXCEPTION_PARAMETER_DECLARATION = 0x42;
	/**
	 * Type in {@code instanceof} expression.
	 * <br> Indicates type: {@link TargetInfoType#OFFSET_TARGET}
	 * <br> Location: Code
	 */
	int INSTANCEOF_EXPRESSION = 0x43;
	/**
	 * Type in {@code new} expression.
	 * <br> Indicates type: {@link TargetInfoType#OFFSET_TARGET}
	 * <br> Location: Code
	 */
	int NEW_EXPRESSION = 0x44;
	/**
	 * Type in method reference expression using {@code ::new}.
	 * <br> Indicates type: {@link TargetInfoType#OFFSET_TARGET}
	 * <br> Location: Code
	 */
	int LAMBDA_NEW_EXPRESSION = 0x45;
	/**
	 * Type in method reference expression using {@code ::Identifier}.
	 * <br> Indicates type: {@link TargetInfoType#OFFSET_TARGET}
	 * <br> Location: Code
	 */
	int LAMBDA_METHOD_REF_EXPRESSION = 0x46;
	/**
	 * Type in cast expression.
	 * <br> Indicates type: {@link TargetInfoType#TYPE_ARGUMENT_TARGET}
	 * <br> Location: Code
	 */
	int CAST_EXPRESSION = 0x47;
	/**
	 * Type argument for generic constructor in new expression or explicit constructor invocation statement.
	 * <br> Indicates type: {@link TargetInfoType#TYPE_ARGUMENT_TARGET}
	 * <br> Location: Code
	 */
	int TYPE_ARGUMENT_OF_NEW_GENERIC_EXPRESSION = 0x48;
	/**
	 * Type argument for generic method in method invocation expression.
	 * <br> Indicates type: {@link TargetInfoType#TYPE_ARGUMENT_TARGET}
	 * <br> Location: Code
	 */
	int TYPE_ARGUMENT_OF_GENERIC_NEW_METHOD_REF_EXPRESSION = 0x49;
	/**
	 * Type argument for generic constructor in method reference expression using {@code ::new}.
	 * <br> Indicates type: {@link TargetInfoType#TYPE_ARGUMENT_TARGET}
	 * <br> Location: Code
	 */
	int TYPE_ARGUMENT_OF_GENERIC_NEW_LAMBDA_CONSTRUCTOR_EXPRESSION = 0x4A;
	/**
	 * Type argument for generic method in method reference expression using {@code ::Identifier}.
	 * <br> Indicates type: {@link TargetInfoType#TYPE_ARGUMENT_TARGET}
	 * <br> Location: Code
	 */
	int TYPE_ARGUMENT_OF_GENERIC_METHOD_REF_EXPRESSION = 0x4B;
}
