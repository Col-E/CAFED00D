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
	 * Visit the end of the method.
	 */
	default void visitMethodEnd() {
		MethodVisitor delegate = methodDelegate();
		if(delegate != null) delegate.visitMethodEnd();
	}

}
