package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.attribute.RecordAttribute;
import me.coley.cafedude.tree.visitor.RecordComponentVisitor;

import java.util.function.Consumer;

public class RecordComponentWriter extends DeclarationWriter implements RecordComponentVisitor {

	private final int nameIndex;
	private final int descriptorIndex;
	private final Consumer<RecordAttribute.RecordComponent> callback;

	RecordComponentWriter(Symbols symbols, int nameIndex, int descriptorIndex,
						  Consumer<RecordAttribute.RecordComponent> callback) {
		super(symbols);
		this.nameIndex = nameIndex;
		this.descriptorIndex = descriptorIndex;
		this.callback = callback;
	}

	@Override
	public void visitRecordComponentEnd() {
		callback.accept(new RecordAttribute.RecordComponent(nameIndex, descriptorIndex, attributes));
	}
}
