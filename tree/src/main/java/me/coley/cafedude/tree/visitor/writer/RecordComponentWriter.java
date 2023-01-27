package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.attribute.RecordAttribute;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.tree.visitor.RecordComponentVisitor;

import java.util.function.Consumer;

public class RecordComponentWriter extends DeclarationWriter implements RecordComponentVisitor {

	private final CpUtf8 name;
	private final CpUtf8 descriptor;
	private final Consumer<RecordAttribute.RecordComponent> callback;

	RecordComponentWriter(Symbols symbols, CpUtf8 name, CpUtf8 descriptor,
						  Consumer<RecordAttribute.RecordComponent> callback) {
		super(symbols);
		this.name = name;
		this.descriptor = descriptor;
		this.callback = callback;
	}

	@Override
	public void visitRecordComponentEnd() {
		callback.accept(new RecordAttribute.RecordComponent(name, descriptor, attributes));
	}
}
