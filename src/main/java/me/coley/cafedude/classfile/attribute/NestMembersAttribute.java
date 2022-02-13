package me.coley.cafedude.classfile.attribute;

import java.util.List;

/**
 * Nest members attribute, lists classes allowed to declare membership of the nest hosted by current class.
 *
 * @author Matt Coley
 */
public class NestMembersAttribute extends Attribute {
	private List<Integer> memberClassIndices;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param memberClassIndices
	 * 		Class indices in constant pool of class that are allowed to declare
	 * 		nest membership of the nest hosted by the current class.
	 */
	public NestMembersAttribute(int nameIndex, List<Integer> memberClassIndices) {
		super(nameIndex);
		this.memberClassIndices = memberClassIndices;
	}

	/**
	 * @return Class indices in constant pool of class that are allowed to declare
	 * nest membership of the nest hosted by the current class.
	 */
	public List<Integer> getMemberClassIndices() {
		return memberClassIndices;
	}

	/**
	 * @param memberClassIndices
	 * 		New class indices in constant pool of class that are allowed to declare
	 * 		nest membership of the nest hosted by the current class.
	 */
	public void setMemberClassIndices(List<Integer> memberClassIndices) {
		this.memberClassIndices = memberClassIndices;
	}

	@Override
	public int computeInternalLength() {
		// U2: classCount
		// ??: count * 2
		return 2 + (memberClassIndices.size() * 2);
	}
}
