package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Annotation collection attribute. Represents either:
 * <ul>
 *     <li>Standard annotations: {@code RuntimeInvisibleAnnotations} &amp; {@code RuntimeVisibleAnnotations}</li>
 *     <li>Type annotations: {@code RuntimeInvisibleTypeAnnotations} &amp; {@code RuntimeVisibleTypeAnnotations}</li>
 * </ul>
 *
 * @author Matt Coley
 */
public class AnnotationsAttribute extends Attribute {
	private List<Annotation> annotations;
	private boolean visible;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param annotations
	 * 		List of annotations.
	 * @param visible
	 * 		Whether the annotations are visible at runtime.
	 */
	public AnnotationsAttribute(@Nonnull CpUtf8 name, @Nonnull List<Annotation> annotations, boolean visible) {
		super(name);
		this.annotations = annotations;
		this.visible = visible;
	}

	/**
	 * @return List of annotations.
	 */
	@Nonnull
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	/**
	 * @param annotations
	 * 		List of annotations.
	 */
	public void setAnnotations(@Nonnull List<Annotation> annotations) {
		this.annotations = annotations;
	}

	/**
	 * @return Whether the annotations are visible at runtime.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible
	 * 		Whether the annotations are visible at runtime.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		for (Annotation annotation : getAnnotations())
			set.addAll(annotation.cpAccesses());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// u2 num_annotations + annotations
		return 2 + annotations.stream().mapToInt(Annotation::computeLength).sum();
	}
}
