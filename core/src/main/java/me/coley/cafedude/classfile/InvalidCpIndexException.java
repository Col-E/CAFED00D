package me.coley.cafedude.classfile;

public class InvalidCpIndexException extends IndexOutOfBoundsException {

	private final ConstPool pool;
	private final int index;

	public InvalidCpIndexException(ConstPool pool, int index) {
		this.index = index;
		this.pool = pool;
	}

	public ConstPool getPool() {
		return pool;
	}

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
