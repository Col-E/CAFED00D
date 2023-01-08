package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.tree.Constant;
import org.jetbrains.annotations.Nullable;

/**
 * Visitor for visiting field information.
 */
public interface FieldVisitor extends DeclarationVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	@Nullable
	default FieldVisitor fieldDelegate() {
		return null;
	}

	@Override
	default DeclarationVisitor declarationDelegate() {
		return fieldDelegate();
	}

	/**
	 * Visit a field constant value.
	 *
	 * @param value
	 * 			Constant value.
	 */
	default void visitConstantValue(Constant value) {
		FieldVisitor delegate = fieldDelegate();
		if(delegate != null) delegate.visitConstantValue(value);
	}

	/**
	 * Visit the end of the field.
	 */
	default void visitFieldEnd() {
		FieldVisitor delegate = fieldDelegate();
		if(delegate != null) delegate.visitFieldEnd();
	}

}