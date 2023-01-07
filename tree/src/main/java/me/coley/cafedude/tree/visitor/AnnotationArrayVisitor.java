package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.tree.Constant;
import org.jetbrains.annotations.Nullable;

/**
 * Visitor for visiting annotation array information.
 */
public interface AnnotationArrayVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	@Nullable
	default AnnotationArrayVisitor arrayDelegate() {
		return null;
	}

	/**
	 * Visit a array element value
	 *
	 * @param value
	 * 		Element value.
	 * @see Constant
	 */
	default void visitArrayValue(Constant value) {
		AnnotationArrayVisitor delegate = arrayDelegate();
		if (delegate != null) delegate.visitArrayValue(value);
	}

	/**
	 * Visit a array enum element value
	 *
	 * @param type
	 * 		Enum type.
	 * @param name
	 * 		Enum name.
	 */
	default void visitArrayEnum(String type, String name) {
		AnnotationArrayVisitor delegate = arrayDelegate();
		if (delegate != null) delegate.visitArrayEnum(type, name);
	}

	/**
	 * Visit a array annotation element value
	 *
	 * @param type
	 * 		Annotation type.
	 * @return Visitor for visiting the annotation.
	 */
	@Nullable
	default AnnotationVisitor visitArrayAnnotation(String type) {
		AnnotationArrayVisitor delegate = arrayDelegate();
		if (delegate != null) return delegate.visitArrayAnnotation(type);
		return null;
	}

	/**
	 * Visit a annotation array element value
	 *
	 * @return Visitor for visiting the array.
	 */
	@Nullable
	default AnnotationArrayVisitor visitSubArray() {
		AnnotationArrayVisitor delegate = arrayDelegate();
		if (delegate != null) return delegate.visitSubArray();
		return null;
	}

	/**
	 * Visit the end of the annotation.
	 */
	default void visitArrayEnd() {
		AnnotationArrayVisitor delegate = arrayDelegate();
		if (delegate != null) delegate.visitArrayEnd();
	}

}
