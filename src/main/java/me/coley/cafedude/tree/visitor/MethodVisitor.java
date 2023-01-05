package me.coley.cafedude.tree.visitor;

import org.jetbrains.annotations.Nullable;

public interface MethodVisitor extends DeclarationVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	@Nullable
	default MethodVisitor methodDelegate() {
		return null;
	}

	@Override
	default MethodVisitor declarationDelegate() {
		return methodDelegate();
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
	 * Visit a thrown exception.
	 *
	 * @param type
	 * 		  Type of the exception.
	 */
	default void visitThrows(String type) {
		MethodVisitor delegate = methodDelegate();
		if(delegate != null) delegate.visitThrows(type);
	}

	/**
	 * Visit the end of the method.
	 */
	default void visitMethodEnd() {
		MethodVisitor delegate = methodDelegate();
		if(delegate != null) delegate.visitMethodEnd();
	}

}
