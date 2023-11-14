package software.coley.cafedude.classfile;

import software.coley.cafedude.classfile.attribute.*;

/**
 * Constants for attribute names.
 *
 * @author Matt Coley
 */
public interface AttributeConstants {
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see AnnotationDefaultAttribute
	 */
	String ANNOTATION_DEFAULT = "AnnotationDefault";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see BootstrapMethodsAttribute
	 */
	String BOOTSTRAP_METHODS = "BootstrapMethods";
	// TODO: Internal attribute?
	/** Attribute string value, used to indicate an attribute's type when parsed. */
	String CHARACTER_RANGE_TABLE = "CharacterRangeTable";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see CodeAttribute
	 */
	String CODE = "Code";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see ConstantValueAttribute
	 */
	String CONSTANT_VALUE = "ConstantValue";
	// TODO: Internal attribute?
	/** Attribute string value, used to indicate an attribute's type when parsed. */
	String COMPILATION_ID = "CompilationID";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see DeprecatedAttribute
	 */
	String DEPRECATED = "Deprecated";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see EnclosingMethodAttribute
	 */
	String ENCLOSING_METHOD = "EnclosingMethod";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see ExceptionsAttribute
	 */
	String EXCEPTIONS = "Exceptions";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see InnerClassesAttribute
	 */
	String INNER_CLASSES = "InnerClasses";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see LineNumberTableAttribute
	 */
	String LINE_NUMBER_TABLE = "LineNumberTable";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see LocalVariableTableAttribute
	 */
	String LOCAL_VARIABLE_TABLE = "LocalVariableTable";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see LocalVariableTypeTableAttribute
	 */
	String LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see MethodParametersAttribute
	 */
	String METHOD_PARAMETERS = "MethodParameters";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see ModuleAttribute
	 */
	String MODULE = "Module";
	// TODO: Internal attribute?
	/** Attribute string value, used to indicate an attribute's type when parsed. */
	String MODULE_HASHES = "ModuleHashes";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see ModuleMainClassAttribute
	 */
	String MODULE_MAIN_CLASS = "ModuleMainClass";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see ModulePackagesAttribute
	 */
	String MODULE_PACKAGES = "ModulePackages";
	// TODO: Internal attribute?
	/** Attribute string value, used to indicate an attribute's type when parsed. */
	String MODULE_RESOLUTION = "ModuleResolution";
	// TODO: Internal attribute?
	/** Attribute string value, used to indicate an attribute's type when parsed. */
	String MODULE_TARGET = "ModuleTarget";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see NestHostAttribute
	 */
	String NEST_HOST = "NestHost";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see NestMembersAttribute
	 */
	String NEST_MEMBERS = "NestMembers";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see RecordAttribute
	 */
	String RECORD = "Record";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see AnnotationsAttribute
	 */
	String RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see ParameterAnnotationsAttribute
	 */
	String RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see AnnotationsAttribute
	 */
	String RUNTIME_VISIBLE_TYPE_ANNOTATIONS = "RuntimeVisibleTypeAnnotations";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see AnnotationsAttribute
	 */
	String RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see ParameterAnnotationsAttribute
	 */
	String RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see AnnotationsAttribute
	 */
	String RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = "RuntimeInvisibleTypeAnnotations";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see PermittedClassesAttribute
	 */
	String PERMITTED_SUBCLASSES = "PermittedSubclasses";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see SignatureAttribute
	 */
	String SIGNATURE = "Signature";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see SourceDebugExtensionAttribute
	 */
	String SOURCE_DEBUG_EXTENSION = "SourceDebugExtension";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see SourceFileAttribute
	 */
	String SOURCE_FILE = "SourceFile";
	// TODO: Internal attribute?
	/** Attribute string value, used to indicate an attribute's type when parsed. */
	String SOURCE_ID = "SourceID";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see StackMapTableAttribute
	 */
	String STACK_MAP_TABLE = "StackMapTable";
	/**
	 * Attribute string value, used to indicate an attribute's type when parsed.
	 *
	 * @see SyntheticAttribute
	 */
	String SYNTHETIC = "Synthetic";
}
