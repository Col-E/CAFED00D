package me.coley.cafedude;

public class InvalidCodeException extends InvalidClassException {

	public InvalidCodeException(String msg) {
		super(msg);
	}

	public InvalidCodeException(Throwable t) {
		super(t);
	}
}
