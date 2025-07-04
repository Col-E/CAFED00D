package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

/**
 * String pool entry. Points to a UTF constant.
 *
 * @author Matt Coley
 */
public non-sealed class CpString extends CpEntry implements CrossCpReferencing, LoadableConstant {
	private CpUtf8 string;

	/**
	 * @param string
	 * 		Constant pool entry holding the string content.
	 */
	public CpString(@Nonnull CpUtf8 string) {
		super(STRING);
		this.string = string;
	}

	/**
	 * @return Constant pool entry holding the string content.
	 */
	@Nonnull
	public CpUtf8 getString() {
		return string;
	}

	/**
	 * @param string
	 * 		New constant pool entry holding the string content.
	 */
	public void setString(@Nonnull CpUtf8 string) {
		this.string = string;
	}

	@Nonnull
	@Override
	public Collection<CpEntry> getReferences() {
		return Collections.singletonList(string);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CpString cpString = (CpString) o;

		return string.equals(cpString.string);
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}
}
