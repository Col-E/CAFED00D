package me.coley.cafedude.classfile.constant;

import java.util.Objects;

/**
 * Method type pool entry. Points to an UTF constant.
 *
 * @author Matt Coley
 */
public class CpMethodType extends CpEntry {
	private CpUtf8 descriptor;

	/**
	 * @param descriptor
	 * 		Index of method descriptor UTF in pool.
	 */
	public CpMethodType(CpUtf8 descriptor) {
		super(METHOD_TYPE);
		this.descriptor = descriptor;
	}

	/**
	 * @return Index of method descriptor UTF in pool.
	 */
	public CpUtf8 getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor
	 * 		New index of method descriptor UTF in pool.
	 */
	public void setDescriptor(CpUtf8 descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpMethodType that = (CpMethodType) o;
		return Objects.equals(descriptor, that.descriptor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(descriptor);
	}
}
