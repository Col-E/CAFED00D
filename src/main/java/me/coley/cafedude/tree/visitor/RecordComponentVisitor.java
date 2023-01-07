package me.coley.cafedude.tree.visitor;

import org.jetbrains.annotations.Nullable;

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
	default RecordComponentVisitor declarationDelegate() {
		return recordComponentDelegate();
	}

	/**
	 * End of the record component.
	 */
	default void visitRecordComponentEnd() {
		RecordComponentVisitor delegate = recordComponentDelegate();
		if(delegate != null) delegate.visitRecordComponentEnd();
	}
}
