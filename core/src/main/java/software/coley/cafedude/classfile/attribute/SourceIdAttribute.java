package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.Set;

/**
 * Source identifier attribute.
 * <br>
 * This is a non-standard attribute implemented by OpenJDK.
 *
 * @author Matt Coley
 * @see CompilationIdAttribute
 */
public non-sealed class SourceIdAttribute extends Attribute {
	private CpUtf8 sourceId;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param sourceId
	 * 		Constant pool entry holding the source identifier text.
	 */
	public SourceIdAttribute(@Nonnull CpUtf8 name, @Nonnull CpUtf8 sourceId) {
		super(name);
		this.sourceId = sourceId;
	}

	/**
	 * @return Constant pool entry holding the source identifier text.
	 */
	@Nonnull
	public CpUtf8 getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId
	 * 		New constant pool entry holding the source identifier text.
	 */
	public void setSourceId(@Nonnull CpUtf8 sourceId) {
		this.sourceId = sourceId;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(getSourceId());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: sourceId
		return 2;
	}
}
