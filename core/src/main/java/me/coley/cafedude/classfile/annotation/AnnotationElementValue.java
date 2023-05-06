package me.coley.cafedude.classfile.annotation;

import me.coley.cafedude.classfile.constant.CpEntry;

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
	public AnnotationElementValue(char tag, Annotation annotation) {
		super(tag);
		if (tag != '@')
			throw new IllegalArgumentException("Annotation element value must have '@' tag");
		this.annotation = annotation;
	}

	/**
	 * @return Nested annotation declaration.
	 */
	public Annotation getAnnotation() {
		return annotation;
	}

	/**
	 * @param annotation
	 * 		Nested annotation declaration.
	 */
	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	/**
	 * @return ASCII tag representation of a class, {@code @}.
	 */
	@Override
	public char getTag() {
		return super.getTag();
	}

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
