package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.tree.Constant;

import javax.annotation.Nullable;

public interface AnnotationDefaultVisitor {
	/**
	 * Return the delegate visitor for pass through implementations.
	 *
	 * @return Delegate visitor.
	 */
	@Nullable
	default AnnotationDefaultVisitor annotationDefaultDelegate() {
		return null;
	}

	/**
	 * Visit the default value of the annotation.
	 *
	 * @param value
	 * 		Default value.
	 */
	default void visitDefaultValue(Constant value) {
		AnnotationDefaultVisitor delegate = annotationDefaultDelegate();
		if (delegate != null) delegate.visitDefaultValue(value);
	}

	/**
	 * Visit the default enum value of the annotation.
	 *
	 * @param type
	 * 		Enum type.
	 * @param name
	 * 		Enum name.
	 */
	default void visitDefaultEnum(String type, String name) {
		AnnotationDefaultVisitor delegate = annotationDefaultDelegate();
		if (delegate != null) delegate.visitDefaultEnum(type, name);
	}

	/**
	 * Visit the default annotation value of the annotation.
	 *
	 * @param type
	 * 		Annotation type.
	 * @return Visitor for visiting the annotation.
	 */
	@Nullable
	default AnnotationVisitor visitDefaultAnnotation(String type) {
		AnnotationDefaultVisitor delegate = annotationDefaultDelegate();
		if (delegate != null) return delegate.visitDefaultAnnotation(type);
		return null;
	}

	/**
	 * Visit the default array value of the annotation.
	 *
	 * @return Visitor for visiting the array.
	 */
	@Nullable
	default AnnotationArrayVisitor visitDefaultArray() {
		AnnotationDefaultVisitor delegate = annotationDefaultDelegate();
		if (delegate != null) return delegate.visitDefaultArray();
		return null;
	}
}
