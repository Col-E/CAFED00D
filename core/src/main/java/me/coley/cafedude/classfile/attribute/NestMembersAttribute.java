package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

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
	 * 		Name index in constant pool.
	 * @param memberClasses
	 * 		Class indices in constant pool of class that are allowed to declare
	 * 		nest membership of the nest hosted by the current class.
	 */
	public NestMembersAttribute(CpUtf8 name, List<CpClass> memberClasses) {
		super(name);
		this.memberClasses = memberClasses;
	}

	/**
	 * @return Class indices in constant pool of class that are allowed to declare
	 * nest membership of the nest hosted by the current class.
	 */
	public List<CpClass> getMemberClasses() {
		return memberClasses;
	}

	/**
	 * @param memberClasses
	 * 		New class indices in constant pool of class that are allowed to declare
	 * 		nest membership of the nest hosted by the current class.
	 */
	public void setMemberClasses(List<CpClass> memberClasses) {
		this.memberClasses = memberClasses;
	}

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
