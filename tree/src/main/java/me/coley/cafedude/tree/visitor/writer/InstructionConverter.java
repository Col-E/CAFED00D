package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.constant.*;
import me.coley.cafedude.classfile.instruction.*;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.Handle;
import me.coley.cafedude.tree.Label;
import me.coley.cafedude.tree.insn.*;
import me.coley.cafedude.util.OpcodeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;

public class InstructionConverter implements Opcodes{

	private final Symbols symbols;
	private final List<BsmEntry> bsmEntries;

	InstructionConverter(Symbols symbols) {
		this.symbols = symbols;
		this.bsmEntries = new ArrayList<>();
	}

	List<Instruction> convert(List<Insn> instructions, List<BootstrapMethod> bootstrapMethods) {
		List<Instruction> converted = new ArrayList<>();
		for (Insn insn : instructions) {
			converted.add(convert(insn));
		}
		return converted;
	}

	private Instruction convert(Insn insn) {
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
				return new CpRefInstruction(opcode, symbols.newConstant(ldcInsn.getConstant()));
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
				checkLabel(flowInsn.getLabel(), insn);
				return new IntOperandInstruction(opcode, flowInsn.getLabel().getOffset());
			}
			case INT: {
				IntInsn intInsn = (IntInsn) insn;
				return new IntOperandInstruction(opcode, intInsn.getOperand());
			}
			case LOOKUP_SWITCH: {
				LookupSwitchInsn lsi = (LookupSwitchInsn) insn;
				checkLabel(lsi.getDefaultLabel(), insn);
				int defaultOffset = lsi.getDefaultLabel().getOffset();
				List<Integer> keys = lsi.getKeys();
				List<Integer> offsets = new ArrayList<>();
				for (Label label : lsi.getLabels()) {
					checkLabel(label, insn);
					offsets.add(label.getOffset());
				}
				return new LookupSwitchInstruction(defaultOffset, keys, offsets);
			}
			case TABLE_SWITCH: {
				TableSwitchInsn tsi = (TableSwitchInsn) insn;
				checkLabel(tsi.getDefaultLabel(), insn);
				int defaultOffset = tsi.getDefaultLabel().getOffset();
				int low = tsi.getMin();
				int high = tsi.getMax();
				List<Integer> offsets = new ArrayList<>();
				for (Label label : tsi.getLabels()) {
					checkLabel(label, insn);
					offsets.add(label.getOffset());
				}
				return new TableSwitchInstruction(defaultOffset, low, high, offsets);
			}
			case INVOKE_DYNAMIC: {
				InvokeDynamicInsn idi = (InvokeDynamicInsn) insn;
				Handle handle = idi.getBootstrapMethod();
				int index = findBsm(handle, idi.getBootstrapArguments());
				CpNameType nameType = symbols.newNameType(idi.getName(), idi.getDescriptor());
				CpInvokeDynamic invokeDynamic = symbols.newInvokeDynamic(index, nameType);
				return new CpRefInstruction(opcode, invokeDynamic);
			}
			case VAR: {
				VarInsn varInsn = (VarInsn) insn;
				int var = varInsn.getIndex();
				if(var < 4) {
					if(opcode >= ILOAD && opcode <= ALOAD) {
						opcode = ILOAD_0 + ((opcode - ILOAD) * 4) + var;
					} else if(opcode >= ISTORE && opcode <= ASTORE) {
						opcode = ISTORE_0 + ((opcode - ISTORE) * 4) + var;
					}
					return new BasicInstruction(opcode);
				} else {
					return new IntOperandInstruction(opcode, var);
				}
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

	private void checkLabel(Label label, Insn insn) {
		if(!label.isResolved()) {
			throw new IllegalStateException("Unresolved label at "
					+ OpcodeUtil.getOpcodeName(insn.getOpcode()));
		}
	}

	int findBsm(Handle handle, List<Constant> args) {
		BsmEntry entry = null;
		int index = 0;
		for (BsmEntry bsmEntry : bsmEntries) {
			if(bsmEntry.handle.equals(handle) && bsmEntry.args.equals(args)) {
				entry = bsmEntry;
				break;
			}
			index++;
		}
		if(entry == null) {
			entry = new BsmEntry(handle, args);
			bsmEntries.add(entry);
			index = bsmEntries.size() - 1;
		}
		return index;
	}

	static class BsmEntry {
		final Handle handle;
		final List<Constant> args;
		BsmEntry(Handle handle, List<Constant> args) {
			this.handle = handle;
			this.args = args;
		}
	}

}
