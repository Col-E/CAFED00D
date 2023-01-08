package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.annotation.TargetInfo;
import me.coley.cafedude.classfile.annotation.TypeAnnotation;
import me.coley.cafedude.classfile.annotation.TypePath;
import org.jetbrains.annotations.Nullable;

/**
 * Visitor for visiting generic declaration information.
 * @see MethodVisitor
 * @see FieldVisitor
 * @see ClassVisitor
 * @see RecordComponentVisitor
 */
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
	 * @see Annotation
	 */
	@Nullable
	default AnnotationVisitor visitAnnotation(String type, boolean visible) {
		DeclarationVisitor delegate = declarationDelegate();
		if(delegate != null) return delegate.visitAnnotation(type, visible);
		return null;
	}

	/**
	 * Visit a type annotation.
	 *
	 * @param type
	 * 			Class type of the annotation.
	 * @param target
	 * 			Type target of the annotation.
	 * 			See {@link TargetInfo} for more information.
	 * @param path
	 * 			Type path of the annotation.
	 * 			See {@link TypePath} for more information.
	 * @param visible
	 * 			Whether the annotation is visible at runtime.
	 * @return A visitor for the annotation.
	 * @see TypeAnnotation
	 */
	@Nullable
	default AnnotationVisitor visitTypeAnnotation(String type, TargetInfo target, TypePath path, boolean visible) {
		DeclarationVisitor delegate = declarationDelegate();
		if(delegate != null) return delegate.visitTypeAnnotation(type, target, path, visible);
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
	 * 			Whether the member is deprecated.
	 */
	default void visitDeprecated(boolean deprecated) {
		DeclarationVisitor delegate = declarationDelegate();
		if(delegate != null) delegate.visitDeprecated(deprecated);
	}

	/**
	 * Visit the synthetic state. This will always be called and {@code false} will be
	 * passed if the synthetic state is not set.
	 *
	 * @param synthetic
	 * 			Whether the member is synthetic.
	 */
	default void visitSynthetic(boolean synthetic) {
		DeclarationVisitor delegate = declarationDelegate();
		if(delegate != null) delegate.visitSynthetic(synthetic);
	}

}
