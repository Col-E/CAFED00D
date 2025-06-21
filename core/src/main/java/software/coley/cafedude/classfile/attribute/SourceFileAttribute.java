package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
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
	 * 		Constant pool entry holding the attribute name.
	 * @param sourceFilename
	 * 		Constant pool entry holding the source file name.
	 */
	public SourceFileAttribute(@Nonnull CpUtf8 name, @Nonnull CpUtf8 sourceFilename) {
		super(name);
		this.sourceFilename = sourceFilename;
	}

	/**
	 * @return Constant pool entry holding the source file name.
	 */
	@Nonnull
	public CpUtf8 getSourceFilename() {
		return sourceFilename;
	}

	/**
	 * @param sourceFilename
	 * 		New constant pool entry holding the source file name.
	 */
	public void setSourceFilename(@Nonnull CpUtf8 sourceFilename) {
		this.sourceFilename = sourceFilename;
	}

	@Nonnull
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
