package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.AttributeConstants;
import me.coley.cafedude.classfile.Field;
import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.attribute.ConstantValueAttribute;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.visitor.FieldVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FieldWriter extends DeclarationWriter implements FieldVisitor {

	private final List<Attribute> attributes = new ArrayList<>();
	private final Field field;
	private final Consumer<Field> callback;

	public FieldWriter(Symbols symbols, int access, int nameIndex, int descriptorIndex, Consumer<Field> callback) {
		super(symbols);
		this.field = new Field(attributes, access, nameIndex, descriptorIndex);
		this.callback = callback;
	}

	@Override
	public void visitConstantValue(Constant value) {
		attributes.add(new ConstantValueAttribute(
				symbols.newUtf8(AttributeConstants.CONSTANT_VALUE),
				symbols.newConstant(value)));
	}

	@Override
	public void visitFieldEnd() {
		super.visitDeclarationEnd();
		callback.accept(field);
	}
}