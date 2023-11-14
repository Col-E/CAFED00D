package software.coley.cafedude.transform;

import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.ConstPool;

/**
 * Base transformer outline.
 *
 * @author Matt Coley
 */
public abstract class Transformer {
	/** Target class. */
	protected final ClassFile clazz;
	/** Target class's pool. */
	protected final ConstPool pool;

	/**
	 * @param clazz
	 * 		Class file to modify.
	 */
	public Transformer(ClassFile clazz) {
		this.clazz = clazz;
		pool = clazz.getPool();
	}

	/**
	 * Apply the transformation.
	 */
	public abstract void transform();
}
