package me.coley.cafedude.classfile.behavior;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.io.AttributeContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
	@Nonnull
	List<Attribute> getAttributes();

	/**
	 * Get an attribute by class
	 *
	 * @param <T>
	 * 		The type of attribute to search for.
	 * @param type
	 * 		The type of attribute to search for.
	 *
	 * @return The attribute, or {@code null} if not found.
	 */
	@Nullable
	<T extends Attribute> T getAttribute(Class<T> type);

	/**
	 * @param attributes
	 * 		New list of attributes.
	 */
	void setAttributes(@Nonnull List<Attribute> attributes);

	/**
	 * @return The type of the holder.
	 */
	@Nonnull
	AttributeContext getHolderType();
}
