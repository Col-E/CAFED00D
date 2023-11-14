package software.coley.cafedude.classfile.behavior;

import software.coley.cafedude.classfile.constant.CpEntry;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Applied to a data type that requires access to the constant pool.
 *
 * @author Matt Coley
 */
public interface CpAccessor {
	/**
	 * @return Indices accessed.
	 */
	@Nonnull
	Set<CpEntry> cpAccesses();
}
