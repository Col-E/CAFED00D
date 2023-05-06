package me.coley.cafedude.classfile.annotation;

import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * Class element value.
 *
 * @author Matt Coley
 */
public class ClassElementValue extends ElementValue {
	private CpUtf8 classEntry;

	/**
	 * @param tag
	 * 		ASCII tag representation, must be {@code c}.
	 * @param classEntry
	 * 		Index of class constant.
	 */
	public ClassElementValue(char tag, @Nonnull CpUtf8 classEntry) {
		super(tag);
		if (tag != 'c')
			throw new IllegalArgumentException("Class element value must have 'c' tag");
		this.classEntry = classEntry;
	}

	/**
	 * @return Index of a class's descriptor.
	 */
	@Nonnull
	public CpUtf8 getClassEntry() {
		return classEntry;
	}

	/**
	 * @param classEntry
	 * 		Index of a class's descriptor.
	 */
	public void setClassEntry(@Nonnull CpUtf8 classEntry) {
		this.classEntry = classEntry;
	}

	/**
	 * @return ASCII tag representation of a class, {@code c}.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		return Collections.singleton(classEntry);
	}

	@Override
	public int computeLength() {
		// u1: tag
		// u2: class_index
		return 3;
	}
}
