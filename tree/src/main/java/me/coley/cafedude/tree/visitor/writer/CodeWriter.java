package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.InvalidCodeException;
import me.coley.cafedude.classfile.attribute.CodeAttribute;
import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Code;
import me.coley.cafedude.tree.visitor.CodeDataVisitor;
import me.coley.cafedude.tree.visitor.CodeVisitor;

import java.util.function.Consumer;

/**
 * Code visitor for writing back to {@link CodeAttribute}.
 *
 * @author Justus Garbe
 */
public class CodeWriter implements CodeVisitor, Opcodes {
	private final Symbols symbols;
	private final CodeDataVisitor converter = new CodeDataVisitor();
	private final Consumer<CodeAttribute> callback;

	CodeWriter(Symbols symbols, Consumer<CodeAttribute> callback) {
		this.symbols = symbols;
		this.callback = callback;
	}

	@Override
	public CodeVisitor codeDelegate() {
		return converter;
	}

	@Override
	public void visitCodeEnd() throws InvalidCodeException {
		Code code = converter.getCode();
		callback.accept(new CodeConverter(code, symbols).convertToAttribute());
	}
}
