package me.coley.cafedude.tree.visitor;

import org.jetbrains.annotations.Nullable;

public interface DeclarationVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	DeclarationVisitor declarationDelegate();

	/**
	 * Visit an annotation.
	 *
	 * @param type
	 * 			Class type of the annotation.
	 * @param visible
	 * 			Whether the annotation is visible at runtime.
	 * @return A visitor for the annotation.
	 */
	@Nullable
	default AnnotationVisitor visitAnnotation(String type, boolean visible) {
		DeclarationVisitor delegate = declarationDelegate();
		if(delegate != null) return delegate.visitAnnotation(type, visible);
		return null;
	}

	/**
	 * Visit a generic signature.
	 * @param signature
	 * 			Generic signature.
	 */
	default void visitSignature(String signature) {
		DeclarationVisitor delegate = declarationDelegate();
		if(delegate != null) delegate.visitSignature(signature);
	}

	/**
	 * Visit the deprecated state. This will always be called and {@code false} will be
	 * passed if the deprecated state is not set.
	 *
	 * @param deprecated
	 * 			Whether the field is deprecated.
	 */
	default void visitDeprecated(boolean deprecated) {
		DeclarationVisitor delegate = declarationDelegate();
		if(delegate != null) delegate.visitDeprecated(deprecated);
	}


}
