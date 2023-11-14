package software.coley.cafedude.classfile;

import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.attribute.CodeAttribute;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.io.AttributeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
	 * 		Constant pool entry holding the method name.
	 * @param type
	 * 		Constant pool entry holding the method type.
	 */
	public Method(@Nonnull List<Attribute> attributes, int access, @Nonnull CpUtf8 name, @Nonnull CpUtf8 type) {
		super(attributes, access, name, type);
	}

	@Nonnull
	@Override
	public AttributeContext getHolderType() {
		return AttributeContext.METHOD;
	}

	@Nonnull
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
}
