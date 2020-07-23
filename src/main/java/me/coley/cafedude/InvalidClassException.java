package me.coley.cafedude;

/**
 * Exception type caused by IO actions on class files.
 *
 * @author Matt Coley
 */
public class InvalidClassException extends Exception {
	/**
	 * @param msg
	 * 		Cause message.
	 */
	public InvalidClassException(String msg) {
		super(msg);
	}

	/**
	 * @param t
	 * 		Cause exception.
	 */
	public InvalidClassException(Throwable t) {
		super(t);
	}
}
