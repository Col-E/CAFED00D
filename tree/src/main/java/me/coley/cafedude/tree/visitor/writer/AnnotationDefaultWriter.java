package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.annotation.AnnotationElementValue;
import me.coley.cafedude.classfile.annotation.ArrayElementValue;
import me.coley.cafedude.classfile.annotation.ElementValue;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.visitor.AnnotationArrayVisitor;
import me.coley.cafedude.tree.visitor.AnnotationDefaultVisitor;
import me.coley.cafedude.tree.visitor.AnnotationVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Annotation visitor implementation to write back the {@link ElementValue default value}
 * portion of an {@link Annotation}
 *
 * @author Justus Garbe
 */
public class AnnotationDefaultWriter implements AnnotationDefaultVisitor {
	private final Symbols symbols;
	private final Consumer<ElementValue> callback;

	public AnnotationDefaultWriter(Symbols symbols, Consumer<ElementValue> callback) {
		this.symbols = symbols;
		this.callback = callback;
	}

	@Override
	public void visitDefaultValue(@Nonnull Constant value) {
		callback.accept(symbols.newElementValue(value));
	}

	@Nullable
	@Override
	public AnnotationVisitor visitDefaultAnnotation(@Nonnull String type) {
		return new AnnotationWriter(symbols, ev -> {
			Annotation annotation = new Annotation(symbols.newUtf8(type), ev);
			callback.accept(new AnnotationElementValue('@', annotation));
		});
	}

	@Nullable
	@Override
	public AnnotationArrayVisitor visitDefaultArray() {
		return new AnnotationArrayWriter(symbols, elements -> {
			callback.accept(new ArrayElementValue('[', elements));
		});
	}
}
