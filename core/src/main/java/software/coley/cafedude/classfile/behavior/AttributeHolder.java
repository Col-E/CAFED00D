package software.coley.cafedude.classfile.behavior;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.ClassMember;
import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.attribute.CodeAttribute;
import software.coley.cafedude.classfile.attribute.RecordAttribute;
import software.coley.cafedude.io.AttributeContext;

import java.util.List;

/**
 * Applied to a data type that have attributes attached to them.
 *
 * @author Matt Coley
 */
public sealed interface AttributeHolder permits ClassFile, ClassMember, CodeAttribute, RecordAttribute.RecordComponent {
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
