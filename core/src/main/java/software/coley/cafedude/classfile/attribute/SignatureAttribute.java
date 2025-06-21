package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
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
	 * 		Constant pool entry holding the attribute name.
	 * @param signature
	 * 		Constant pool entry holding the signature content.
	 */
	public SignatureAttribute(@Nonnull CpUtf8 name, @Nonnull CpUtf8 signature) {
		super(name);
		this.signature = signature;
	}

	/**
	 * @return Constant pool entry holding the signature content.
	 */
	@Nonnull
	public CpUtf8 getSignature() {
		return signature;
	}

	/**
	 * @param signature
	 * 		New constant pool entry holding the signature content.
	 */
	public void setSignature(@Nonnull CpUtf8 signature) {
		this.signature = signature;
	}

	@Nonnull
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
