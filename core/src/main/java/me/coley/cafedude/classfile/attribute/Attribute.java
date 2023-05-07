package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Base attribute.
 *
 * @author Matt Coley
 */
public abstract class Attribute implements CpAccessor {
	private final CpUtf8 name;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 */
	public Attribute(@Nonnull CpUtf8 name) {
		this.name = name;

		// TODO: Now that we are passing Cp refs around, we should validate the name matches expected constants
		//  .
		//  protected abstract String getExpectedAttributeName();
		//  .
		//  if (!name.getText().equals(getExpectedAttributeName()))
		//  	throw new IllegalStateException("Attribute name for " + getExpectedAttributeName() +
		//       " was wrong: " + name.getText());
	}

	/**
	 * @return Constant pool entry holding the attribute name.
	 */
	@Nonnull
	public CpUtf8 getName() {
		return name;
	}

	/**
	 * @return Computed size for the internal length value of this attribute for serialization.
	 */
	public abstract int computeInternalLength();

	/**
	 * Complete length is the {@link #getName() U2:name_index}
	 * plus the {@link #computeInternalLength() U4:attribute_length}
	 * plus the {@link #computeInternalLength() internal length}
	 *
	 * @return Computed size for the complete attribute.
	 */
	public final int computeCompleteLength() {
		// u2: Name index
		// u4: Attribute length
		// ??: Internal length
		return 6 + computeInternalLength();
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = new HashSet<>();
		set.add(getName());
		return set;
	}
}
