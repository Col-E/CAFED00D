package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.InvalidCodeException;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.instruction.*;
import me.coley.cafedude.tree.Code;
import me.coley.cafedude.tree.visitor.CodeVisitor;
import me.coley.cafedude.tree.visitor.CodeDataVisitor;

import java.util.function.Consumer;

public class CodeWriter implements CodeVisitor, Opcodes {

	private final Symbols symbols;
	private final CodeDataVisitor converter = new CodeDataVisitor();
	private final Consumer<CodeAttribute> callback;

	public CodeWriter(Symbols symbols, Consumer<CodeAttribute> callback) {
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
		callback.accept(new CodeConverter(code, symbols).convert());
	}
	

}
