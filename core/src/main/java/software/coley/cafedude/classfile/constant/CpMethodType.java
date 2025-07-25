package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Method type pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public non-sealed class CpMethodType extends CpEntry implements CrossCpReferencing, LoadableConstant {
	private CpUtf8 descriptor;

	/**
	 * @param descriptor
	 * 		Constant pool entry holding the method's descriptor.
	 */
	public CpMethodType(@Nonnull CpUtf8 descriptor) {
		super(METHOD_TYPE);
		this.descriptor = descriptor;
	}

	/**
	 * @return Constant pool entry holding the method's descriptor.
	 */
	@Nonnull
	public CpUtf8 getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		New constant pool entry holding the method's descriptor.
	 */
	public void setDescriptor(@Nonnull CpUtf8 descriptor) {
		this.descriptor = descriptor;
	}

	@Nonnull
	@Override
	public Collection<CpEntry> getReferences() {
		return Collections.singletonList(descriptor);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CpMethodType that)) return false;

		return Objects.equals(descriptor, that.descriptor);
	}

	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}

	@Override
	public String toString() {
		return "method-type=" + descriptor.getText();
	}
}
