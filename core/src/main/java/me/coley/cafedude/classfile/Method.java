package me.coley.cafedude.classfile;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.attribute.CodeAttribute;
import me.coley.cafedude.io.AttributeContext;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Method class member.
 *
 * @author Matt Coley
 */
public class Method extends ClassMember{
	/**
	 * @param attributes
	 * 		Attributes of the method.
	 * @param access
	 * 		Method access flags.
	 * @param nameIndex
	 * 		Index of name UTF in pool.
	 * @param typeIndex
	 * 		Index of descriptor UTF in pool.
	 */
	public Method(List<Attribute> attributes, int access, int nameIndex, int typeIndex) {
		super(attributes, access, nameIndex, typeIndex);
	}

	@Override
	public AttributeContext getHolderType() {
		return AttributeContext.METHOD;
	}

	@Override
	public Set<Integer> cpAccesses() {
		Set<Integer> set = new TreeSet<>();
		set.add(getNameIndex());
		set.add(getTypeIndex());
		for (Attribute attribute : getAttributes()) {
			if(attribute instanceof CodeAttribute) {
				int access = getAccess();
				if(Modifiers.has(access, Modifiers.ACC_NATIVE) || Modifiers.has(access, Modifiers.ACC_ABSTRACT))
					// Native and abstract methods cannot have code, but they can still have the attribute.
					continue;
			}
			set.addAll(attribute.cpAccesses());
		}
		return set;
	}
}
