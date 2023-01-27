package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import java.util.Set;

/**
 * Signature attribute, for generic types.
 *
 * @author Matt Coley
 */
public class SignatureAttribute extends Attribute {
	private CpUtf8 signature;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param signature
	 * 		UTF8 index in constant pool of the signature.
	 */
	public SignatureAttribute(CpUtf8 name, CpUtf8 signature) {
		super(name);
		this.signature = signature;
	}

	/**
	 * @return UTF8 index in constant pool of the signature.
	 */
	public CpUtf8 getSignature() {
		return signature;
	}

	/**
	 * @param signature
	 * 		UTF8 index in constant pool of the signature.
	 */
	public void setSignature(CpUtf8 signature) {
		this.signature = signature;
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.add(getSignature());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: signatureIndex
		return 2;
	}
}
