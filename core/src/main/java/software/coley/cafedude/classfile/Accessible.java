package software.coley.cafedude.classfile;

/**
 * Outline of an item that has access modifiers.
 *
 * @author Matt Coley
 */
public interface Accessible {
	/**
	 * @return Access modifiers mask.
	 */
	int getAccess();

	/**
	 * @param access
	 * 		Access modifiers mask to assign.
	 */
	void setAccess(int access);
}
