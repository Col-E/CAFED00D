package software.coley.cafedude.classfile.constant;

import jakarta.annotation.Nonnull;
import software.coley.cafedude.classfile.ConstPool;

/**
 * Reference implementation used by internals of {@link ConstPool}.
 * You should <i>never</i> see these while operating on class files.
 *
 * @author Matt Coley
 */
public non-sealed class ConstRefInternal extends ConstRef {
	/**
	 * @param classRef
	 * 		Some class reference.
	 * @param nameType
	 * 		Some member name/type reference.
	 */
	public ConstRefInternal(@Nonnull CpClass classRef, @Nonnull CpNameType nameType) {
		super(-1, classRef, nameType);
	}
}
