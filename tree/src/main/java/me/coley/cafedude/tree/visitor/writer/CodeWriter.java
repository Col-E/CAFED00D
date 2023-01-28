package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.AttributeConstants;
import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.instruction.*;
import me.coley.cafedude.io.InstructionWriter;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.Handle;
import me.coley.cafedude.tree.Label;
import me.coley.cafedude.tree.insn.FlowInsn;
import me.coley.cafedude.tree.insn.Insn;
import me.coley.cafedude.tree.insn.LookupSwitchInsn;
import me.coley.cafedude.tree.insn.TableSwitchInsn;
import me.coley.cafedude.tree.visitor.CodeVisitor;
import me.coley.cafedude.tree.visitor.InsnConverter;
import me.coley.cafedude.util.OpcodeUtil;
import me.coley.cafedude.util.Optional;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static me.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import static me.coley.cafedude.classfile.attribute.LocalVariableTableAttribute.VarEntry;
import static me.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute.VarTypeEntry;
import static me.coley.cafedude.classfile.attribute.LineNumberTableAttribute.LineEntry;

public class CodeWriter implements CodeVisitor, Opcodes {

	private final Symbols symbols;
	private final Consumer<CodeAttribute> callback;
	private final List<VarEntry> localVariables = new ArrayList<>();
	private final List<VarTypeEntry> localVariableTypes = new ArrayList<>();
	private final List<CodeAttribute.ExceptionTableEntry> exceptionTable = new ArrayList<>();
	private final List<LineEntry> lineNumbers = new ArrayList<>();
	private final List<Attribute> attributes = new ArrayList<>();
	private int maxStack = 0;
	private int maxLocals = 0;
	private final InsnConverter converter = new InsnConverter();

	public CodeWriter(Symbols symbols, Consumer<CodeAttribute> instructions) {
		this.symbols = symbols;
		this.callback = instructions;
	}

	@Override
	public CodeVisitor codeDelegate() {
		return converter;
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocal) {
		this.maxStack = maxStack;
		this.maxLocals = maxLocal;
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		lineNumbers.add(new LineEntry(line, start.getOffset()));
	}

	@Override
	public void visitLocalVariable(int index, String name, Descriptor descriptor, @Nullable String signature,
								   Label start, Label end) {
		localVariables.add(new VarEntry(start.getOffset(), end.getOffset(),
				symbols.newUtf8(name), symbols.newUtf8(descriptor.getDescriptor()), index));
		if(signature != null) {
			localVariableTypes.add(new VarTypeEntry(start.getOffset(), end.getOffset(),
					symbols.newUtf8(name), symbols.newUtf8(signature), index));
		}
	}

	@Override
	public void visitExceptionHandler(@Nullable String type, Label start, Label end, Label handler) {
		exceptionTable.add(new CodeAttribute.ExceptionTableEntry(start.getOffset(), end.getOffset(),
				handler.getOffset(), Optional.orNull(type, symbols::newClass)));
	}

	@Override
	public void visitCodeEnd() {
		if(!localVariables.isEmpty()) {
			attributes.add(new LocalVariableTableAttribute(
					symbols.newUtf8(AttributeConstants.LOCAL_VARIABLE_TABLE),
					localVariables));
		}
		if(!localVariableTypes.isEmpty()) {
			attributes.add(new LocalVariableTypeTableAttribute(
					symbols.newUtf8(AttributeConstants.LOCAL_VARIABLE_TYPE_TABLE),
					localVariableTypes));
		}
		if(!lineNumbers.isEmpty()) {
			attributes.add(new LineNumberTableAttribute(
					symbols.newUtf8(AttributeConstants.LINE_NUMBER_TABLE),
					lineNumbers));
		}
		List<Insn> instructions = converter.getInstructions();
		InstructionConverter converter = new InstructionConverter(symbols);
		List<BootstrapMethod> bsm = new ArrayList<>();
		List<Instruction> converted = converter.convert(instructions, bsm);
		InstructionWriter writer = new InstructionWriter();
		byte[] code = writer.writeCode(converted);

		if(!bsm.isEmpty()) {
			attributes.add(new BootstrapMethodsAttribute(
					symbols.newUtf8(AttributeConstants.BOOTSTRAP_METHODS),
					bsm));
		}

		callback.accept(new CodeAttribute(
				symbols.newUtf8(AttributeConstants.CODE),
				maxStack, maxLocals, code, exceptionTable, attributes));
	}

}
