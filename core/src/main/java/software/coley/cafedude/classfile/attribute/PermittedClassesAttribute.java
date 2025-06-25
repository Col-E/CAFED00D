package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Permitted classes attribute.
 *
 * @author Matt Coley
 */
public non-sealed class PermittedClassesAttribute extends Attribute {
	private List<CpClass> classes;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param classes
	 * 		Indices of allowed {@code CP_CLASS} values.
	 */
	public PermittedClassesAttribute(@Nonnull CpUtf8 name, @Nonnull List<CpClass> classes) {
		super(name);
		this.classes = classes;
	}

	/**
	 * @return Indices of allowed {@code CP_CLASS} values.
	 */
	@Nonnull
	public List<CpClass> getClasses() {
		return classes;
	}

	/**
	 * @param classes
	 * 		New indices of allowed {@code CP_CLASS} values.
	 */
	public void setClasses(@Nonnull List<CpClass> classes) {
		this.classes = classes;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		set.addAll(getClasses());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// u2: count
		// u2: class_index * count
		return 2 + (2 * classes.size());
	}
}
