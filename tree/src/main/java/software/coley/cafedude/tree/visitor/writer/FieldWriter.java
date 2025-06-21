package software.coley.cafedude.tree.visitor.writer;

import software.coley.cafedude.classfile.attribute.AttributeConstants;
import software.coley.cafedude.classfile.Field;
import software.coley.cafedude.classfile.attribute.ConstantValueAttribute;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.tree.Constant;
import software.coley.cafedude.tree.visitor.FieldVisitor;

import jakarta.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Field visitor implementation to write back to a {@link Field}.
 *
 * @author Justus Garbe
 */
public class FieldWriter extends DeclarationWriter implements FieldVisitor {
	private final Field field;
	private final Consumer<Field> callback;

	FieldWriter(Symbols symbols, int access, CpUtf8 name, CpUtf8 descriptor, Consumer<Field> callback) {
		super(symbols);
		this.field = new Field(attributes, access, name, descriptor);
		this.callback = callback;
	}

	@Override
	public void visitConstantValue(@Nonnull Constant value) {
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
