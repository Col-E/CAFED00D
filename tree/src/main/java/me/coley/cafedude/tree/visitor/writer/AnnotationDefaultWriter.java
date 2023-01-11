package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.annotation.AnnotationElementValue;
import me.coley.cafedude.classfile.annotation.ArrayElementValue;
import me.coley.cafedude.classfile.annotation.ElementValue;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.visitor.AnnotationArrayVisitor;
import me.coley.cafedude.tree.visitor.AnnotationDefaultVisitor;
import me.coley.cafedude.tree.visitor.AnnotationVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AnnotationDefaultWriter implements AnnotationDefaultVisitor {

	private final Symbols symbols;
	private final Consumer<ElementValue> callback;

	public AnnotationDefaultWriter(Symbols symbols, Consumer<ElementValue> callback) {
		this.symbols = symbols;
		this.callback = callback;
	}

	@Override
	public void visitDefaultValue(Constant value) {
		callback.accept(symbols.newElementValue(value));
	}

	@Override
	public @Nullable AnnotationVisitor visitDefaultAnnotation(String type) {
		return new AnnotationWriter(symbols, ev -> {
			Annotation annotation = new Annotation(symbols.newUtf8(type), ev);
			callback.accept(new AnnotationElementValue('@', annotation));
		});
	}

	@Override
	public @Nullable AnnotationArrayVisitor visitDefaultArray() {
		return new AnnotationArrayWriter(symbols, elements -> {
			callback.accept(new ArrayElementValue('[', elements));
		});
	}
}
