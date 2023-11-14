package software.coley.cafedude.classfile.constant;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * NameType pool entry. Points to two UTF constants.
 *
 * @author Matt Coley
 */
public class CpNameType extends CpEntry {
	private CpUtf8 name;
	private CpUtf8 type;

	/**
	 * @param name
	 * 		Constant pool entry holding the name.
	 * @param type
	 * 		Constant pool entry holding the type.
	 */
	public CpNameType(@Nonnull CpUtf8 name, @Nonnull CpUtf8 type) {
		super(NAME_TYPE);
		this.name = name;
		this.type = type;
	}

	/**
	 * @return Constant pool entry holding the name.
	 */
	@Nonnull
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @param name
	 * 		New constant pool entry holding the name.
	 */
	public void setName(@Nonnull CpUtf8 name) {
		this.name = name;
	}

	/**
	 * @return Constant pool entry holding the type.
	 */
	@Nonnull
	public CpUtf8 getType() {
		return type;
	}

	/**
	 * @param type
	 * 		New constant pool entry holding the type.
	 */
	public void setType(@Nonnull CpUtf8 type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CpNameType that = (CpNameType) o;
		return name.equals(that.name) && type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}
}
