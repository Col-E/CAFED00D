package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.annotation.Annotation;

import java.util.List;

/**
 * Annotation collection attribute. Represents either:
 * <ul>
 *     <li>{@code RuntimeInvisibleAnnotations}</li>>
 *     <li>{@code RuntimeVisibleAnnotations}</li>>
 * </ul>
 *
 * @author Matt Coley
 */
public class AnnotationsAttribute extends Attribute {
	private List<Annotation> annotations;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param annotations
	 * 		List of annotations.
	 */
	public AnnotationsAttribute(int nameIndex, List<Annotation> annotations) {
		super(nameIndex);
		this.annotations = annotations;
	}

	/**
	 * @return List of annotations.
	 */
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	/**
	 * @param annotations
	 * 		List of annotations.
	 */
	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
	}

	@Override
	public int computeInternalLength() {
		// u2 num_annotations + annotations
		return 2 + annotations.stream().mapToInt(Annotation::computeLength).sum();
	}
}
