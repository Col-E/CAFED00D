package me.coley.cafedude.attribute;

/**
 * Nest host attribute, points to host class.
 *
 * @author Matt Coley
 */
public class NestHostAttribute extends Attribute {
	private int hostClassIndex;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param hostClassIndex
	 * 		Class index in constant pool of class that is the nest host of the current class.
	 */
	public NestHostAttribute(int nameIndex, int hostClassIndex) {
		super(nameIndex);
		this.hostClassIndex = hostClassIndex;
	}

	/**
	 * @return Class index in constant pool of class that is the nest host of the current class.
	 */
	public int getHostClassIndex() {
		return hostClassIndex;
	}

	/**
	 * @param hostClassIndex
	 * 		New class index in constant pool of class that is the nest host of the current class.
	 */
	public void setHostClassIndex(int hostClassIndex) {
		this.hostClassIndex = hostClassIndex;
	}

	@Override
	public int computeInternalLength() {
		// U2: hostClassIndex
		return 2;
	}
}
