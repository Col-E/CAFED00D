package software.coley.cafedude.tree.frame;

/**
 * Frame representing a single state of stack and locals.
 *
 * @author Justus Garbe
 */
public class Frame {
	/**
	 * Full frame information. Full stack and locals are given
	 */
	public static final int FULL = 0;
	/**
	 * Append frame information. This frame indicates that the locals are the same as
	 * previous frame except that additional locals are defined, and that the stack
	 * is empty.
	 */
	public static final int APPEND = 1;
	/**
	 * Chop frame information. This frame indicates that the locals are the same as
	 * previous frame except that the last k locals are absent and that the stack is
	 * empty.
	 */
	public static final int CHOP = 2;
	/**
	 * Same frame information. This frame indicates that the locals are the same as the
	 * previous frame and that the stack is empty.
	 */
	public static final int SAME = 3;
	/**
	 * Same locals and one stack item frame information. This frame indicates that the
	 * locals are the same as the previous frame and that the stack contains one item.
	 */
	public static final int SAME1 = 4;

	Value[] locals;
	Value[] stack;

}
