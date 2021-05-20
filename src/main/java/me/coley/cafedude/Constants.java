package me.coley.cafedude;

import me.coley.cafedude.annotation.TargetInfoType;

/**
 * Class file constants.
 *
 * @author Matt Coley
 */
public interface Constants {
	// TODO: Document and add TLDR what in the class file changed between each version
	int JAVA1 = 45;
	int JAVA2 = 46;
	int JAVA3 = 47;
	int JAVA4 = 48;
	int JAVA5 = 49;
	int JAVA6 = 50;
	int JAVA7 = 51;
	int JAVA8 = 52;
	int JAVA9 = 53;
	int JAVA10 = 54;
	int JAVA11 = 55;
	int JAVA12 = 56;
	int JAVA13 = 57;
	int JAVA14 = 58;
	int JAVA15 = 59;
	int JAVA16 = 60;
	int JAVA17 = 61;

	// TODO: Move this to access flag utility
	int ACC_ANNOTATION = 0x2000;
	int ACC_ABSTRACT = 0x0400;


	/**
	 * Constants for constant pool.
	 */
	interface ConstantPool {
		/** Constant pool identifier for UTF8 values. These values are used by other constants. */
		int UTF8 = 1;
		/** Constant pool identifier for integers. */
		int INTEGER = 3;
		/** Constant pool identifier for floats. */
		int FLOAT = 4;
		/** Constant pool identifier for longs. */
		int LONG = 5;
		/** Constant pool identifier for doubles. */
		int DOUBLE = 6;
		/** Constant pool identifier for classes. */
		int CLASS = 7;
		/** Constant pool identifier for strings. */
		int STRING = 8;
		/** Constant pool identifier for field references. */
		int FIELD_REF = 9;
		/** Constant pool identifier for method references. */
		int METHOD_REF = 10;
		/** Constant pool identifier for interface method references. */
		int INTERFACE_METHOD_REF = 11;
		/** Constant pool identifier for name-type. These are simply name/descriptor pairs. */
		int NAME_TYPE = 12;
		/** Constant pool identifier for method handles. */
		int METHOD_HANDLE = 15;
		/** Constant pool identifier for method types. These are simply descriptor UTF8s. */
		int METHOD_TYPE = 16;
		/** Constant pool identifier for dynamically fetched values to be held by constants. */
		int DYNAMIC = 17;
		/** Constant pool identifier for dynamically fetched method handles. */
		int INVOKE_DYNAMIC = 18;
		/** Constant pool identifier for modules. */
		int MODULE = 19;
		/** Constant pool identifier for packages. */
		int PACKAGE = 20;
	}

	/**
	 * Constants for attributes.
	 */
	interface Attributes {
		// TODO: Differentiate in documentation
		//  - More definitions @ https://github.com/openjdk/jdk/blob/master/src/jdk.compiler/share/classes/com/sun/tools/javac/util/Names.java
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String ANNOTATION_DEFAULT = "AnnotationDefault";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String BOOTSTRAP_METHODS = "BootstrapMethods";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String CHARACTER_RANGE_TABLE = "CharacterRangeTable";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String CODE = "Code";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String CONSTANT_VALUE = "ConstantValue";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String COMPILATION_ID = "CompilationID";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String DEPRECATED = "Deprecated";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String ENCLOSING_METHOD = "EnclosingMethod";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String EXCEPTIONS = "Exceptions";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String INNER_CLASSES = "InnerClasses";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String LINE_NUMBER_TABLE = "LineNumberTable";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String LOCAL_VARIABLE_TABLE = "LocalVariableTable";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String METHOD_PARAMETERS = "MethodParameters";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String MODULE = "Module";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String MODULE_HASHES = "ModuleHashes";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String MODULE_MAIN_CLASS = "ModuleMainClass";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String MODULE_PACKAGES = "ModulePackages";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String MODULE_RESOLUTION = "ModuleResolution";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String MODULE_TARGET = "ModuleTarget";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String NEST_HOST = "NestHost";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String NEST_MEMBERS = "NestMembers";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String RECORD = "Record";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String RUNTIME_VISIBLE_TYPE_ANNOTATIONS = "RuntimeVisibleTypeAnnotations";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = "RuntimeInvisibleTypeAnnotations";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String PERMITTED_SUBCLASSES = "PermittedSubclasses";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String SIGNATURE = "Signature";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String SOURCE_DEBUG_EXTENSION = "SourceDebugExtension";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String SOURCE_FILE = "SourceFile";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String SOURCE_ID = "SourceID";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String STACK_MAP_TABLE = "StackMapTable";
		/** Attribute string value, used to indicate an attribute's type when parsed. */
		String SYNTHETIC = "Synthetic";
	}

	/**
	 * Constants for annotation attributes.
	 */
	interface Annotations {
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
	
	/**
	 * Constants for the stack map table.
	 */
	interface StackMapTable {
		/**
		 * Indicates the verification type top.
		 */
		final int ITEM_Top = 0;
		/**
		 * Indicates the verification type int.
		 */
		final int ITEM_Integer = 1;
		/**
		 * Indicates the verification type float.
		 */
		final int ITEM_Float = 2;
		/**
		 * Indicates the verification type double.
		 */
		final int ITEM_Double = 3;
		/**
		 * Indicates the verification type long.
		 */
		final int ITEM_Long = 4;
		/**
		 * Indicates the verification type null.
		 */
		final int ITEM_Null = 5;
		/**
		 * Indicates the verification type uninitializedThis.
		 */
		final int ITEM_UninitializedThis = 6;
		/**
		 * Indicates the verification type of a class reference.
		 */
		final int ITEM_Object = 7;
		/**
		 * Indicates the verification type uninitialized.
		 */
		final int ITEM_Uninitialized = 8;
		
		/**
		 * The lower bound of the same_frame's frame_type.
		 */
		final int SameFrame_min = 0;
		/**
		 * The upper bound of the same_frame's frame_type.
		 */
		final int SameFrame_max = 63;
		/**
		 * The lower bound of the same_locals_1_stack_item_frame's frame_type.
		 */
		final int SameLocalsOneStackItem_min = 64;
		/**
		 * The upper bound of the same_locals_1_stack_item_frame's frame_type.
		 */
		final int SameLocalsOneStackItem_max = 127;
		/**
		 * The lower bound of the same_locals_1_stack_item_frame_extended's
		 * frame_type.
		 */
		final int SameLocalsOneStackItemExtended_min = 247;
		/**
		 * The upper bound of the same_locals_1_stack_item_frame_extended's
		 * frame_type.
		 */
		final int SameLocalsOneStackItemExtended_max = 247;
		/**
		 * The lower bound of the chop_frame's frame_type.
		 */
		final int ChopFrame_min = 248;
		/**
		 * The upper bound of the chop_frame's frame_type.
		 */
		final int ChopFrame_max = 250;
		/**
		 * The lower bound of the same_frame_extended's frame_type.
		 */
		final int SameFrameExtended_min = 251;
		/**
		 * The upper bound of the same_frame_extended's frame_type.
		 */
		final int SameFrameExtended_max = 251;
		/**
		 * The lower bound of the append_frame's frame_type.
		 */
		final int AppendFrame_min = 252;
		/**
		 * The upper bound of the append_frame's frame_type.
		 */
		final int AppendFrame_max = 254;
		/**
		 * The lower bound of the full_frame's frame_type.
		 */
		final int FullFrame_min = 255;
		/**
		 * The upper bound of the full_frame's frame_type.
		 */
		final int FullFrame_max = 255;
	}
}
