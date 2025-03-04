package software.coley.cafedude.tree.visitor.writer;

import software.coley.cafedude.InvalidCodeException;
import software.coley.cafedude.UnresolvedLabelException;
import software.coley.cafedude.classfile.attribute.AttributeConstants;
import software.coley.cafedude.classfile.attribute.*;
import software.coley.cafedude.classfile.constant.*;
import software.coley.cafedude.classfile.instruction.*;
import software.coley.cafedude.tree.*;
import software.coley.cafedude.tree.insn.*;

import java.util.*;

import static software.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import static software.coley.cafedude.classfile.attribute.LineNumberTableAttribute.LineEntry;
import static software.coley.cafedude.classfile.attribute.LocalVariableTableAttribute.VarEntry;
import static software.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute.VarTypeEntry;

/**
 * Converter for {@link Code} back into a {@link CodeAttribute}.
 *
 * @author Justus Garbe
 */
public class CodeConverter implements Opcodes {
	private final Code code;
	private final Symbols symbols;

	CodeConverter(Code code, Symbols symbols) {
		this.code = code;
		this.symbols = symbols;
	}

	CodeAttribute convertToAttribute() throws InvalidCodeException {
		State state = new State();
		List<Instruction> converted = new ArrayList<>();
		Map<Integer, Insn> insnMap = new TreeMap<>();
		prepass(code.getInstructions());
		for (Insn insn : code.getInstructions()) {
			insnMap.put(state.offset, insn);
			Instruction convertedInsn = convertToInstruction(insn, state);
			if (convertedInsn != null) {
				converted.add(convertedInsn);
				state.offset += convertedInsn.computeSize();
			}
		}
		List<VarEntry> localVariables = new ArrayList<>();
		List<VarTypeEntry> localVariableTypes = new ArrayList<>();
		for (Local local : code.getLocals()) {
			checkLabel(local.getStart(), "local <" + local.getIndex() + "> start");
			checkLabel(local.getEnd(), "local <" + local.getIndex() + "> end");
			// Local variable table
			int startPc = local.getStart().getOffset();
			int endPc = local.getEnd().getOffset();
			int length = endPc - startPc;
			VarEntry var = new VarEntry(
					startPc,
					length,
					symbols.newUtf8(local.getName()),
					symbols.newUtf8(local.getDesc().getDescriptor()),
					local.getIndex());
			localVariables.add(var);
			if (local.getSignature() != null) {
				// Local variable type table
				VarTypeEntry type = new VarTypeEntry(
						startPc,
						length,
						symbols.newUtf8(local.getName()),
						symbols.newUtf8(local.getSignature()),
						local.getIndex());
				localVariableTypes.add(type);
			}
		}
		List<BootstrapMethod> bootstrapMethods = new ArrayList<>();
		for (BsmEntry bsmEntry : state.bsmEntries) {
			CpMethodHandle bsm = symbols.newHandle(bsmEntry.handle);
			List<CpEntry> args = new ArrayList<>();
			for (Constant constant : bsmEntry.args)
				args.add(symbols.newConstant(constant));
			bootstrapMethods.add(new BootstrapMethod(bsm, args));
		}
		List<CodeAttribute.ExceptionTableEntry> exceptionTable = new ArrayList<>();
		for (ExceptionHandler handler : code.getHandlers()) {
			String catchType = handler.getType();
			String catchTypeRep = catchType == null ? "*" : catchType;
			checkLabel(handler.getStart(), "handler <" + catchTypeRep + "> start");
			checkLabel(handler.getEnd(), "handler <" + catchTypeRep + "> end");
			checkLabel(handler.getHandler(), "handler <" + catchTypeRep + "> handler");
			exceptionTable.add(new CodeAttribute.ExceptionTableEntry(
					handler.getStart().getOffset(),
					handler.getEnd().getOffset(),
					handler.getHandler().getOffset(),
					catchType == null ? null : symbols.newClass(catchType)));
		}
		List<Attribute> attributes = new ArrayList<>();
		if (!localVariables.isEmpty())
			attributes.add(new LocalVariableTableAttribute(
					symbols.newUtf8(AttributeConstants.LOCAL_VARIABLE_TABLE),
					localVariables));
		if (!localVariableTypes.isEmpty())
			attributes.add(new LocalVariableTypeTableAttribute(
					symbols.newUtf8(AttributeConstants.LOCAL_VARIABLE_TYPE_TABLE),
					localVariableTypes));
		if (!bootstrapMethods.isEmpty())
			attributes.add(new BootstrapMethodsAttribute(
					symbols.newUtf8(AttributeConstants.BOOTSTRAP_METHODS),
					bootstrapMethods));
		if (!state.lineEntries.isEmpty()) {
			List<LineEntry> sorted = new ArrayList<>(state.lineEntries);
			sorted.sort(Comparator.comparingInt(LineEntry::getStartPc));
			attributes.add(new LineNumberTableAttribute(
					symbols.newUtf8(AttributeConstants.LINE_NUMBER_TABLE),
					sorted));
		}
		return new CodeAttribute(
				symbols.newUtf8(AttributeConstants.CODE),
				code.getMaxStack(),
				code.getMaxLocals(),
				converted,
				exceptionTable,
				attributes);
	}

