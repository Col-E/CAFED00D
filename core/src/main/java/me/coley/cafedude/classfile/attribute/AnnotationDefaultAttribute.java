package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.annotation.ElementValue;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import java.util.Set;

/**
 * Represents the default value of an annotation field <i>(Which are technically methods, but I digress)</i>.
 *
 * @author Matt Coley
 */
public class AnnotationDefaultAttribute extends Attribute {
	private final ElementValue elementValue;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param elementValue
	 * 		Value of the annotation type element represented by the {@code method_info} structure
	 * 		enclosing this attribute.
	 */
	public AnnotationDefaultAttribute(CpUtf8 name, ElementValue elementValue) {
		super(name);
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
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.addAll(elementValue.cpAccesses());
		return set;
	}

	@Override
	public int computeInternalLength() {
		return getElementValue().computeLength();
	}
}
