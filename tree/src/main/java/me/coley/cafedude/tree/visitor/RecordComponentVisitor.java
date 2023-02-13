package me.coley.cafedude.tree.visitor;

import org.jetbrains.annotations.Nullable;

/**
 * Visitor for visiting record component information.
 */
public interface RecordComponentVisitor extends DeclarationVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	@Nullable
	default RecordComponentVisitor recordComponentDelegate() {
		return null;
	}

	@Override
	default DeclarationVisitor declarationDelegate() {
		return recordComponentDelegate();
	}

	@Override
	@Deprecated
	default void visitSynthetic(boolean synthetic) {
		throw new UnsupportedOperationException("Synthetic flag not supported for record components");
	}

	@Override
	@Deprecated
	default void visitDeprecated(boolean deprecated) {
		throw new UnsupportedOperationException("Deprecated flag not supported for record components");
	}

	/**
	 * End of the record component.
	 */
	default void visitRecordComponentEnd() {
		RecordComponentVisitor delegate = recordComponentDelegate();
		if(delegate != null) delegate.visitRecordComponentEnd();
	}
}
