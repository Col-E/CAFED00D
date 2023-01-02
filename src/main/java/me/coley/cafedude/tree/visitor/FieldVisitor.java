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
	 * Visit the end of the field.
	 */
	default void visitFieldEnd() {
		FieldVisitor delegate = fieldDelegate();
		if(delegate != null) delegate.visitFieldEnd();
	}

}
