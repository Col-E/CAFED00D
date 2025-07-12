package software.coley.cafedude.classfile;

import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.io.AttributeContext;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * Field class member.
 *
 * @author Matt Coley
 */
public non-sealed class Field extends ClassMember {
	/**
	 * @param attributes
	 * 		Attributes of the field.
	 * @param access
	 * 		Field access flags.
	 * @param name
	 * 		Constant pool entry holding the field name.
	 * @param type
	 * 		Constant pool entry holding the field type.
	 */
	public Field(@Nonnull List<Attribute> attributes, int access, @Nonnull CpUtf8 name, @Nonnull CpUtf8 type) {
		super(attributes, access, name, type);
	}

	@Nonnull
	@Override
	public AttributeContext getHolderType() {
		return AttributeContext.FIELD;
	}

	@Override
	public String toString() {
		return getName().getText() + " " + getType().getText();
	}
}
