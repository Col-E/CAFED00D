package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.annotation.*;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.visitor.AnnotationArrayVisitor;
import me.coley.cafedude.tree.visitor.AnnotationVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AnnotationArrayWriter implements AnnotationArrayVisitor {

	private final Symbols symbols;
	private final Consumer<List<ElementValue>> callback;

	public AnnotationArrayWriter(Symbols symbols, Consumer<List<ElementValue>> callback) {
		this.callback = callback;
		this.symbols = symbols;
	}

	private final List<ElementValue> array = new ArrayList<>();

	@Override
	public void visitArrayValue(Constant value) {
		array.add(symbols.newElementValue(value));
	}

	@Override
	public void visitArrayEnum(String type, String name) {
		array.add(new EnumElementValue('e', symbols.newUtf8(type), symbols.newUtf8(name)));
	}

	@Override
	public AnnotationVisitor visitArrayAnnotation(String type) {
		return new AnnotationWriter(symbols, values -> {
			array.add(new AnnotationElementValue('@', new Annotation(symbols.newUtf8(type), values)));
		});
	}

	@Override
	public @Nullable AnnotationArrayVisitor visitSubArray() {
		return new AnnotationArrayWriter(symbols, array -> {
			this.array.add(new ArrayElementValue('[', array));
		});
	}

	@Override
	public void visitArrayEnd() {
		callback.accept(array);
	}
}
