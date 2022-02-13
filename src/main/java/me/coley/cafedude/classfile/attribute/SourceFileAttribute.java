package me.coley.cafedude.classfile.attribute;

import java.util.Set;

/**
 * Source file attribute.
 *
 * @author Matt Coley
 */
public class SourceFileAttribute extends Attribute {
	private int sourceFileNameIndex;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param sourceFileNameIndex
	 * 		UTF8 index in constant pool of the source file name.
	 */
	public SourceFileAttribute(int nameIndex, int sourceFileNameIndex) {
		super(nameIndex);
		this.sourceFileNameIndex = sourceFileNameIndex;
	}

	/**
	 * @return UTF8 index in constant pool of the source file name.
	 */
	public int getSourceFileNameIndex() {
		return sourceFileNameIndex;
	}

	/**
	 * @param sourceFileNameIndex
	 * 		UTF8 index in constant pool of the source file name.
	 */
	public void setSourceFileNameIndex(int sourceFileNameIndex) {
		this.sourceFileNameIndex = sourceFileNameIndex;
	}

	@Override
	public Set<Integer> cpAccesses() {
		Set<Integer> set = super.cpAccesses();
		set.add(getSourceFileNameIndex());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: sourceFileNameIndex
		return 2;
	}
}
