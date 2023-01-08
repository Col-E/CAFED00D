package me.coley.cafedude.tree.visitor.reader;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.constant.*;
import me.coley.cafedude.classfile.instruction.*;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.Handle;
import me.coley.cafedude.tree.Label;
import me.coley.cafedude.tree.frame.*;
import me.coley.cafedude.tree.visitor.CodeVisitor;
import me.coley.cafedude.util.ConstantUtil;
import me.coley.cafedude.util.OpcodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static me.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import static me.coley.cafedude.classfile.attribute.StackMapTableAttribute.*;
import static me.coley.cafedude.classfile.instruction.Opcodes.*;

class InstructionVisitor {

	private static final Logger logger = LoggerFactory.getLogger(InstructionVisitor.class);
	private final BootstrapMethodsAttribute bsma;
	private final LocalVariableTableAttribute lvta;
	private final LocalVariableTypeTableAttribute lvtta;
	private final StackMapTableAttribute smta;
	private final CodeVisitor cv;
	private final CodeAttribute ca;
	private final ConstPool pool;
	private final Method method;
	private final Map<Integer, Label> labels;
	private final Map<Integer, Instruction> instructions;
	private Stack<Value> stack = new Stack<>();
	private final Stack<Value> locals = new Stack<>();
	private static final Stack<Value> EMPTY = new Stack<>();

	InstructionVisitor(ClassFile clazz, CodeAttribute ca, CodeVisitor cv, Method method,
							  Map<Integer, Label> labels, Map<Integer, Instruction> instructions) {
		this.bsma = clazz.getAttribute(BootstrapMethodsAttribute.class);
		this.lvta = ca.getAttribute(LocalVariableTableAttribute.class);
		this.lvtta = ca.getAttribute(LocalVariableTypeTableAttribute.class);
		this.smta = ca.getAttribute(StackMapTableAttribute.class);
		this.cv = cv;
		this.ca = ca;
		this.pool = clazz.getPool();
		this.labels = labels;
		this.method = method;
		this.instructions = instructions;
	}


	void accept() {
		if (instructions == null) {
			logger.warn("Method visited but no instructions present, Method=" + pool.getUtf(method.getNameIndex()));
			return;
		}
		if (instructions.isEmpty()) return; // no instructions, abstract/interface method
		Map<Integer, StackMapFrame> frames = getStackMapFrames();
		for (Map.Entry<Integer, Instruction> entry : instructions.entrySet()) {
			int insnPos = entry.getKey();
			Label currentLabel = labels.get(insnPos);
			if (currentLabel != null) {
				cv.visitLabel(currentLabel);
				for (Integer line : currentLabel.getLines()) {
					cv.visitLineNumber(line, currentLabel);
				}
			}
			StackMapFrame frame = frames.get(insnPos);
			if(frame != null) {
				visitFrame(frame);
			}
			Instruction insn = entry.getValue();
			if (insn instanceof IntOperandInstruction) {
				visitIntOpInsn((IntOperandInstruction) insn, insnPos);
			} else if (insn instanceof BiIntOperandInstruction) {
				visitBiIntOpInsn((BiIntOperandInstruction) insn, insnPos);
			} else if (insn instanceof WideInstruction) {
				Instruction backing = ((WideInstruction) insn).getBacking();
				if (backing instanceof IntOperandInstruction) {
					visitIntOpInsn((IntOperandInstruction) backing, insnPos);
				} else if (backing instanceof BiIntOperandInstruction) {
					visitBiIntOpInsn((BiIntOperandInstruction) backing, insnPos);
				}
			} else if (insn instanceof LookupSwitchInstruction) {
				visitLookupSwitchInsn((LookupSwitchInstruction) insn, insnPos);
			} else if (insn instanceof TableSwitchInstruction) {
				visitTableSwitchInsn((TableSwitchInstruction) insn, insnPos);
			} else if (insn instanceof BasicInstruction) {
				visitBasicInsn((BasicInstruction) insn, insnPos);
			}
		}
		visitLocalVariables();
		cv.visitMaxs(ca.getMaxStack(), ca.getMaxLocals());
		cv.visitCodeEnd();
	}

