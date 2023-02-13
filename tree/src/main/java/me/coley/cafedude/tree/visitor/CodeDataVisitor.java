package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.*;
import me.coley.cafedude.tree.insn.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Visits code and converts it to a list of {@link Insn}s.
 */
public class CodeDataVisitor implements CodeVisitor {

	private final List<Insn> insns = new ArrayList<>();
	private final List<Local> locals = new ArrayList<>();
	private final List<ExceptionHandler> handlers = new ArrayList<>();
	private int maxStack;
	private int maxLocals;

	@Override
	public void visitNop() {
		add(Insn.nop());
	}

	@Override
	public void visitThrow() {
		add(Insn.athrow());
	}

	@Override
	public void visitMonitorInsn(int opcode) {
		add(Insn.monitor(opcode));
	}

	@Override
	public void visitArrayInsn(int opcode) {
		add(new ArrayInsn(opcode));
	}

	@Override
	public void visitArithmeticInsn(int opcode) {
		add(new ArithmeticInsn(opcode));
	}

	@Override
	public void visitConstantInsn(int opcode) {
		add(new ConstantInsn(opcode));
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, Descriptor type) {
		add(new FieldInsn(opcode, owner, name, type));
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, Descriptor descriptor) {
		add(new MethodInsn(opcode, owner, name, descriptor));
	}

	@Override
	public void visitFlowInsn(int opcode, @NotNull Label label) {
		add(new FlowInsn(opcode, label));
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		add(new IntInsn(opcode, operand));
	}

	@Override
	public void visitIIncInsn(int var, int increment) {
		add(new IIncInsn(var, increment));
	}

	@Override
	public void visitInvokeDynamicInsn(String name, Descriptor descriptor, Handle bootstrapMethod, Constant... bootstrapArgs) {
		add(new InvokeDynamicInsn(name, descriptor, bootstrapMethod, Arrays.asList(bootstrapArgs)));
	}

	@Override
	public void visitLdcInsn(Constant constant) {
		add(new LdcInsn(Opcodes.LDC, constant)); // assume LDC here, index size is not known.
		// Should be recalculated by writer
	}

	@Override
	public void visitLookupSwitchInsn(Label defaultLabel, int[] keys, Label... labels) {
		List<Integer> keyList = new ArrayList<>();
		for(int key : keys) {
			keyList.add(key);
		}
		add(new LookupSwitchInsn(keyList, Arrays.asList(labels), defaultLabel));
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label defaultLabel, Label... labels) {
		add(new TableSwitchInsn(min, max, Arrays.asList(labels), defaultLabel));
	}

	@Override
	public void visitMultiANewArrayInsn(String type, int dimensions) {
		add(new MultiANewArrayInsn(type, dimensions));
	}

	@Override
	public void visitStackInsn(int opcode) {
		add(new StackInsn(opcode));
	}

	@Override
	public void visitReturnInsn(int opcode) {
		add(new ReturnInsn(opcode));
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		add(new TypeInsn(opcode, Descriptor.from(type)));
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		add(new VarInsn(opcode, var));
	}

	@Override
	public void visitLabel(Label label) {
		add(new LabelInsn(label));
	}

	@Override
	public void visitLocalVariable(int index, String name, Descriptor descriptor, @Nullable String signature, Label start, Label end) {
		Local local = new Local(index);
		local.setName(name);
		local.setDesc(descriptor);
		local.setSignature(signature);
		local.setStart(start);
		local.setEnd(end);
		locals.add(local);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocal) {
		this.maxStack = maxStack;
		this.maxLocals = maxLocal;
	}

	@Override
	public void visitExceptionHandler(@Nullable String type, Label start, Label end, Label handler) {
		handlers.add(new ExceptionHandler(type, start, end, handler));
	}

	public Code getCode() {
		return new Code(insns, locals, handlers, maxStack, maxLocals);
	}

	void add(Insn insn) {
		insns.add(insn);
	}

}