	private void prepass(List<Insn> insns) {
		// calculate labels offsets
		int offset = 0;
		for (Insn insn : insns) {
			int size = insn.size();
			switch (insn.getKind()) {
				case LDC: {
					LdcInsn ldcInsn = (LdcInsn) insn;
					CpEntry constant = symbols.newConstant(ldcInsn.getConstant());

					// Expand size based if WIDE usage is required but LDC is specified
					if ((constant.getIndex() > 255 || constant.isWide()) && ldcInsn.getOpcode() == LDC)
						size += 1;
					break;
				}
				case VAR: {
					VarInsn varInsn = (VarInsn) insn;

					// Check for cases where insn can become X{STORE/LOAD}_N
					if (varInsn.supportsSingleOpInsn())
						size--;
					break;
				}
				case TABLE_SWITCH:
				case LOOKUP_SWITCH: {
					// adjust also for opcode alignment
					offset += (4 - (offset + 1) & 3); // align to 4 bytes
					break;
				}
				case LABEL: {
					LabelInsn labelInsn = (LabelInsn) insn;
					labelInsn.getLabel().setOffset(offset);
					break;
				}
				default:
					break;
			}
			offset += size;
		}
	}

	Instruction convertToInstruction(Insn insn, State state) throws InvalidCodeException {
		int opcode = insn.getOpcode();
		switch (insn.getKind()) {
			case ARITHMETIC:
			case ARRAY:
			case CONSTANT:
			case RETURN:
			case STACK:
			case NOP:
			case MONITOR:
			case THROW:
				return new BasicInstruction(opcode);
			case FIELD: {
				FieldInsn fieldInsn = (FieldInsn) insn;
				CpFieldRef fieldRef = symbols.newField(fieldInsn.getOwner(), fieldInsn.getName(),
						fieldInsn.getDescriptor());
				return new CpRefInstruction(opcode, fieldRef);
			}
			case METHOD: {
				MethodInsn methodInsn = (MethodInsn) insn;
				CpMethodRef methodRef = symbols.newMethod(methodInsn.getOwner(), methodInsn.getName(),
						methodInsn.getDescriptor());
				return new CpRefInstruction(opcode, methodRef);
			}
			case LDC: {
				LdcInsn ldcInsn = (LdcInsn) insn;
				CpEntry constant = symbols.newConstant(ldcInsn.getConstant());
				int ldcOpcode = Opcodes.LDC;
				if (constant.getIndex() > 255)
					ldcOpcode = Opcodes.LDC_W;
				if (constant.isWide())
					ldcOpcode = Opcodes.LDC2_W;
				return new CpRefInstruction(ldcOpcode, symbols.newConstant(ldcInsn.getConstant()));
			}
			case MULTI_ANEWARRAY: {
				MultiANewArrayInsn manai = (MultiANewArrayInsn) insn;
				return new MultiANewArrayInstruction(symbols.newClass(manai.getOwner()), manai.getDimensions());
			}
			case TYPE: {
				TypeInsn typeInsn = (TypeInsn) insn;
				return new CpRefInstruction(opcode, symbols.newClass(typeInsn.getDescriptor().getDescriptor()));
			}
			case FLOW: {
				FlowInsn flowInsn = (FlowInsn) insn;
				checkLabel(flowInsn.getLabel(), insn, state);
				int target = flowInsn.getLabel().getOffset();
				int offset = target - state.offset;
				return new IntOperandInstruction(opcode, offset);
			}
			case INT: {
				IntInsn intInsn = (IntInsn) insn;
				return new IntOperandInstruction(opcode, intInsn.getOperand());
			}
			case LOOKUP_SWITCH: {
				LookupSwitchInsn lsi = (LookupSwitchInsn) insn;
				checkLabel(lsi.getDefaultLabel(), insn, state);
				int defaultOffset = lsi.getDefaultLabel().getOffset() - state.offset;
				List<Integer> keys = lsi.getKeys();
				List<Integer> offsets = new ArrayList<>();
				for (Label label : lsi.getLabels()) {
					offsets.add(label.getOffset() - state.offset);
				}
				LookupSwitchInstruction lswitch = new LookupSwitchInstruction(defaultOffset, keys, offsets);
				lswitch.notifyStartPosition(state.offset); // TODO: Mirror the correct offset from the current state
				return lswitch;
			}
			case TABLE_SWITCH: {
				TableSwitchInsn tsi = (TableSwitchInsn) insn;
				checkLabel(tsi.getDefaultLabel(), insn, state);
				int defaultOffset = tsi.getDefaultLabel().getOffset() - state.offset;
				int low = tsi.getMin();
				int high = tsi.getMax();
				List<Integer> offsets = new ArrayList<>();
				for (Label label : tsi.getLabels()) {
					offsets.add(label.getOffset() - state.offset);
				}
				TableSwitchInstruction tswitch = new TableSwitchInstruction(defaultOffset, low, high, offsets);
				tswitch.notifyStartPosition(state.offset); // TODO: Mirror the correct offset from the current state
				return tswitch;
			}
			case INVOKE_DYNAMIC: {
				InvokeDynamicInsn idi = (InvokeDynamicInsn) insn;
				Handle handle = idi.getBootstrapMethod();
				int index = state.findBsm(handle, idi.getBootstrapArguments());
				CpNameType nameType = symbols.newNameType(idi.getName(), idi.getDescriptor());
				CpInvokeDynamic invokeDynamic = symbols.newInvokeDynamic(index, nameType);
				return new CpRefInstruction(opcode, invokeDynamic);
			}
			case VAR: {
				VarInsn varInsn = (VarInsn) insn;
				int var = varInsn.getIndex();
				if (var < 4) {
					if (opcode >= ILOAD && opcode <= ALOAD) {
						opcode = ILOAD_0 + ((opcode - ILOAD) * 4) + var;
					} else if (opcode >= ISTORE && opcode <= ASTORE) {
						opcode = ISTORE_0 + ((opcode - ISTORE) * 4) + var;
					}
					state.offset--; // compensate for the index
					return new BasicInstruction(opcode);
				} else {
					return new IntOperandInstruction(opcode, var);
				}
			}
			case LABEL: {
				LabelInsn labelInsn = (LabelInsn) insn;
				for (Integer line : labelInsn.getLabel().getLines()) {
					state.lineEntries.add(new LineEntry(state.offset, line));
				}
				return null;
			}
			case IINC: {
				IIncInsn iincInsn = (IIncInsn) insn;
				return new IincInstruction(iincInsn.getIndex(), iincInsn.getIncrement());
			}
			case UNKNOWN:
			default:
				throw new IllegalArgumentException("Unknown instruction: " + insn);
		}
	}