	private void visitBasicInsn(BasicInstruction insn, int pos) {
		int opcode = insn.getOpcode();
		if (opcode >= ACONST_NULL && opcode <= DCONST_1) {
			cv.visitConstantInsn(opcode);
		} else if((opcode >= ILOAD_0 && opcode <= ALOAD_3)) {
			int base = opcode - ILOAD_0;
			int var = base % 4;
			int type = base / 4;
			int op = ILOAD + type;
			cv.visitVarInsn(op, var);
		} else if((opcode >= ISTORE_0 && opcode <= ASTORE_3)) {
			int base = opcode - ISTORE_0;
			int var = base % 4;
			int type = base / 4;
			int op = ISTORE + type;
			cv.visitVarInsn(op, var);
		} else if((opcode >= IASTORE && opcode <= SASTORE)
				|| ((opcode >= IALOAD && opcode <= SALOAD))
				|| opcode == ARRAYLENGTH) {
			cv.visitArrayInsn(opcode);
		} else if((opcode >= POP && opcode <= SWAP)) {
			cv.visitStackInsn(opcode);
		} else if((opcode >= IADD && opcode <= LXOR)
				|| (opcode >= I2L && opcode <= I2S)
				|| (opcode >= LCMP && opcode <= DCMPG)) {
			cv.visitArithmeticInsn(opcode);
		} else if((opcode >= IRETURN && opcode <= RETURN)) {
			cv.visitReturnInsn(opcode);
		} else {
			switch (opcode) {
				case NOP:
					cv.visitNop();
					break;
				case ATHROW:
					cv.visitThrow();
					break;
				case MONITORENTER:
				case MONITOREXIT:
					cv.visitMonitorInsn(opcode);
					break;
				default:
					throw new IllegalStateException("Unsupported opcode (no operand): "
						+ OpcodeUtil.getOpcodeName(opcode) + " (" + opcode + ")" + " at " + pos);
			}
		}
	}

