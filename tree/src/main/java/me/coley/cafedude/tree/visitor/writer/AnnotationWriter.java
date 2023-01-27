package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.annotation.*;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.visitor.AnnotationArrayVisitor;
import me.coley.cafedude.tree.visitor.AnnotationVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AnnotationWriter implements AnnotationVisitor {

	private final Symbols symbols;
	private final Map<CpUtf8, ElementValue> values = new HashMap<>();
	private final Consumer<Map<CpUtf8, ElementValue>> callback;

	public AnnotationWriter(Symbols symbols, Consumer<Map<CpUtf8, ElementValue>> callback) {
		this.callback = callback;
		this.symbols = symbols;
	}

	@Override
	public void visitValue(String key, Constant value) {
		values.put(symbols.newUtf8(key), symbols.newElementValue(value));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String key, String type) {
		return new AnnotationWriter(this.symbols, values -> {
			Annotation anno = new Annotation(this.symbols.newUtf8(type), values);
			this.values.put(this.symbols.newUtf8(key), new AnnotationElementValue('@', anno));
		});
	}

	@Override
	public AnnotationArrayVisitor visitArray(String key) {
		return new AnnotationArrayWriter(symbols, array -> {
			values.put(symbols.newUtf8(key), new ArrayElementValue('[', array));
		});
	}

	@Override
	public void visitEnum(String key, String type, String name) {
		values.put(symbols.newUtf8(key), new EnumElementValue('e', symbols.newUtf8(type), symbols.newUtf8(name)));
	}

	@Override
	public void visitAnnotationEnd() {
		callback.accept(values);
	}
}
