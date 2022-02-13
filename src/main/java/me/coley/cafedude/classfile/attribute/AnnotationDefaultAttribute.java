package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.annotation.ElementValue;

import java.util.Set;

/**
 * Represents the default value of a annotation field <i>(Which are technically methods, but I digress)</i>.
 *
 * @author Matt Coley
 */
public class AnnotationDefaultAttribute extends Attribute {
	private final ElementValue elementValue;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param elementValue
	 * 		Value of the annotation type element represented by the {@code method_info} structure
	 * 		enclosing this attribute.
	 */
	public AnnotationDefaultAttribute(int nameIndex, ElementValue elementValue) {
		super(nameIndex);
		this.elementValue = elementValue;
	}

	/**
	 * @return Value of the annotation type element represented by the {@code method_info} structure
	 * enclosing this attribute.
	 */
	public ElementValue getElementValue() {
		return elementValue;
	}

	@Override
	public Set<Integer> cpAccesses() {
		Set<Integer> set = super.cpAccesses();
		set.addAll(elementValue.cpAccesses());
		return set;
	}

	@Override
	public int computeInternalLength() {
		return getElementValue().computeLength();
	}
}
