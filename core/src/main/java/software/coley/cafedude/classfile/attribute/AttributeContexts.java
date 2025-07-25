package software.coley.cafedude.classfile.attribute;

import jakarta.annotation.Nonnull;
import software.coley.cafedude.io.AttributeHolderType;

import java.util.EnumSet;

/**
 * Attribute relations to allowed locations.
 *
 * @author Matt Coley
 */
public class AttributeContexts implements AttributeConstants {
	/**
	 * For more information on location see:
	 * <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html#jvms-4.7-320">jvms-4.7 Table 4.7-C</a>
	 *
	 * @param attributeName
	 * 		Name of attribute, see {@link AttributeConstants}.
	 *
	 * @return Allowed locations for attribute.
	 * If the attribute's allowed locations are unknown, then {@code -1}.
	 */
	@Nonnull
	public static EnumSet<AttributeHolderType> getAllowedContexts(@Nonnull String attributeName) {
		switch (attributeName) {
			case BOOTSTRAP_METHODS:
			case COMPILATION_ID:
			case ENCLOSING_METHOD:
			case INNER_CLASSES:
			case MODULE:
			case MODULE_MAIN_CLASS:
			case MODULE_PACKAGES:
			case MODULE_RESOLUTION:
			case NEST_HOST:
			case NEST_MEMBERS:
			case PERMITTED_SUBCLASSES:
			case RECORD:
			case SOURCE_DEBUG_EXTENSION:
			case SOURCE_FILE:
			case SOURCE_ID:
				return EnumSet.of(AttributeHolderType.CLASS);
			case CONSTANT_VALUE:
				return EnumSet.of(AttributeHolderType.FIELD);
			case ANNOTATION_DEFAULT:
			case CODE:
			case EXCEPTIONS:
			case METHOD_PARAMETERS:
			case RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				return EnumSet.of(AttributeHolderType.METHOD);
			case DEPRECATED:
			case SYNTHETIC:
				return EnumSet.of(AttributeHolderType.CLASS, AttributeHolderType.FIELD, AttributeHolderType.METHOD);
			case LINE_NUMBER_TABLE:
			case LOCAL_VARIABLE_TABLE:
			case LOCAL_VARIABLE_TYPE_TABLE:
			case STACK_MAP_TABLE:
			case CHARACTER_RANGE_TABLE:
				return EnumSet.of(AttributeHolderType.ATTRIBUTE);
			case RUNTIME_VISIBLE_ANNOTATIONS:
			case RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
			case RUNTIME_INVISIBLE_ANNOTATIONS:
			case RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case SIGNATURE:
				return EnumSet.allOf(AttributeHolderType.class);
			default:
				break;
		}
		// Default behavior for unknown attribute, allow anywhere
		return EnumSet.allOf(AttributeHolderType.class);
	}
}
