package software.coley.cafedude.tree.visitor.writer;

import software.coley.cafedude.classfile.annotation.*;
import software.coley.cafedude.tree.Constant;
import software.coley.cafedude.tree.visitor.AnnotationArrayVisitor;
import software.coley.cafedude.tree.visitor.AnnotationVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Visitor for visiting annotation array values and writing them back to {@link ElementValue}s.
 *
 * @author Justus Garbe
 */
public class AnnotationArrayWriter implements AnnotationArrayVisitor {
	private final List<ElementValue> array = new ArrayList<>();
	private final Symbols symbols;
	private final Consumer<List<ElementValue>> callback;

	AnnotationArrayWriter(@Nonnull Symbols symbols, @Nonnull Consumer<List<ElementValue>> callback) {
		this.callback = callback;
		this.symbols = symbols;
	}

	@Override
	public void visitArrayValue(@Nonnull Constant value) {
		array.add(symbols.newElementValue(value));
	}

	@Override
	public void visitArrayEnum(@Nonnull String type, @Nonnull String name) {
		array.add(new EnumElementValue('e', symbols.newUtf8(type), symbols.newUtf8(name)));
	}

	@Override
	public AnnotationVisitor visitArrayAnnotation(@Nonnull String type) {
		return new AnnotationWriter(symbols, values -> {
			array.add(new AnnotationElementValue('@', new Annotation(symbols.newUtf8(type), values)));
		});
	}

	@Nullable
	@Override
	public AnnotationArrayVisitor visitSubArray() {
		return new AnnotationArrayWriter(symbols, array -> {
			this.array.add(new ArrayElementValue('[', array));
		});
	}

	@Override
	public void visitArrayEnd() {
		callback.accept(array);
	}
}
