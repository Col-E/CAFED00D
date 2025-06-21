package software.coley.cafedude.classfile;

import jakarta.annotation.Nonnull;

/**
 * Exception thrown when {@link ConstPool#get(int)} triggers a {@link IndexOutOfBoundsException}.
 *
 * @author Justus Garbe
 */
public class InvalidCpIndexException extends IndexOutOfBoundsException {
	private final ConstPool pool;
	private final int index;

	/**
	 * @param pool
	 * 		Affected pool.
	 * @param index
	 * 		Attempted lookup index.
	 */
	public InvalidCpIndexException(@Nonnull ConstPool pool, int index) {
		this.index = index;
		this.pool = pool;
	}

	/**
	 * @return Affected pool.
	 */
	@Nonnull
	public ConstPool getPool() {
		return pool;
	}

	/**
	 * @return Attempted lookup index.
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "InvalidCpIndexException{" +
				"pool=" + pool +
				", index=" + index +
				'}';
	}
}
