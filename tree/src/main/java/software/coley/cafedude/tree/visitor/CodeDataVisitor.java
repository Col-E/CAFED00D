package software.coley.cafedude.tree.visitor;

import software.coley.cafedude.classfile.Descriptor;
import software.coley.cafedude.classfile.instruction.Opcodes;
import software.coley.cafedude.tree.*;
import software.coley.cafedude.tree.insn.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Visits code and converts it to a list of {@link Insn}s.
 *
 * @author Justus Garbe
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
	public void visitFieldInsn(int opcode, @Nonnull String owner, @Nonnull String name, @Nonnull Descriptor type) {
		add(new FieldInsn(opcode, owner, name, type));
	}

	@Override
	public void visitMethodInsn(int opcode, @Nonnull String owner, @Nonnull String name,
								@Nonnull Descriptor descriptor) {
		add(new MethodInsn(opcode, owner, name, descriptor));
	}

	@Override
	public void visitFlowInsn(int opcode, @Nonnull Label label) {
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
	public void visitInvokeDynamicInsn(@Nonnull String name, @Nonnull Descriptor descriptor,
									   @Nonnull Handle bootstrapMethod, Constant... bootstrapArgs) {
		add(new InvokeDynamicInsn(name, descriptor, bootstrapMethod, Arrays.asList(bootstrapArgs)));
	}

	@Override
	public void visitLdcInsn(@Nonnull Constant constant) {
		add(new LdcInsn(Opcodes.LDC, constant)); // assume LDC here, index size is not known.
		// Should be recalculated by writer
	}

	@Override
	public void visitLookupSwitchInsn(@Nonnull Label defaultLabel, int[] keys, Label... labels) {
		List<Integer> keyList = new ArrayList<>();
		for (int key : keys) {
			keyList.add(key);
		}
		add(new LookupSwitchInsn(keyList, Arrays.asList(labels), defaultLabel));
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, @Nonnull Label defaultLabel, Label... labels) {
		add(new TableSwitchInsn(min, max, Arrays.asList(labels), defaultLabel));
	}

	@Override
	public void visitMultiANewArrayInsn(@Nonnull String type, int dimensions) {
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
	public void visitTypeInsn(int opcode, @Nonnull String type) {
		add(new TypeInsn(opcode, Descriptor.from(type)));
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		add(new VarInsn(opcode, var));
	}

	@Override
	public void visitLabel(@Nonnull Label label) {
		add(new LabelInsn(label));
	}

	@Override
	public void visitLocalVariable(int index, @Nonnull String name, @Nonnull Descriptor descriptor,
								   @Nullable String signature, @Nonnull Label start, @Nonnull Label end) {
		Local local = new Local(index, name, descriptor, signature,start,end);
		locals.add(local);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocal) {
		this.maxStack = maxStack;
		this.maxLocals = maxLocal;
	}

	@Override
	public void visitExceptionHandler(@Nullable String type, @Nonnull Label start, @Nonnull Label end,
									  @Nonnull Label handler) {
		handlers.add(new ExceptionHandler(type, start, end, handler));
	}

	/**
	 * @return {@link Code} object representing the visited code.
	 */
	public Code getCode() {
		return new Code(insns, locals, handlers, maxStack, maxLocals);
	}

	void add(Insn insn) {
		insns.add(insn);
	}

}
