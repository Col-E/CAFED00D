package me.coley.cafedude.classfile.behavior;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.io.AttributeContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Applied to a data type that have attributes attached to them.
 *
 * @author Matt Coley
 */
public interface AttributeHolder {
	/**
	 * @return All attributes applied to the current object.
	 */
	List<Attribute> getAttributes();

	/**
	 * Get an attribute by class
	 *
	 * @param <T> The type of attribute to search for.
	 * @param type The type of attribute to search for.
	 * @return The attribute, or null if not found.
	 */
	@Nullable
	<T extends Attribute> T getAttribute(Class<T> type);

	/**
	 * @param attributes
	 * 		New list of attributes.
	 */
	void setAttributes(List<Attribute> attributes);

	/**
	 * @return The type of the holder.
	 */
	AttributeContext getHolderType();
}
