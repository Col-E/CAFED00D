package software.coley.cafedude.tree.visitor.writer;

import software.coley.cafedude.classfile.annotation.*;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.tree.Constant;
import software.coley.cafedude.tree.visitor.AnnotationArrayVisitor;
import software.coley.cafedude.tree.visitor.AnnotationVisitor;

import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Annotation visitor implementation to write back to a {@link Annotation}.
 *
 * @author Justus Garbe
 */
public class AnnotationWriter implements AnnotationVisitor {
	private final Symbols symbols;
	private final Map<CpUtf8, ElementValue> values = new HashMap<>();
	private final Consumer<Map<CpUtf8, ElementValue>> callback;

	AnnotationWriter(Symbols symbols, Consumer<Map<CpUtf8, ElementValue>> callback) {
		this.callback = callback;
		this.symbols = symbols;
	}

	@Override
	public void visitValue(@Nonnull String key, @Nonnull Constant value) {
		values.put(symbols.newUtf8(key), symbols.newElementValue(value));
	}

	@Override
	public AnnotationVisitor visitAnnotation(@Nonnull String key, @Nonnull String type) {
		return new AnnotationWriter(this.symbols, values -> {
			Annotation anno = new Annotation(this.symbols.newUtf8(type), values);
			this.values.put(this.symbols.newUtf8(key), new AnnotationElementValue('@', anno));
		});
	}

	@Override
	public AnnotationArrayVisitor visitArray(@Nonnull String key) {
		return new AnnotationArrayWriter(symbols, array -> {
			values.put(symbols.newUtf8(key), new ArrayElementValue('[', array));
		});
	}

	@Override
	public void visitEnum(@Nonnull String key, @Nonnull String type, @Nonnull String name) {
		values.put(symbols.newUtf8(key), new EnumElementValue('e', symbols.newUtf8(type), symbols.newUtf8(name)));
	}

	@Override
	public void visitAnnotationEnd() {
		callback.accept(values);
	}
}
