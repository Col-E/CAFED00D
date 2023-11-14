package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Nest members attribute, lists classes allowed to declare membership of the nest hosted by current class.
 *
 * @author Matt Coley
 */
public class NestMembersAttribute extends Attribute {
	private List<CpClass> memberClasses;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param memberClasses
	 * 		Class indices in constant pool of class that are allowed to declare
	 * 		nest membership of the nest hosted by the current class.
	 */
	public NestMembersAttribute(@Nonnull CpUtf8 name, @Nonnull List<CpClass> memberClasses) {
		super(name);
		this.memberClasses = memberClasses;
	}

	/**
	 * @return Class indices in constant pool of class that are allowed to declare
	 * nest membership of the nest hosted by the current class.
	 */
	@Nonnull
	public List<CpClass> getMemberClasses() {
		return memberClasses;
	}

	/**
	 * @param memberClasses
	 * 		New class indices in constant pool of class that are allowed to declare
	 * 		nest membership of the nest hosted by the current class.
	 */
	public void setMemberClasses(@Nonnull List<CpClass> memberClasses) {
		this.memberClasses = memberClasses;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.addAll(getMemberClasses());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// U2: classCount
		// ??: count * 2
		return 2 + (memberClasses.size() * 2);
	}
}
