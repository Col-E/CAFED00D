package software.coley.cafedude.classfile.annotation;

import software.coley.cafedude.classfile.constant.CpEntry;

import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Nested annotation element value.
 *
 * @author Matt Coley
 */
public class AnnotationElementValue extends ElementValue {
	private Annotation annotation;

	/**
	 * @param tag
	 * 		ASCII tag representation, must be {@code c}.
	 * @param annotation
	 * 		Nested annotation declaration.
	 */
	public AnnotationElementValue(char tag, @Nonnull Annotation annotation) {
		super(tag);
		if (tag != ElementValueConstants.TAG_ANNOTATION)
			throw new IllegalArgumentException("Annotation element value must have '@' tag");
		this.annotation = annotation;
	}

	/**
	 * @return Nested annotation declaration.
	 */
	@Nonnull
	public Annotation getAnnotation() {
		return annotation;
	}

	/**
	 * @param annotation
	 * 		Nested annotation declaration.
	 */
	public void setAnnotation(@Nonnull Annotation annotation) {
		this.annotation = annotation;
	}

	/**
	 * @return ASCII tag representation of a class, {@code @}.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		return new HashSet<>(annotation.cpAccesses());
	}

	@Override
	public int computeLength() {
		// u1: tag
		// ??: annotation
		return 1 + annotation.computeLength();
	}
}