	private void checkLabel(Label label, Insn insn, State state) throws InvalidCodeException {
		if (!label.isResolved())
			throw new UnresolvedLabelException(label, state.offset, insn);
	}

	private void checkLabel(Label label, String where) throws InvalidCodeException {
		if (!label.isResolved())
			throw new UnresolvedLabelException(label, where);
	}

	static class BsmEntry {
		final Handle handle;
		final List<Constant> args;

		BsmEntry(Handle handle, List<Constant> args) {
			this.handle = handle;
			this.args = args;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			BsmEntry bsmEntry = (BsmEntry) o;

			if (!handle.equals(bsmEntry.handle)) return false;
			return args.equals(bsmEntry.args);
		}

		@Override
		public int hashCode() {
			int result = handle.hashCode();
			result = 31 * result + args.hashCode();
			return result;
		}
	}

	static class State {

		Set<BsmEntry> bsmEntries = new HashSet<>();
		Set<LineEntry> lineEntries = new HashSet<>();
		int offset = 0;

		int findBsm(Handle handle, List<Constant> args) {
			BsmEntry entry = null;
			int index = 0;
			for (BsmEntry bsmEntry : bsmEntries) {
				if (bsmEntry.handle.equals(handle) && bsmEntry.args.equals(args)) {
					entry = bsmEntry;
					break;
				}
				index++;
			}
			if (entry == null) {
				entry = new BsmEntry(handle, args);
				bsmEntries.add(entry);
				index = bsmEntries.size() - 1;
			}
			return index;
		}

	}

}
