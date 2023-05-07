package me.coley.cafedude.tree.visitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Visitor for visiting method information.
 *
 * @author Justus Garbe
 */
public interface MethodVisitor extends DeclarationVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 *
	 * @return Delegate visitor.
	 */
	@Nullable
	default MethodVisitor methodDelegate() {
		return null;
	}

	@Override
	default DeclarationVisitor declarationDelegate() {
		return methodDelegate();
	}

	/**
	 * Visit the code of the method.
	 *
	 * @return A visitor for the code.
	 */
	@Nullable
	default CodeVisitor visitCode() {
		MethodVisitor delegate = methodDelegate();
		if (delegate != null) return delegate.visitCode();
		return null;
	}

	/**
	 * Visit a thrown exception.
	 *
	 * @param type
	 * 		Type of the exception.
	 */
	default void visitThrows(@Nonnull String type) {
		MethodVisitor delegate = methodDelegate();
		if (delegate != null) delegate.visitThrows(type);
	}

	/**
	 * Visit the annotation default value for this method
	 *
	 * @return Visitor for visiting the annotation default value.
	 */
	@Nullable
	default AnnotationDefaultVisitor visitAnnotationDefault() {
		MethodVisitor delegate = methodDelegate();
		if (delegate != null) return delegate.visitAnnotationDefault();
		return null;
	}


	/**
	 * Visit a parameter annotation.
	 *
	 * @param parameter
	 * 		Index of the parameter in the method descriptor.
	 * @param type
	 * 		Class type of the annotation.
	 * @param visible
	 * 		Whether the annotation is visible at runtime.
	 *
	 * @return A visitor for the annotation.
	 */
	@Nullable
	default AnnotationVisitor visitParameterAnnotation(int parameter, @Nonnull String type, boolean visible) {
		MethodVisitor delegate = methodDelegate();
		if (delegate != null) return delegate.visitParameterAnnotation(parameter, type, visible);
		return null;
	}

	/**
	 * Visit a method parameter
	 *
	 * @param name
	 * 		Name of the parameter.
	 * @param access
	 * 		Access flags.
	 */
	default void visitParameter(@Nonnull String name, int access) {
		MethodVisitor delegate = methodDelegate();
		if (delegate != null) delegate.visitParameter(name, access);
	}

	/**
	 * Visit the end of the method.
	 */
	default void visitMethodEnd() {
		MethodVisitor delegate = methodDelegate();
		if (delegate != null) delegate.visitMethodEnd();
	}
}
