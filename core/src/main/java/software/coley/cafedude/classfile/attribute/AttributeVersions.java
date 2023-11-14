package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.AttributeConstants;
import software.coley.cafedude.classfile.VersionConstants;

import javax.annotation.Nonnull;

/**
 * Attribute relations to class file versions.
 *
 * @author Matt Coley
 */
public class AttributeVersions implements AttributeConstants, VersionConstants {
	/**
	 * For more information on history see:
	 * <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html#jvms-4.7-310">jvms-4.7 Table 4.7-B</a>
	 *
	 * @param attributeName
	 * 		Name of attribute, see {@link AttributeConstants}.
	 *
	 * @return Java version attribute was introduced in.
	 * If the attribute's introduction version is unknown, then {@code -1}.
	 */
	public static int getIntroducedVersion(@Nonnull String attributeName) {
		switch (attributeName) {
			case CODE:
			case CONSTANT_VALUE:
			case DEPRECATED:
			case EXCEPTIONS:
			case INNER_CLASSES:
			case LINE_NUMBER_TABLE:
			case LOCAL_VARIABLE_TABLE:
			case SOURCE_FILE:
			case SYNTHETIC:
				return JAVA1;
			case ANNOTATION_DEFAULT:
			case ENCLOSING_METHOD:
			case LOCAL_VARIABLE_TYPE_TABLE:
			case RUNTIME_INVISIBLE_ANNOTATIONS:
			case RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case RUNTIME_VISIBLE_ANNOTATIONS:
			case RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
			case SIGNATURE:
			case SOURCE_DEBUG_EXTENSION:
				return JAVA5;
			case STACK_MAP_TABLE:
				return JAVA6;
			case BOOTSTRAP_METHODS:
				return JAVA7;
			case METHOD_PARAMETERS:
			case RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				return JAVA8;
			case MODULE:
			case MODULE_MAIN_CLASS:
			case MODULE_PACKAGES:
				return JAVA9;
			case NEST_HOST:
			case NEST_MEMBERS:
				return JAVA11;
			case RECORD:
				// Records first preview in 14
				return JAVA14;
			case PERMITTED_SUBCLASSES:
				// Sealed classes first preview in 15
				return JAVA15;
			default:
				break;
		}
		// TODO: Research unused attributes?
		//  - Some of the following items are listed as consts in the compiler, but are nowhere in the spec...
		//     - CHARACTER_RANGE_TABLE
		//     - COMPILATION_ID
		//     - MODULE_HASHES
		//     - MODULE_RESOLUTION
		//     - MODULE_TARGET
		//     - SOURCE_ID
		return -1;
	}
}
