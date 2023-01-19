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
import me.coley.cafedude.util.OpcodeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static me.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import static me.coley.cafedude.classfile.attribute.LocalVariableTableAttribute.VarEntry;
import static me.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute.VarTypeEntry;
import static me.coley.cafedude.classfile.attribute.LineNumberTableAttribute.LineEntry;

public class CodeWriter implements CodeVisitor, Opcodes {

	private final Map<Integer, Instruction> instructions = new HashMap<>();
	private final Map<Integer, Label> labels = new HashMap<>();
	private final Symbols symbols;
	private final Consumer<CodeAttribute> callback;
	private final Map<Integer, FlowInsn> flowInsns = new HashMap<>();
	private final Map<Integer, TableSwitchInsn> tableSwitchInsns = new HashMap<>();
	private final Map<Integer, LookupSwitchInsn> lookupSwitchInsns = new HashMap<>();
	private final List<BootstrapMethod> bootstrapMethods = new ArrayList<>();
	private final List<VarEntry> localVariables = new ArrayList<>();
	private final List<VarTypeEntry> localVariableTypes = new ArrayList<>();
	private final List<CodeAttribute.ExceptionTableEntry> exceptionTable = new ArrayList<>();
	private final List<LineEntry> lineNumbers = new ArrayList<>();
	private final List<Attribute> attributes = new ArrayList<>();
	private int maxStack = 0;
	private int maxLocals = 0;
	private int offset = 0;
	public CodeWriter(Symbols symbols, Consumer<CodeAttribute> instructions) {
		this.symbols = symbols;
		this.callback = instructions;
	}

	@Override
	public void visitNop() {
		add(new BasicInstruction(NOP));
	}

	@Override
	public void visitThrow() {
		add(new BasicInstruction(ATHROW));
	}

	@Override
	public void visitMonitorInsn(int opcode) {
		add(new BasicInstruction(opcode));
	}

	@Override
	public void visitReturnInsn(int opcode) {
		add(new BasicInstruction(opcode));
	}

	@Override
	public void visitConstantInsn(int opcode) {
		add(new BasicInstruction(opcode));
	}

	@Override
	public void visitArithmeticInsn(int opcode) {
		add(new BasicInstruction(opcode));
	}

	@Override
	public void visitArrayInsn(int opcode) {
		add(new BasicInstruction(opcode));
	}

	@Override
	public void visitStackInsn(int opcode) {
		add(new BasicInstruction(opcode));
	}

	@Override
	public void visitLabel(Label label) {
		labels.put(offset, label);
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		lineNumbers.add(new LineEntry(start.getOffset(), line));
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		add(new IntOperandInstruction(opcode, operand));
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		// convert load insn < 4 to load_0, load_1, etc.
		if(var < 4) {
			if(opcode >= ILOAD && opcode <= ALOAD) {
				opcode = ILOAD_0 + ((opcode - ILOAD) * 4) + var;
			} else if(opcode >= ISTORE && opcode <= ASTORE) {
				opcode = ISTORE_0 + ((opcode - ISTORE) * 4) + var;
			}
			add(new BasicInstruction(opcode));
		} else {
			add(new IntOperandInstruction(opcode, var));
		}
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		add(new IntOperandInstruction(opcode, symbols.newClass(type)));
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, Descriptor type) {
		add(new IntOperandInstruction(opcode, symbols.newField(owner, name, type)));
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, Descriptor descriptor) {
		int methodRef;
		if(opcode == INVOKEINTERFACE) {
			methodRef = symbols.newInterfaceMethod(owner, name, descriptor);
		} else {
			methodRef = symbols.newMethod(owner, name, descriptor);
		}
		add(new IntOperandInstruction(opcode, methodRef));
	}

	@Override
	public void visitInvokeDynamicInsn(String name, Descriptor descriptor, Handle bootstrapMethod,
									   Constant... bootstrapArgs) {
		int nameAndTypeIndex = symbols.newNameType(name, descriptor);
		int handleIndex = symbols.newHandle(bootstrapMethod);
		List<Integer> args = new ArrayList<>();
		for (Constant bootstrapArg : bootstrapArgs) {
			args.add(symbols.newConstant(bootstrapArg));
		}
		BootstrapMethod bsm = new BootstrapMethod(handleIndex, args);
		bootstrapMethods.add(bsm);
		int indyIndex = symbols.newInvokeDynamic(nameAndTypeIndex, bootstrapMethods.size() - 1);
		add(new IntOperandInstruction(INVOKEDYNAMIC, indyIndex));
	}

