package me.coley.cafedude.transform;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.attribute.CodeAttribute;
import me.coley.cafedude.classfile.attribute.LineNumberTableAttribute;
import me.coley.cafedude.classfile.instruction.Instruction;
import me.coley.cafedude.classfile.instruction.IntOperandInstruction;
import me.coley.cafedude.classfile.instruction.LookupSwitchInstruction;
import me.coley.cafedude.classfile.instruction.TableSwitchInstruction;
import me.coley.cafedude.io.InstructionReader;
import me.coley.cafedude.tree.Label;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static me.coley.cafedude.classfile.instruction.Opcodes.*;
import static me.coley.cafedude.classfile.instruction.Opcodes.JSR_W;

/**
 * Label transformer for converting instruction offsets to labels.
 */
public class LabelTransformer extends Transformer {

	private final Map<Method, TreeMap<Integer, Label>> labels = new HashMap<>();
	private final Map<Method, TreeMap<Integer, Instruction>> instructions = new HashMap<>();

	/**
	 * @param clazz Class file to modify.
	 */
	public LabelTransformer(ClassFile clazz) {
		super(clazz);
	}

	@Override
	public void transform() {
		InstructionReader reader = new InstructionReader(new IllegalRewritingInstructionsReader(pool));
		for (Method method : clazz.getMethods()) {
			CodeAttribute ca = method.getAttribute(CodeAttribute.class);

			if (ca != null) {
				LineNumberTableAttribute lnta = ca.getAttribute(LineNumberTableAttribute.class);
				List<Instruction> insns = reader.read(ca.getCode());
				// populate maps
				TreeMap<Integer, Label> labels = new TreeMap<>();
				TreeMap<Integer, Instruction> instructions = new TreeMap<>();

				for (CodeAttribute.ExceptionTableEntry exceptionTableEntry : ca.getExceptionTable()) {
					int start = exceptionTableEntry.getStartPc();
					int end = exceptionTableEntry.getEndPc();
					int handler = exceptionTableEntry.getHandlerPc();
					if(!labels.containsKey(start)) {
						labels.put(start, new Label(start));
					}
					if(!labels.containsKey(end)) {
						labels.put(end, new Label(end));
					}
					if(!labels.containsKey(handler)) {
						labels.put(handler, new Label(handler));
					}
				}

				int pos = 0;
				for (Instruction insn : insns) {
					int opcode = insn.getOpcode();
					if ((opcode >= IFEQ && opcode <= JSR) || (opcode >= IFNULL && opcode <= JSR_W)) {
						IntOperandInstruction ioi = (IntOperandInstruction) insn;
						int offset = ioi.getOperand();
						int target = pos + offset;
						if (!labels.containsKey(target)) {
							labels.put(target, new Label(target));
						}
					} else if(opcode == TABLESWITCH) {
						TableSwitchInstruction tsi = (TableSwitchInstruction) insn;
						List<Integer> offsets = tsi.getOffsets();
						for (int offset : offsets) {
							int target = pos + offset;
							if (!labels.containsKey(target)) {
								labels.put(target, new Label(target));
							}
						}
						int defaultTarget = pos + tsi.getDefault();
						if (!labels.containsKey(defaultTarget)) {
							labels.put(defaultTarget, new Label(defaultTarget));
						}
					} else if(opcode == LOOKUPSWITCH) {
						LookupSwitchInstruction lsi = (LookupSwitchInstruction) insn;
						List<Integer> offsets = lsi.getOffsets();
						for (int offset : offsets) {
							int target = pos + offset;
							if (!labels.containsKey(target)) {
								labels.put(target, new Label(target));
							}
						}
						int defaultTarget = pos + lsi.getDefault();
						if (!labels.containsKey(defaultTarget)) {
							labels.put(defaultTarget, new Label(defaultTarget));
						}
					}
					instructions.put(pos, insn);
					pos += insn.computeSize();
				}
				// add lines to labels
				if(lnta != null) {
					for (LineNumberTableAttribute.LineEntry entry : lnta.getEntries()) {
						Label lab = labels.computeIfAbsent(entry.getStartPc(), Label::new);
						lab.addLineNumber(entry.getLine());
					}
				}
				this.labels.put(method, labels);
				this.instructions.put(method, instructions);
				// TODO: patch invalid offsets
			}
		}
	}

	/**
	 * @param method Method to get labels for.
	 * @return Labels for the given method.
	 */
	public TreeMap<Integer, Label> getLabels(Method method) {
		return labels.get(method);
	}

	/**
	 * @param method Method to get instructions for.
	 * @return Instructions for the given method.
	 */
	public TreeMap<Integer, Instruction> getInstructions(Method method) {
		return instructions.get(method);
	}
}
