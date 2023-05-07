package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.InvalidCodeException;
import me.coley.cafedude.UnresolvedLabelException;
import me.coley.cafedude.classfile.AttributeConstants;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.constant.*;
import me.coley.cafedude.classfile.instruction.*;
import me.coley.cafedude.tree.*;
import me.coley.cafedude.tree.insn.*;

import java.util.*;

import static me.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import static me.coley.cafedude.classfile.attribute.LineNumberTableAttribute.LineEntry;
import static me.coley.cafedude.classfile.attribute.LocalVariableTableAttribute.VarEntry;
import static me.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute.VarTypeEntry;

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

	CodeAttribute convert() throws InvalidCodeException {
		State state = new State();
		List<Instruction> converted = new ArrayList<>();
		Map<Integer, Insn> insnMap = new TreeMap<>();
		prepass(code.getInstructions());
		for (Insn insn : code.getInstructions()) {
			insnMap.put(state.offset, insn);
			Instruction convertedInsn = convert(insn, state);
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
			VarEntry var = new VarEntry(
					local.getStart().getOffset(),
					local.getEnd().getOffset(),
					symbols.newUtf8(local.getName()),
					symbols.newUtf8(local.getDesc().getDescriptor()),
					local.getIndex());
			localVariables.add(var);
			if (local.getSignature() != null) {
				// Local variable type table
				VarTypeEntry type = new VarTypeEntry(
						local.getStart().getOffset(),
						local.getEnd().getOffset(),
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
			checkLabel(handler.getStart(), "handler <" + handler.getType() + "> start");
			checkLabel(handler.getEnd(), "handler <" + handler.getType() + "> end");
			checkLabel(handler.getHandler(), "handler <" + handler.getType() + "> handler");
			exceptionTable.add(new CodeAttribute.ExceptionTableEntry(
					handler.getStart().getOffset(),
					handler.getEnd().getOffset(),
					handler.getHandler().getOffset(),
					symbols.newClass(handler.getType())));
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
			switch (insn.getKind()) {
				case LDC: {
					LdcInsn ldcInsn = (LdcInsn) insn;
					CpEntry constant = symbols.newConstant(ldcInsn.getConstant());
					if ((constant.getIndex() > 255 || constant.isWide()) && ldcInsn.getOpcode() == LDC)
						offset += 1;
					break;
				}
				case VAR: {
					VarInsn varInsn = (VarInsn) insn;
					if (varInsn.getIndex() <= 3)
						offset--; // patching will convert to X{STORE/LOAD}_N
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
			}
			offset += insn.size();
		}
	}

	private Instruction convert(Insn insn, State state) throws InvalidCodeException {
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
				// allign to 4 bytes
				int padding = 4 - (state.offset + 1) & 3;
				LookupSwitchInsn lsi = (LookupSwitchInsn) insn;
				checkLabel(lsi.getDefaultLabel(), insn, state);
				int defaultOffset = lsi.getDefaultLabel().getOffset() - state.offset;
				List<Integer> keys = lsi.getKeys();
				List<Integer> offsets = new ArrayList<>();
				for (Label label : lsi.getLabels()) {
					offsets.add(label.getOffset() - state.offset);
				}
				return new LookupSwitchInstruction(padding, defaultOffset, keys, offsets);
			}
			case TABLE_SWITCH: {
				// allign to 4 bytes
				int padding = 4 - (state.offset + 1) & 3;
				TableSwitchInsn tsi = (TableSwitchInsn) insn;
				checkLabel(tsi.getDefaultLabel(), insn, state);
				int defaultOffset = tsi.getDefaultLabel().getOffset() - state.offset;
				int low = tsi.getMin();
				int high = tsi.getMax();
				List<Integer> offsets = new ArrayList<>();
				for (Label label : tsi.getLabels()) {
					offsets.add(label.getOffset() - state.offset);
				}
				return new TableSwitchInstruction(defaultOffset, low, high, offsets);
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
		if (!label.isResolved()) throw new UnresolvedLabelException(label, state.offset, insn);
	}

	private void checkLabel(Label label, String where) throws InvalidCodeException {
		if (!label.isResolved()) throw new UnresolvedLabelException(label, where);
	}

	static class BsmEntry {
		final Handle handle;
		final List<Constant> args;

		BsmEntry(Handle handle, List<Constant> args) {
			this.handle = handle;
			this.args = args;
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
