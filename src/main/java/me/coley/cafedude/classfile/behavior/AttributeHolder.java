package me.coley.cafedude.classfile.behavior;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.io.AttributeContext;

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
	 * @param attributes
	 * 		New list of attributes.
	 */
	void setAttributes(List<Attribute> attributes);

	/**
	 * @return The type of the holder.
	 */
	AttributeContext getHolderType();
}
