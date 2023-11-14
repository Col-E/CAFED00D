package software.coley.cafedude;

/**
 * Exception thrown for invalid code models.
 *
 * @author Justus Garbe
 */
public class InvalidCodeException extends InvalidClassException {
	/**
	 * @param msg
	 * 		Invalid reason.
	 */
	public InvalidCodeException(String msg) {
		super(msg);
	}

	/**
	 * @param t
	 * 		Invalid parent cause.
	 */
	public InvalidCodeException(Throwable t) {
		super(t);
	}
}
