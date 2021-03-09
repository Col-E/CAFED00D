package me.coley.cafedude.attribute;

import me.coley.cafedude.Constants;
import me.coley.cafedude.Constants.Attributes;

/**
 * Attribute relations to class file versions.
 *
 * @author Matt Coley
 */
public class AttributeVersions {
	/**
	 * For more information on history see:
	 * <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html#jvms-4.7-310">jvms-4.7 Table 4.7-B</a>
	 *
	 * @param attributeName
	 * 		Name of attribute, see {@link Attributes}.
	 *
	 * @return Java version attribute was introduced in.
	 * If the attribute's introduction version is unknown, then {@code -1}.
	 */
	public static int getIntroducedVersion(String attributeName) {
		switch (attributeName) {
			case Attributes.CODE:
			case Attributes.CONSTANT_VALUE:
			case Attributes.DEPRECATED:
			case Attributes.EXCEPTIONS:
			case Attributes.INNER_CLASSES:
			case Attributes.LINE_NUMBER_TABLE:
			case Attributes.LOCAL_VARIABLE_TABLE:
			case Attributes.SOURCE_FILE:
			case Attributes.SYNTHETIC:
				return Constants.JAVA1;
			case Attributes.ANNOTATION_DEFAULT:
			case Attributes.ENCLOSING_METHOD:
			case Attributes.LOCAL_VARIABLE_TYPE_TABLE:
			case Attributes.RUNTIME_INVISIBLE_ANNOTATIONS:
			case Attributes.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case Attributes.RUNTIME_VISIBLE_ANNOTATIONS:
			case Attributes.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
			case Attributes.SIGNATURE:
			case Attributes.SOURCE_DEBUG_EXTENSION:
				return Constants.JAVA5;
			case Attributes.STACK_MAP_TABLE:
				return Constants.JAVA6;
			case Attributes.BOOTSTRAP_METHODS:
				return Constants.JAVA7;
			case Attributes.METHOD_PARAMETERS:
			case Attributes.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case Attributes.RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				return Constants.JAVA8;
			case Attributes.MODULE:
			case Attributes.MODULE_MAIN_CLASS:
			case Attributes.MODULE_PACKAGES:
				return Constants.JAVA9;
			case Attributes.NEST_HOST:
			case Attributes.NEST_MEMBERS:
				return Constants.JAVA11;
			case Attributes.RECORD:
				// Records first preview in 14
				return Constants.JAVA14;
			case Attributes.PERMITTED_SUBCLASSES:
				// Sealed classes first preview in 15
				return Constants.JAVA15;
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
