package me.coley.cafedude.attribute;

/**
 * Base attribute.
 *
 * @author Matt Coley
 */
public abstract class Attribute {
	// Constants
	public static final String ANNOTATION_DEFAULT = "AnnotationDefault";
	public static final String BOOTSTRAP_METHODS = "BootstrapMethods";
	public static final String CHARACTER_RANGE_TABLE = "CharacterRangeTable";
	public static final String CODE = "Code";
	public static final String CONSTANT_VALUE = "ConstantValue";
	public static final String COMPILATION_ID = "CompilationID";
	public static final String DEPRECATED = "Deprecated";
	public static final String ENCLOSING_METHOD = "EnclosingMethod";
	public static final String EXCEPTIONS = "Exceptions";
	public static final String INNER_CLASSES = "InnerClasses";
	public static final String LINE_NUMBER_TABLE = "LineNumberTable";
	public static final String LOCAL_VARIABLE_TABLE = "LocalVariableTable";
	public static final String LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable";
	public static final String METHOD_PARAMETERS = "MethodParameters";
	public static final String MODULE = "Module";
	public static final String MODULE_HASHES = "ModuleHashes";
	public static final String MODULE_MAIN_CLASS = "ModuleMainClass";
	public static final String MODULE_PACKAGES = "ModulePackages";
	public static final String MODULE_RESOLUTION = "ModuleResolution";
	public static final String MODULE_TARGET = "ModuleTarget";
	public static final String NEST_HOST = "NestHost";
	public static final String NEST_MEMBERS = "NestMembers";
	public static final String RECORD = "Record";
	public static final String RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations";
	public static final String RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations";
	public static final String RUNTIME_VISIBLE_TYPE_ANNOTATIONS = "RuntimeVisibleTypeAnnotations";
	public static final String RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";
	public static final String RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations";
	public static final String RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = "RuntimeInvisibleTypeAnnotations";
	public static final String PERMITTED_SUBCLASSES = "PermittedSubclasses";
	public static final String SIGNATURE = "Signature";
	public static final String SOURCE_DEBUG_EXTENSION = "SourceDebugExtension";
	public static final String SOURCE_FILE = "SourceFile";
	public static final String SOURCE_ID = "SourceID";
	public static final String STACK_MAP = "StackMap";
	public static final String STACK_MAP_TABLE = "StackMapTable";
	public static final String SYNTHETIC = "Synthetic";
	// Instance fields
	private final int nameIndex;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 */
	public Attribute(int nameIndex) {
		this.nameIndex = nameIndex;
	}

	/**
	 * @return Name index in constant pool.
	 */
	public int getNameIndex() {
		return nameIndex;
	}

	/**
	 * @return Computed size for the internal length value of this attribute for serialization.
	 */
	public abstract int computeInternalLength();

	/**
	 * Complete length is the {@link #getNameIndex() U2:name_index}
	 * plus the {@link #computeInternalLength() U4:attribute_length}
	 * plus the {@link #computeInternalLength() internal length}
	 *
	 * @return Computed size for the complete attribute.
	 */
	public int computeCompleteLength() {
		// u2: Name index
		// u4: Attribute length
		// ??: Internal length
		return 6 + computeInternalLength();
	}
}
