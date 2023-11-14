package software.coley.cafedude.tree.visitor.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.cafedude.InvalidCodeException;
import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.Descriptor;
import software.coley.cafedude.classfile.Method;
import software.coley.cafedude.classfile.StackMapTableConstants;
import software.coley.cafedude.classfile.attribute.*;
import software.coley.cafedude.classfile.constant.*;
import software.coley.cafedude.classfile.instruction.*;
import software.coley.cafedude.tree.Code;
import software.coley.cafedude.tree.Constant;
import software.coley.cafedude.tree.Handle;
import software.coley.cafedude.tree.Label;
import software.coley.cafedude.tree.frame.*;
import software.coley.cafedude.tree.visitor.CodeDataVisitor;
import software.coley.cafedude.tree.visitor.CodeVisitor;
import software.coley.cafedude.tree.visitor.writer.CodeConverter;
import software.coley.cafedude.util.ConstantUtil;
import software.coley.cafedude.util.OpcodeUtil;
import software.coley.cafedude.util.Optional;

import javax.annotation.Nonnull;
import java.util.*;

import static software.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;

/**
 * Reader for a method's {@link CodeAttribute} to pass it along to a {@link CodeVisitor}.
 *
 * @author Justus Garbe
 * @see CodeConverter Reverse of the process.
 * @see CodeDataVisitor Visitor implementation to create a {@link Code} model.
 */
public class CodeReader {
	private static final Logger logger = LoggerFactory.getLogger(CodeReader.class);
	private final BootstrapMethodsAttribute bsma;
	private final LocalVariableTableAttribute lvta;
	private final LocalVariableTypeTableAttribute lvtta;
	private final StackMapTableAttribute smta;
	private final CodeVisitor cv;
	private final CodeAttribute ca;
	private final Method method;
	private final TreeMap<Integer, Label> labels;
	private final TreeMap<Integer, Instruction> instructions;
	private Stack<Value> stack = new Stack<>();
	private final Stack<Value> locals = new Stack<>();
	private static final Stack<Value> EMPTY = new Stack<>();

	public CodeReader(@Nonnull ClassFile clazz, @Nonnull CodeAttribute ca, @Nonnull CodeVisitor cv, @Nonnull Method method,
					  @Nonnull TreeMap<Integer, Label> labels, @Nonnull TreeMap<Integer, Instruction> instructions) {
		this.bsma = clazz.getAttribute(BootstrapMethodsAttribute.class);
		this.lvta = ca.getAttribute(LocalVariableTableAttribute.class);
		this.lvtta = ca.getAttribute(LocalVariableTypeTableAttribute.class);
		this.smta = ca.getAttribute(StackMapTableAttribute.class);
		this.cv = cv;
		this.ca = ca;
		this.labels = labels;
		this.method = method;
		this.instructions = instructions;
	}

	public void accept() throws InvalidCodeException {
		if (instructions == null) {
			logger.warn("Method visited but no instructions present, Method=" + method.getName().getText());
			return;
		}
		if (instructions.isEmpty()) return; // no instructions, abstract/interface method
		// visit exception handlers
		for (CodeAttribute.ExceptionTableEntry entry : ca.getExceptionTable()) {
			String type = Optional.orNull(entry.getCatchType(), t -> t.getName().getText());
			cv.visitExceptionHandler(type,
					labels.get(entry.getStartPc()),
					labels.get(entry.getEndPc()),
					labels.get(entry.getHandlerPc()));
		}
		Map<Integer, StackMapTableAttribute.StackMapFrame> frames = getStackMapFrames();
		int start = 0;
		int end = 0;
		// get last label in label map
		if (!labels.isEmpty()) {
			end = labels.lastKey();
		}
		for (int pos = start; pos < end; pos++) {
			Label currentLabel = labels.get(pos);
			if (currentLabel != null) {
				cv.visitLabel(currentLabel);
				for (Integer line : currentLabel.getLines()) {
					cv.visitLineNumber(line, currentLabel);
				}
			}
			StackMapTableAttribute.StackMapFrame frame = frames.get(pos);
			if (frame != null) {
				visitFrame(frame);
			}
			Instruction insn = instructions.get(pos);
			if (insn instanceof IntOperandInstruction) {
				visitIntOpInsn((IntOperandInstruction) insn, pos);
			} else if (insn instanceof CpRefInstruction) {
				visitCpRefInsn((CpRefInstruction) insn, pos);
			} else if (insn instanceof IincInstruction) {
				visitIincInsn((IincInstruction) insn);
			} else if (insn instanceof MultiANewArrayInstruction) {
				visitMultiANewArrayInsn((MultiANewArrayInstruction) insn);
			} else if (insn instanceof WideInstruction) {
				Instruction backing = ((WideInstruction) insn).getBacking();
				if (backing instanceof IntOperandInstruction) {
					visitIntOpInsn((IntOperandInstruction) backing, pos);
				} else if (backing instanceof IincInstruction) {
					visitIincInsn((IincInstruction) backing);
				}
			} else if (insn instanceof LookupSwitchInstruction) {
				visitLookupSwitchInsn((LookupSwitchInstruction) insn, pos);
			} else if (insn instanceof TableSwitchInstruction) {
				visitTableSwitchInsn((TableSwitchInstruction) insn, pos);
			} else if (insn instanceof BasicInstruction) {
				visitBasicInsn((BasicInstruction) insn, pos);
			}
		}
		visitLocalVariables();
		cv.visitMaxs(ca.getMaxStack(), ca.getMaxLocals());
		cv.visitCodeEnd();
	}