	private void visitLookupSwitchInsn(LookupSwitchInstruction insn, int pos) {
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

	private void visitTableSwitchInsn(TableSwitchInstruction insn, int pos) {
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

	private void visitIntOpInsn(IntOperandInstruction ioi, int pos) {
		int operand = ioi.getOperand();
		int opcode = ioi.getOpcode();
		if (opcode == BIPUSH || opcode == SIPUSH || opcode == NEWARRAY || opcode == RET) {
			cv.visitIntInsn(opcode, operand);
		} else if (opcode == LDC || opcode == LDC_W || opcode == LDC2_W) {
			Constant cst = ConstantUtil.from(pool.get(operand), pool);
			cv.visitLdcInsn(cst);
		} else if ((opcode >= ILOAD && opcode <= ALOAD) || (opcode >= ISTORE && opcode <= ASTORE)) {
			cv.visitVarInsn(opcode, operand);
		} else if ((opcode >= IFEQ && opcode <= JSR) || (opcode >= IFNULL && opcode <= JSR_W)) {
			int targetPos = pos + operand;
			Label targetLabel = labels.get(targetPos);
			cv.visitFlowInsn(opcode, targetLabel);
		} else if (opcode == NEW || opcode == ANEWARRAY || opcode == CHECKCAST || opcode == INSTANCEOF) {
			CpClass cc = (CpClass) pool.get(operand);
			cv.visitTypeInsn(opcode, pool.getUtf(cc.getIndex()));
		} else if (opcode >= GETSTATIC && opcode <= PUTFIELD) {
			CpFieldRef fr = (CpFieldRef) pool.get(operand);
			CpClass cc = (CpClass) pool.get(fr.getClassIndex());
			CpNameType nt = (CpNameType) pool.get(fr.getNameTypeIndex());
			String name = pool.getUtf(nt.getNameIndex());
			String owner = pool.getUtf(cc.getIndex());
			String type = pool.getUtf(nt.getTypeIndex());
			cv.visitFieldInsn(opcode, owner, name, Descriptor.from(type));
		} else if (opcode == INVOKEVIRTUAL
				|| opcode == INVOKESPECIAL
				|| opcode == INVOKESTATIC
				|| opcode == INVOKEINTERFACE) {
			ConstRef fr = (ConstRef) pool.get(operand);
			CpClass cc = (CpClass) pool.get(fr.getClassIndex());
			CpNameType nt = (CpNameType) pool.get(fr.getNameTypeIndex());
			String name = pool.getUtf(nt.getNameIndex());
			String owner = pool.getUtf(cc.getIndex());
			String type = pool.getUtf(nt.getTypeIndex());
			cv.visitMethodInsn(opcode, owner, name, Descriptor.from(type));
		} else if (opcode == INVOKEDYNAMIC) {
			if(bsma == null) {
				throw new IllegalStateException(
						"INVOKEDYNAMIC instruction found, but no BootstrapMethodsAttribute present");
			}
			CpInvokeDynamic id = (CpInvokeDynamic) pool.get(operand);
			CpNameType nt = (CpNameType) pool.get(id.getNameTypeIndex());
			String name = pool.getUtf(nt.getNameIndex());
			String type = pool.getUtf(nt.getTypeIndex());
			BootstrapMethod bsm = bsma.getBootstrapMethods().get(id.getBsmIndex());
			CpMethodHandle mh = (CpMethodHandle) pool.get(bsm.getBsmMethodref());
			ConstRef mr = (ConstRef) pool.get(mh.getReferenceIndex());
			CpClass cc = (CpClass) pool.get(mr.getClassIndex());
			CpNameType bsmnt = (CpNameType) pool.get(mr.getNameTypeIndex());
			String bsmName = pool.getUtf(bsmnt.getNameIndex());
			String bsmOwner = pool.getUtf(cc.getIndex());
			String bsmType = pool.getUtf(bsmnt.getTypeIndex());
			Handle bsmHandle = new Handle(
					Handle.Tag.fromKind(mh.getKind()),
					bsmOwner,
					bsmName,
					Descriptor.from(bsmType));
			Constant[] args = new Constant[bsm.getArgs().size()];
			for (int i = 0; i < args.length; i++) {
				args[i] = ConstantUtil.from(pool.get(bsm.getArgs().get(i)), pool);
			}
			cv.visitInvokeDynamicInsn(name, Descriptor.from(type), bsmHandle, args);
		} else {
			throw new IllegalStateException("Unsupported opcode (integer operand): "
					+ OpcodeUtil.getOpcodeName(opcode) + " " + operand + " (" + opcode + ")" + " at " + pos);
		}
	}

	private void visitBiIntOpInsn(BiIntOperandInstruction ioi, int pos) {
		int op1 = ioi.getFirstOperand();
		int op2 = ioi.getSecondOperand();
		int opcode = ioi.getOpcode();
		if (opcode == IINC) {
			cv.visitIIncInsn(op1, op2);
		} else if (opcode == MULTIANEWARRAY) {
			CpClass cc = (CpClass) pool.get(op1);
			cv.visitMultiANewArrayInsn(pool.getUtf(cc.getIndex()), op2);
		} else {
			throw new IllegalStateException("Unsupported opcode (two integer operands): "
					+ OpcodeUtil.getOpcodeName(opcode) + " " + op1 + " " + op2 + " (" + opcode + ")" + " at " + pos);
		}
	}

	private void visitLocalVariables() {
		List<LocalVariableTypeTableAttribute.VarTypeEntry> varTypes = Collections.emptyList();
		if (lvtta != null) {
			varTypes = lvtta.getEntries();
		}
		if(lvta != null) {
			for (LocalVariableTableAttribute.VarEntry entry : lvta.getEntries()) {
				String name = pool.getUtf(entry.getNameIndex());
				Descriptor desc = Descriptor.from(pool.getUtf(entry.getDescIndex()));
				String signature = null;
				for (LocalVariableTypeTableAttribute.VarTypeEntry varType : varTypes) {
					if(varType.getIndex() == entry.getIndex() && varType.getStartPc() == entry.getStartPc()) {
						signature = pool.getUtf(varType.getSignatureIndex());
						break;
					}
				}
				Label start = labels.computeIfAbsent(entry.getStartPc(), Label::new);
				Label end = labels.computeIfAbsent(entry.getStartPc() + entry.getLength(), Label::new);
				cv.visitLocalVariable(entry.getIndex(), name, desc, signature, start, end);
			}
		}
	}

	private void visitFrame(StackMapFrame frame) {
		int kind = Frame.FULL;
		int argument = 0;
		if (frame instanceof SameFrame || frame instanceof SameFrameExtended) {
			kind = Frame.SAME;
			stack = EMPTY;
		} else if (frame instanceof SameLocalsOneStackItem) {
			SameLocalsOneStackItem slo = (SameLocalsOneStackItem) frame;
			stack = new Stack<>();
			stack.push(toValue(slo.stack));
			kind = Frame.SAME1;
		} else if (frame instanceof SameLocalsOneStackItemExtended) {
			SameLocalsOneStackItemExtended slo = (SameLocalsOneStackItemExtended) frame;
			stack = new Stack<>();
			stack.push(toValue(slo.stack));
			kind = Frame.SAME1;
		} else if (frame instanceof ChopFrame) {
			ChopFrame cf = (ChopFrame) frame;
			argument = cf.absentVariables;
			for(int i = 0; i < argument; i++) {
				locals.pop();
			}
			stack = EMPTY;
			kind = Frame.CHOP;
		} else if (frame instanceof AppendFrame) {
			AppendFrame af = (AppendFrame) frame;
			argument = af.additionalLocals.size();
			for (TypeInfo local : af.additionalLocals) {
				locals.push(toValue(local));
			}
			stack = EMPTY;
			kind = Frame.APPEND;
		} else if (frame instanceof FullFrame) {
			FullFrame ff = (FullFrame) frame;
			for(TypeInfo local : ff.locals) {
				locals.push(toValue(local));
			}
			for(TypeInfo stackItem : ff.stack) {
				stack.push(toValue(stackItem));
			}
		} else {
			throw new IllegalStateException("Unsupported frame type: " + frame.getClass().getName());
		}
		cv.visitFrame(kind, stack.toArray(new Value[0]), locals.toArray(new Value[0]), argument);
	}

	private Value toValue(TypeInfo typeInfo) {
		switch (typeInfo.getTag()) {
			case ITEM_TOP:
			case ITEM_INTEGER:
			case ITEM_FLOAT:
			case ITEM_DOUBLE:
			case ITEM_LONG:
			case ITEM_NULL:
			case ITEM_UNINITIALIZED_THIS:
				return new PrimitiveValue(typeInfo.getTag());
			case ITEM_OBJECT:
				ObjectVariableInfo objectInfo = (ObjectVariableInfo) typeInfo;
				CpClass classInfo = (CpClass) pool.get(objectInfo.classIndex);
				return new ObjectValue(pool.getUtf(classInfo.getIndex()));
			case ITEM_UNINITIALIZED:
				UninitializedVariableInfo uninitializedInfo = (UninitializedVariableInfo) typeInfo;
				return new UninitializedValue(labels.computeIfAbsent(uninitializedInfo.offset, Label::new));
			default:
				throw new IllegalArgumentException("Unknown verification type tag " + typeInfo.getTag());
		}
	}

	private Map<Integer, StackMapFrame> getStackMapFrames() {
		if (smta == null) {
			return Collections.emptyMap();
		}
		Map<Integer, StackMapFrame> frames = new HashMap<>();
		int offset = -1;
		for (StackMapFrame frame : smta.getFrames()) {
			if(offset == -1) {
				offset = frame.offsetDelta;
			} else {
				offset += frame.offsetDelta + 1;
			}
			frames.put(offset, frame);
		}
		return frames;
	}

}