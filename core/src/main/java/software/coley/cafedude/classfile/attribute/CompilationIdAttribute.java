package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.Set;

/**
 * Compilation identifier attribute.
 * <br>
 * This is a non-standard attribute implemented by OpenJDK.
 *
 * @author Matt Coley
 * @see SourceIdAttribute
 */
public non-sealed class CompilationIdAttribute extends Attribute {
	private CpUtf8 compilationId;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param compilationId
	 * 		Constant pool entry holding the compilation identifier text.
	 */
	public CompilationIdAttribute(@Nonnull CpUtf8 name, @Nonnull CpUtf8 compilationId) {
		super(name);
		this.compilationId = compilationId;
	}

	/**
	 * @return Constant pool entry holding the compilation identifier text.
	 */
	@Nonnull
	public CpUtf8 getCompilationId() {
		return compilationId;
	}

	/**
	 * @param compilationId
	 * 		New constant pool entry holding the compilation identifier text.
	 */
	public void setCompilationId(@Nonnull CpUtf8 compilationId) {
		this.compilationId = compilationId;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(getCompilationId());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: compileId
		return 2;
	}
}