	private void visitBasicInsn(@Nonnull BasicInstruction insn, int pos) {
		int opcode = insn.getOpcode();
		if (opcode >= Opcodes.ACONST_NULL && opcode <= Opcodes.DCONST_1) {
			cv.visitConstantInsn(opcode);
		} else if ((opcode >= Opcodes.ILOAD_0 && opcode <= Opcodes.ALOAD_3)) {
			int base = opcode - Opcodes.ILOAD_0;
			int var = base % 4;
			int type = base / 4;
			int op = Opcodes.ILOAD + type;
			cv.visitVarInsn(op, var);
		} else if ((opcode >= Opcodes.ISTORE_0 && opcode <= Opcodes.ASTORE_3)) {
			int base = opcode - Opcodes.ISTORE_0;
			int var = base % 4;
			int type = base / 4;
			int op = Opcodes.ISTORE + type;
			cv.visitVarInsn(op, var);
		} else if ((opcode >= Opcodes.IASTORE && opcode <= Opcodes.SASTORE)
				|| ((opcode >= Opcodes.IALOAD && opcode <= Opcodes.SALOAD))
				|| opcode == Opcodes.ARRAYLENGTH) {
			cv.visitArrayInsn(opcode);
		} else if ((opcode >= Opcodes.POP && opcode <= Opcodes.SWAP)) {
			cv.visitStackInsn(opcode);
		} else if ((opcode >= Opcodes.IADD && opcode <= Opcodes.LXOR)
				|| (opcode >= Opcodes.I2L && opcode <= Opcodes.I2S)
				|| (opcode >= Opcodes.LCMP && opcode <= Opcodes.DCMPG)) {
			cv.visitArithmeticInsn(opcode);
		} else if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
			cv.visitReturnInsn(opcode);
		} else {
			switch (opcode) {
				case Opcodes.NOP:
					cv.visitNop();
					break;
				case Opcodes.ATHROW:
					cv.visitThrow();
					break;
				case Opcodes.MONITORENTER:
				case Opcodes.MONITOREXIT:
					cv.visitMonitorInsn(opcode);
					break;
				default:
					throw new IllegalStateException("Unsupported opcode (no operand): "
							+ OpcodeUtil.getOpcodeName(opcode) + " (" + opcode + ")" + " at " + pos);
			}
		}
	}

	private void visitLookupSwitchInsn(@Nonnull LookupSwitchInstruction insn, int pos) {
		List<Integer> keys = insn.getKeys();
		List<Integer> offsets = insn.getOffsets();
		int defaultOffset = insn.getDefault();
		Label defaultLabel = labels.get(pos + defaultOffset);
		Label[] labels = new Label[offsets.size()];
		for (int i = 0; i < offsets.size(); i++) {
			labels[i] = this.labels.get(pos + offsets.get(i));
		}
		int[] keysArr = new int[keys.size()];
		for (int i = 0; i < keys.size(); i++) {
			keysArr[i] = keys.get(i);
		}
		cv.visitLookupSwitchInsn(defaultLabel, keysArr, labels);
	}

	private void visitTableSwitchInsn(@Nonnull TableSwitchInstruction insn, int pos) {
		int min = insn.getLow();
		int max = insn.getHigh();
		int defaultOffset = insn.getDefault();
		Label defaultLabel = labels.get(pos + defaultOffset);
		Label[] labels = new Label[max - min + 1];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = this.labels.get(pos + insn.getOffsets().get(i));
		}
		cv.visitTableSwitchInsn(min, max, defaultLabel, labels);
	}

	private void visitIntOpInsn(@Nonnull IntOperandInstruction ioi, int pos) {
		int operand = ioi.getOperand();
		int opcode = ioi.getOpcode();
		if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH || opcode == Opcodes.NEWARRAY || opcode == Opcodes.RET) {
			cv.visitIntInsn(opcode, operand);
		} else if ((opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD) || (opcode >= Opcodes.ISTORE && opcode <= Opcodes.ASTORE)) {
			cv.visitVarInsn(opcode, operand);
		} else if ((opcode >= Opcodes.IFEQ && opcode <= Opcodes.JSR) || (opcode >= Opcodes.IFNULL && opcode <= Opcodes.JSR_W)) {
			int targetPos = pos + operand;
			Label targetLabel = labels.get(targetPos);
			if (targetLabel == null) {
				throw new IllegalStateException("No label for target position: " + targetPos);
			}
			cv.visitFlowInsn(opcode, targetLabel);
		} else {
			throw new IllegalStateException("Unsupported opcode (integer operand): "
					+ OpcodeUtil.getOpcodeName(opcode) + " " + operand + " (" + opcode + ")" + " at " + pos);
		}
	}

	private void visitCpRefInsn(@Nonnull CpRefInstruction cpr, int pos) {
		int opcode = cpr.getOpcode();
		if (opcode == Opcodes.NEW || opcode == Opcodes.ANEWARRAY || opcode == Opcodes.CHECKCAST || opcode == Opcodes.INSTANCEOF) {
			CpClass cc = (CpClass) cpr.getEntry();
			cv.visitTypeInsn(opcode, cc.getName().getText());
		} else if (opcode >= Opcodes.GETSTATIC && opcode <= Opcodes.PUTFIELD) {
			CpFieldRef fr = (CpFieldRef) cpr.getEntry();
			CpNameType nt = fr.getNameType();
			String name = nt.getName().getText();
			String owner = fr.getClassRef().getName().getText();
			String type = nt.getType().getText();
			cv.visitFieldInsn(opcode, owner, name, Descriptor.from(type));
		} else if (opcode == Opcodes.LDC || opcode == Opcodes.LDC_W || opcode == Opcodes.LDC2_W) {
			cv.visitLdcInsn(ConstantUtil.from(cpr.getEntry()));
		} else if (opcode == Opcodes.INVOKEVIRTUAL
				|| opcode == Opcodes.INVOKESPECIAL
				|| opcode == Opcodes.INVOKESTATIC
				|| opcode == Opcodes.INVOKEINTERFACE) {
			ConstRef cr = (ConstRef) cpr.getEntry();
			CpNameType nt = cr.getNameType();
			String name = nt.getName().getText();
			String owner = cr.getClassRef().getName().getText();
			String type = nt.getType().getText();
			cv.visitMethodInsn(opcode, owner, name, Descriptor.from(type));
		} else if (opcode == Opcodes.INVOKEDYNAMIC) {
			if (bsma == null) {
				throw new IllegalStateException(
						"INVOKEDYNAMIC instruction found, but no BootstrapMethodsAttribute present " +
								"at " + pos);
			}
			CpInvokeDynamic id = (CpInvokeDynamic) cpr.getEntry();
			CpNameType nt = id.getNameType();
			String name = nt.getName().getText();
			String type = nt.getType().getText();
			BootstrapMethod bsm = bsma.getBootstrapMethods().get(id.getBsmIndex());
			CpMethodHandle mh = bsm.getBsmMethodRef();
			ConstRef mr = mh.getReference();
			CpNameType bsmnt = mr.getNameType();
			String bsmName = bsmnt.getName().getText();
			String bsmOwner = mr.getClassRef().getName().getText();
			String bsmType = bsmnt.getType().getText();
			Handle bsmHandle = new Handle(
					Handle.Tag.fromKind(mh.getKind()),
					bsmOwner,
					bsmName,
					Descriptor.from(bsmType));
			Constant[] args = new Constant[bsm.getArgs().size()];
			for (int i = 0; i < args.length; i++) {
				args[i] = ConstantUtil.from(bsm.getArgs().get(i));
			}
			cv.visitInvokeDynamicInsn(name, Descriptor.from(type), bsmHandle, args);
		}
	}

	private void visitIincInsn(@Nonnull IincInstruction iinc) {
		cv.visitIIncInsn(iinc.getVar(), iinc.getIncrement());
	}

	private void visitMultiANewArrayInsn(@Nonnull MultiANewArrayInstruction manai) {
		cv.visitMultiANewArrayInsn(manai.getDescriptor().getName().getText(), manai.getDimensions());
	}

	private void visitLocalVariables() {
		List<LocalVariableTypeTableAttribute.VarTypeEntry> varTypes = Collections.emptyList();
		if (lvtta != null) {
			varTypes = lvtta.getEntries();
		}
		if (lvta != null) {
			for (LocalVariableTableAttribute.VarEntry entry : lvta.getEntries()) {
				String name = entry.getName().getText();
				Descriptor desc = Descriptor.from(entry.getDesc().getText());
				String signature = null;
				for (LocalVariableTypeTableAttribute.VarTypeEntry varType : varTypes) {
					if (varType.getIndex() == entry.getIndex() && varType.getStartPc() == entry.getStartPc()) {
						signature = varType.getSignature().getText();
						break;
					}
				}
				Label start = labels.computeIfAbsent(entry.getStartPc(), Label::new);
				Label end = labels.computeIfAbsent(entry.getStartPc() + entry.getLength(), Label::new);
				cv.visitLocalVariable(entry.getIndex(), name, desc, signature, start, end);
			}
		}
	}

	private void visitFrame(@Nonnull StackMapTableAttribute.StackMapFrame frame) {
		int kind = Frame.FULL;
		int argument = 0;
		if (frame instanceof StackMapTableAttribute.SameFrame || frame instanceof StackMapTableAttribute.SameFrameExtended) {
			kind = Frame.SAME;
			stack = EMPTY;
		} else if (frame instanceof StackMapTableAttribute.SameLocalsOneStackItem) {
			StackMapTableAttribute.SameLocalsOneStackItem slo = (StackMapTableAttribute.SameLocalsOneStackItem) frame;
			stack = new Stack<>();
			stack.push(toValue(slo.getStack()));
			kind = Frame.SAME1;
		} else if (frame instanceof StackMapTableAttribute.SameLocalsOneStackItemExtended) {
			StackMapTableAttribute.SameLocalsOneStackItemExtended slo = (StackMapTableAttribute.SameLocalsOneStackItemExtended) frame;
			stack = new Stack<>();
			stack.push(toValue(slo.getStack()));
			kind = Frame.SAME1;
		} else if (frame instanceof StackMapTableAttribute.ChopFrame) {
			StackMapTableAttribute.ChopFrame cf = (StackMapTableAttribute.ChopFrame) frame;
			argument = cf.getAbsentVariables();
			for (int i = 0; i < argument; i++) {
				locals.pop();
			}
			stack = EMPTY;
			kind = Frame.CHOP;
		} else if (frame instanceof StackMapTableAttribute.AppendFrame) {
			StackMapTableAttribute.AppendFrame af = (StackMapTableAttribute.AppendFrame) frame;
			argument = af.getAdditionalLocals().size();
			for (StackMapTableAttribute.TypeInfo local : af.getAdditionalLocals()) {
				locals.push(toValue(local));
			}
			stack = EMPTY;
			kind = Frame.APPEND;
		} else if (frame instanceof StackMapTableAttribute.FullFrame) {
			StackMapTableAttribute.FullFrame ff = (StackMapTableAttribute.FullFrame) frame;
			for (StackMapTableAttribute.TypeInfo local : ff.getLocals()) {
				locals.push(toValue(local));
			}
			for (StackMapTableAttribute.TypeInfo stackItem : ff.getStack()) {
				stack.push(toValue(stackItem));
			}
		} else {
			throw new IllegalStateException("Unsupported frame type: " + frame.getClass().getName());
		}
		cv.visitFrame(kind, stack.toArray(new Value[0]), locals.toArray(new Value[0]), argument);
	}

	@Nonnull
	private Value toValue(@Nonnull StackMapTableAttribute.TypeInfo typeInfo) {
		switch (typeInfo.getTag()) {
			case StackMapTableConstants.ITEM_TOP:
			case StackMapTableConstants.ITEM_INTEGER:
			case StackMapTableConstants.ITEM_FLOAT:
			case StackMapTableConstants.ITEM_DOUBLE:
			case StackMapTableConstants.ITEM_LONG:
			case StackMapTableConstants.ITEM_NULL:
			case StackMapTableConstants.ITEM_UNINITIALIZED_THIS:
				return new PrimitiveValue(typeInfo.getTag());
			case StackMapTableConstants.ITEM_OBJECT:
				StackMapTableAttribute.ObjectVariableInfo objectInfo = (StackMapTableAttribute.ObjectVariableInfo) typeInfo;
				return new ObjectValue(objectInfo.getClassEntry().getName().getText());
			case StackMapTableConstants.ITEM_UNINITIALIZED:
				StackMapTableAttribute.UninitializedVariableInfo uninitializedInfo = (StackMapTableAttribute.UninitializedVariableInfo) typeInfo;
				return new UninitializedValue(labels.computeIfAbsent(uninitializedInfo.getOffset(), Label::new));
			default:
				throw new IllegalArgumentException("Unknown verification type tag " + typeInfo.getTag());
		}
	}

	@Nonnull
	private Map<Integer, StackMapTableAttribute.StackMapFrame> getStackMapFrames() {
		if (smta == null) {
			return Collections.emptyMap();
		}
		Map<Integer, StackMapTableAttribute.StackMapFrame> frames = new HashMap<>();
		int offset = -1;
		for (StackMapTableAttribute.StackMapFrame frame : smta.getFrames()) {
			if (offset == -1) {
				offset = frame.getOffsetDelta();
			} else {
				offset += frame.getOffsetDelta() + 1;
			}
			frames.put(offset, frame);
		}
		return frames;
	}
}