	@Override
	public void visitFlowInsn(int opcode, Label label) {
		FlowInsn insn = new FlowInsn(opcode, label);
		flowInsns.put(offset, insn);
		offset += 3; // opcode + 2 bytes for offset
	}

	@Override
	public void visitLdcInsn(int opcode, Constant constant) {
		add(new IntOperandInstruction(opcode, symbols.newConstant(constant)));
	}

	@Override
	public void visitIIncInsn(int var, int increment) {
		add(new BiIntOperandInstruction(IINC, var, increment));
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label defaultLabel, Label... labels) {
		TableSwitchInsn insn = new TableSwitchInsn(min, max, Arrays.asList(labels), defaultLabel);
		tableSwitchInsns.put(offset, insn);
		// opcode + padding + default offset + low + high + (labels.length * 4)
		offset += 1 + 3 + 4 + 4 + 4 + labels.length * 4;
	}

	@Override
	public void visitLookupSwitchInsn(Label defaultLabel, int[] keys, Label... labels) {
		List<Integer> keyList = new ArrayList<>();
		for (int key : keys) {
			keyList.add(key);
		}
		lookupSwitchInsns.put(offset, new LookupSwitchInsn(keyList, Arrays.asList(labels), defaultLabel));
		// opcode + padding + default offset + npairs + (npairs * 8)
		offset += 1 + 3 + 4 + 4 + 4 * keys.length + 4 * labels.length;
	}

	@Override
	public void visitMultiANewArrayInsn(String type, int dimensions) {
		add(new BiIntOperandInstruction(MULTIANEWARRAY, symbols.newClass(type), dimensions));
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocal) {
		this.maxStack = maxStack;
		this.maxLocals = maxLocal;
	}

	@Override
	public void visitLocalVariable(int index, String name, Descriptor descriptor, @Nullable String signature, Label start, Label end) {
		localVariables.add(new VarEntry(start.getOffset(), end.getOffset(),
				symbols.newUtf8(name), symbols.newUtf8(descriptor.getDescriptor()), index));
		if(signature != null) {
			localVariableTypes.add(new VarTypeEntry(start.getOffset(), end.getOffset(), index,
					symbols.newUtf8(name), symbols.newUtf8(signature)));
		}
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
		transformUnresolvedLabels();
		InstructionWriter writer = new InstructionWriter();
		byte[] code = writer.writeCode(new ArrayList<>(instructions.values()));
		callback.accept(new CodeAttribute(
				symbols.newUtf8(AttributeConstants.CODE),
				maxStack, maxLocals, code, exceptionTable, attributes));
	}

	private void transformUnresolvedLabels() {
		for (Map.Entry<Integer, FlowInsn> entry : flowInsns.entrySet()) {
			FlowInsn insn = entry.getValue();
			int position = entry.getKey();
			checkLabel(insn.getLabel(), insn, position);
			instructions.put(position, new IntOperandInstruction(insn.getOpcode(), insn.getLabel().getOffset()));
 		}
		for (Map.Entry<Integer, LookupSwitchInsn> entry : lookupSwitchInsns.entrySet()) {
			LookupSwitchInsn insn = entry.getValue();
			int position = entry.getKey();
			List<Integer> offsets = new ArrayList<>();
			for (Label label : insn.getLabels()) {
				checkLabel(label, insn, position);
				offsets.add(label.getOffset());
			}
			checkLabel(insn.getDefaultLabel(), insn, position);
			instructions.put(position, new LookupSwitchInstruction(insn.getDefaultLabel().getOffset(),
					insn.getKeys(), offsets));
		}
		for (Map.Entry<Integer, TableSwitchInsn> entry : tableSwitchInsns.entrySet()) {
			TableSwitchInsn insn = entry.getValue();
			int position = entry.getKey();
			List<Integer> offsets = new ArrayList<>();
			for (Label label : insn.getLabels()) {
				checkLabel(label, insn, position);
				offsets.add(label.getOffset());
			}
			checkLabel(insn.getDefaultLabel(), insn, position);
			instructions.put(position, new TableSwitchInstruction(insn.getMin(), insn.getMax(),
					insn.getDefaultLabel().getOffset(), offsets));
		}
	}

	private void checkLabel(Label label, Insn insn, int position) {
		if(!label.isResolved()) {
			throw new IllegalStateException("Unresolved label at "
					+ OpcodeUtil.getOpcodeName(insn.getOpcode()) + "(" + position + ")");
		}
	}

	void add(Instruction instruction) {
		instructions.put(offset, instruction);
		offset += instruction.computeSize();
	}

}
