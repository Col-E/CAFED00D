package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.annotation.ElementValue;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.Set;

/**
 * Represents the default value of an annotation field <i>(Which are technically methods, but I digress)</i>.
 *
 * @author Matt Coley
 */
public non-sealed class AnnotationDefaultAttribute extends Attribute {
	private final ElementValue elementValue;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param elementValue
	 * 		Value of the annotation type element represented by the {@code method_info} structure
	 * 		enclosing this attribute.
	 */
	public AnnotationDefaultAttribute(@Nonnull CpUtf8 name, @Nonnull ElementValue elementValue) {
		super(name);
		this.elementValue = elementValue;
	}

	/**
	 * @return Value of the annotation type element represented by the {@code method_info} structure
	 * enclosing this attribute.
	 */
	@Nonnull
	public ElementValue getElementValue() {
		return elementValue;
	}

	@Nonnull
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
