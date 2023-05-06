package me.coley.cafedude.classfile;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.io.AttributeContext;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Field class member.
 *
 * @author Matt Coley
 */
public class Field extends ClassMember {
	/**
	 * @param attributes
	 * 		Attributes of the field.
	 * @param access
	 * 		Field access flags.
	 * @param name
	 * 		Index of name UTF in pool.
	 * @param typeIndex
	 * 		Index of descriptor UTF in pool.
	 */
	public Field(List<Attribute> attributes, int access, CpUtf8 name, CpUtf8 type) {
		super(attributes, access, name, type);
	}

	@Nonnull
	@Override
	public AttributeContext getHolderType() {
		return AttributeContext.FIELD;
	}
}
