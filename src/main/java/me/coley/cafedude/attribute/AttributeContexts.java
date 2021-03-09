package me.coley.cafedude.attribute;

import me.coley.cafedude.Constants.Attributes;
import me.coley.cafedude.io.AttributeContext;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Attribute relations to allowed locations.
 *
 * @author Matt Coley
 */
public class AttributeContexts {
	/**
	 * For more information on location see:
	 * <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html#jvms-4.7-320">jvms-4.7 Table 4.7-C</a>
	 *
	 * @param attributeName
	 * 		Name of attribute, see {@link Attributes}.
	 *
	 * @return Allowed locations for attribute.
	 * If the attribute's allowed locations are unknown, then {@code -1}.
	 */
	public static Collection<AttributeContext> getAllowedContexts(String attributeName) {
		switch (attributeName) {
			case Attributes.BOOTSTRAP_METHODS:
			case Attributes.ENCLOSING_METHOD:
			case Attributes.INNER_CLASSES:
			case Attributes.MODULE:
			case Attributes.MODULE_MAIN_CLASS:
			case Attributes.MODULE_PACKAGES:
			case Attributes.NEST_HOST:
			case Attributes.NEST_MEMBERS:
			case Attributes.PERMITTED_SUBCLASSES:
			case Attributes.RECORD:
			case Attributes.SOURCE_DEBUG_EXTENSION:
			case Attributes.SOURCE_FILE:
				return EnumSet.of(AttributeContext.CLASS);
			case Attributes.CONSTANT_VALUE:
				return EnumSet.of(AttributeContext.FIELD);
			case Attributes.ANNOTATION_DEFAULT:
			case Attributes.CODE:
			case Attributes.EXCEPTIONS:
			case Attributes.METHOD_PARAMETERS:
			case Attributes.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case Attributes.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				return EnumSet.of(AttributeContext.METHOD);
			case Attributes.DEPRECATED:
			case Attributes.SYNTHETIC:
				return EnumSet.of(AttributeContext.CLASS, AttributeContext.FIELD, AttributeContext.METHOD);
			case Attributes.LINE_NUMBER_TABLE:
			case Attributes.LOCAL_VARIABLE_TABLE:
			case Attributes.LOCAL_VARIABLE_TYPE_TABLE:
			case Attributes.STACK_MAP_TABLE:
				return EnumSet.of(AttributeContext.ATTRIBUTE);
			case Attributes.RUNTIME_VISIBLE_ANNOTATIONS:
			case Attributes.RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
			case Attributes.RUNTIME_INVISIBLE_ANNOTATIONS:
			case Attributes.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case Attributes.SIGNATURE:
				return EnumSet.allOf(AttributeContext.class);
			default:
				break;
		}
		// Default behavior for unknown attribute, allow anywhere
		return EnumSet.allOf(AttributeContext.class);
	}
}
