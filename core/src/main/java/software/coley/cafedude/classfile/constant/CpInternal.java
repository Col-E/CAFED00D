package software.coley.cafedude.classfile.constant;

import software.coley.cafedude.classfile.ConstPool;

/**
 * Reference implementation used by internals of {@link ConstPool}.
 * You should <i>never</i> see these while operating on class files.
 *
 * @author Matt Coley
 */
public non-sealed class CpInternal extends CpEntry {
	/**
	 * Create base attribute.
	 *
	 * @param tag
	 * 		Constant's tag.
	 */
	public CpInternal(int tag) {
		super(tag);
	}

	@Override
	public String toString() {
		return "<INTERNAL>";
	}
}
