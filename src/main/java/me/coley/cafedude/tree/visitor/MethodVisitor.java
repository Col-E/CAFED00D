package me.coley.cafedude.tree.visitor;

import org.jetbrains.annotations.Nullable;

public interface MethodVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	@Nullable
	default MethodVisitor methodDelegate() {
		return null;
	}

	/**
	 * Visit the code of the method.
	 * @return A visitor for the code.
	 */
	default CodeVisitor visitCode() {
		MethodVisitor delegate = methodDelegate();
		if(delegate != null) return delegate.visitCode();
		return null;
	}

	/**
	 * Visit a method annotation.
	 *
	 * @param type
	 * 			Class type of the annotation.
	 * @param visible
	 * 			Whether the annotation is visible at runtime.
	 * @return A visitor for the annotation.
	 */
	@Nullable
	default AnnotationVisitor visitAnnotation(String type, boolean visible) {
		MethodVisitor delegate = methodDelegate();
		if(delegate != null) return delegate.visitAnnotation(type, visible);
		return null;
	}

	/**
	 * Visit the end of the method.
	 */
	default void visitMethodEnd() {
		MethodVisitor delegate = methodDelegate();
		if(delegate != null) delegate.visitMethodEnd();
	}

}
