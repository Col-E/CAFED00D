package me.coley.cafedude.attribute;

/**
 * Signature attribute, for generic types.
 *
 * @author Matt Coley
 */
public class SignatureAttribute extends Attribute {
	private int signatureIndex;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param signatureIndex
	 * 		UTF8 index in constant pool of the signature.
	 */
	public SignatureAttribute(int nameIndex, int signatureIndex) {
		super(nameIndex);
		this.signatureIndex = signatureIndex;
	}

	/**
	 * @return UTF8 index in constant pool of the signature.
	 */
	public int getSignatureIndex() {
		return signatureIndex;
	}

	/**
	 * @param signatureIndex
	 * 		UTF8 index in constant pool of the signature.
	 */
	public void setSignatureIndex(int signatureIndex) {
		this.signatureIndex = signatureIndex;
	}

	@Override
	public int computeInternalLength() {
		// U2: signatureIndex
		return 2;
	}
}
