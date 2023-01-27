package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import java.util.Set;

/**
 * Source file attribute.
 *
 * @author Matt Coley
 */
public class SourceFileAttribute extends Attribute {
	private CpUtf8 sourceFilename;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param sourceFilename
	 * 		UTF8 index in constant pool of the source file name.
	 */
	public SourceFileAttribute(CpUtf8 name, CpUtf8 sourceFilename) {
		super(name);
		this.sourceFilename = sourceFilename;
	}

	/**
	 * @return UTF8 index in constant pool of the source file name.
	 */
	public CpUtf8 getSourceFilename() {
		return sourceFilename;
	}

	/**
	 * @param sourceFilename
	 * 		UTF8 index in constant pool of the source file name.
	 */
	public void setSourceFilename(CpUtf8 sourceFilename) {
		this.sourceFilename = sourceFilename;
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(getSourceFilename());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: sourceFilename
		return 2;
	}
}
