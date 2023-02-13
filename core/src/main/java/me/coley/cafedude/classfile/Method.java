package me.coley.cafedude.classfile;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.attribute.CodeAttribute;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.io.AttributeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Method class member.
 *
 * @author Matt Coley
 */
public class Method extends ClassMember {

	private static final Logger logger = LoggerFactory.getLogger(Method.class);

	/**
	 * @param attributes
	 * 		Attributes of the method.
	 * @param access
	 * 		Method access flags.
	 * @param name
	 * 		Name UTF in pool.
	 * @param type
	 * 		Type UTF in pool.
	 */
	public Method(List<Attribute> attributes, int access, CpUtf8 name, CpUtf8 type) {
		super(attributes, access, name, type);
	}

	@Override
	public AttributeContext getHolderType() {
		return AttributeContext.METHOD;
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		for (Attribute attribute : getAttributes()) {
			if(attribute instanceof CodeAttribute) {
				int access = getAccess();
				if(Modifiers.has(access, Modifiers.ACC_NATIVE) || Modifiers.has(access, Modifiers.ACC_ABSTRACT)) {
					// Native and abstract methods cannot have code, but they can still have the attribute.
					logger.warn("Code attribute found on native or abstract method: {}", this);
					continue;
				}
			}
			set.addAll(attribute.cpAccesses());
		}
		return set;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Method other = (Method) obj;
		if (getAccess() != other.getAccess())
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if (getType() == null) {
			return other.getType() == null;
		} else return getType().equals(other.getType());
	}
}
