package me.coley.cafedude.tree.visitor;

import org.jetbrains.annotations.Nullable;

public interface FieldVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	@Nullable
	default FieldVisitor fieldDelegate() {
		return null;
	}

	/**
	 * Visit a field annotation.
	 *
	 * @param type
	 * 			Class type of the annotation.
	 * @param visible
	 * 			Whether the annotation is visible at runtime.
	 * @return A visitor for the annotation.
	 */
	@Nullable
	default AnnotationVisitor visitAnnotation(String type, boolean visible) {
		FieldVisitor delegate = fieldDelegate();
		if(delegate != null) return delegate.visitAnnotation(type, visible);
		return null;
	}

	/**
	 * Visit the end of the field.
	 */
	default void visitFieldEnd() {
		FieldVisitor delegate = fieldDelegate();
		if(delegate != null) delegate.visitFieldEnd();
	}

}
