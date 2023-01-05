package me.coley.cafedude.classfile.behavior;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.io.AttributeContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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
	 * @param type The type of attribute to search for.
	 * @return Optional of the first attribute of the given type.
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
