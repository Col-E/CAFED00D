package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.tree.Constant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Visitor for visiting the annotation attribute of any member.
 *
 * @author Justus Garbe
 */
public interface AnnotationVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 *
	 * @return Delegate visitor.
	 */
	@Nullable
	default AnnotationVisitor annotationDelegate() {
		return null;
	}

	/**
	 * Visit a annotation element value
	 *
	 * @param key
	 * 		Element key.
	 * @param value
	 * 		Element value.
	 *
	 * @see Constant
	 */
	default void visitValue(@Nonnull String key, @Nonnull Constant value) {
		AnnotationVisitor delegate = annotationDelegate();
		if (delegate != null) delegate.visitValue(key, value);
	}

	/**
	 * Visit a annotation enum element value
	 *
	 * @param key
	 * 		Element key.
	 * @param type
	 * 		Enum type.
	 * @param name
	 * 		Enum name.
	 */
	default void visitEnum(@Nonnull String key, @Nonnull String type, @Nonnull String name) {
		AnnotationVisitor delegate = annotationDelegate();
		if (delegate != null) delegate.visitEnum(key, type, name);
	}

	/**
	 * Visit a annotation annotation element value
	 *
	 * @param key
	 * 		Element key.
	 * @param type
	 * 		Annotation type.
	 *
	 * @return Visitor for visiting the annotation.
	 */
	@Nullable
	default AnnotationVisitor visitAnnotation(@Nonnull String key, @Nonnull String type) {
		AnnotationVisitor delegate = annotationDelegate();
		if (delegate != null) return delegate.visitAnnotation(key, type);
		return null;
	}

	/**
	 * Visit a annotation array element value
	 *
	 * @param key
	 * 		Element key.
	 *
	 * @return Visitor for visiting the array.
	 */
	@Nullable
	default AnnotationArrayVisitor visitArray(@Nonnull String key) {
		AnnotationVisitor delegate = annotationDelegate();
		if (delegate != null) return delegate.visitArray(key);
		return null;
	}

	/**
	 * Visit the end of the annotation.
	 */
	default void visitAnnotationEnd() {
		AnnotationVisitor delegate = annotationDelegate();
		if (delegate != null) delegate.visitAnnotationEnd();
	}
}
