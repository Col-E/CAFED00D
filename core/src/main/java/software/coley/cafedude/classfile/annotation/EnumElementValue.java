package software.coley.cafedude.classfile.annotation;

import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Enum element value.
 *
 * @author Matt Coley
 */
public class EnumElementValue extends ElementValue {
	private CpUtf8 type;
	private CpUtf8 name;

	/**
	 * @param tag
	 * 		ASCII tag representation, must be {@code e}.
	 * @param type
	 * 		Constant pool entry holding the element's type.
	 * @param name
	 * 		Constant pool entry holding the element's name.
	 */
	public EnumElementValue(char tag, @Nonnull CpUtf8 type, @Nonnull CpUtf8 name) {
		super(tag);
		if (tag != 'e')
			throw new IllegalArgumentException("UTF8 element value must have 'e' tag");
		this.type = type;
		this.name = name;
	}

	/**
	 * @return Constant pool entry holding the element's type.
	 */
	@Nonnull
	public CpUtf8 getType() {
		return type;
	}

	/**
	 * @param type
	 * 		New constant pool entry holding the element's type.
	 */
	public void setType(@Nonnull CpUtf8 type) {
		this.type = type;
	}

	/**
	 * @return Constant pool entry holding the element's name.
	 */
	@Nonnull
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @param name
	 * 		New constant pool entry holding the element's name.
	 */
	public void setName(@Nonnull CpUtf8 name) {
		this.name = name;
	}

	/**
	 * @return ASCII tag representation of an enum, {@code e}.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = new HashSet<>();
		set.add(name);
		set.add(type);
		return set;
	}

	@Override
	public int computeLength() {
		// u1: tag
		// u2: enum_type_index
		// u2: enum_name_index
		return 5;
	}
}
