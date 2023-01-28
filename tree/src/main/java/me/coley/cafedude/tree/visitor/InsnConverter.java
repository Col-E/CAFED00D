package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.Handle;
import me.coley.cafedude.tree.Label;
import me.coley.cafedude.tree.insn.*;
import me.coley.cafedude.util.OpcodeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Visits code and converts it to a list of {@link Insn}s.
 */
public class InsnConverter implements CodeVisitor {

	List<Insn> instructions = new ArrayList<>();
	int offset = 0;

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
	public void visitFlowInsn(int opcode, Label label) {
		checkLabel(label);
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
		checkLabel(defaultLabel);
		List<Label> labelList = new ArrayList<>();
		for(Label label : labels) {
			checkLabel(label);
			labelList.add(label);
		}
		List<Integer> keyList = new ArrayList<>();
		for(int key : keys) {
			keyList.add(key);
		}
		add(new LookupSwitchInsn(keyList, labelList, defaultLabel));
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label defaultLabel, Label... labels) {
		checkLabel(defaultLabel);
		List<Label> labelList = new ArrayList<>();
		for(Label label : labels) {
			checkLabel(label);
			labelList.add(label);
		}
		add(new TableSwitchInsn(min, max, labelList, defaultLabel));
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
		label.setOffset(offset);
	}

	void add(Insn insn) {
		instructions.add(insn);
		offset += insn.size();
	}

	public List<Insn> getInstructions() {
		return instructions;
	}

	private void checkLabel(Label label) {
		if(!label.isResolved()) {
			throw new IllegalStateException("Unresolved label: " + label);
		}
	}
}
