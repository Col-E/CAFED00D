package me.coley.cafedude.transform;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.attribute.CodeAttribute;
import me.coley.cafedude.classfile.attribute.LineNumberTableAttribute;
import me.coley.cafedude.classfile.instruction.Instruction;
import me.coley.cafedude.classfile.instruction.IntOperandInstruction;
import me.coley.cafedude.io.InstructionReader;
import me.coley.cafedude.tree.Label;
import me.coley.cafedude.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static me.coley.cafedude.classfile.instruction.Opcodes.IFEQ;
import static me.coley.cafedude.classfile.instruction.Opcodes.JSR;

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
			LineNumberTableAttribute lnta = method.getAttribute(LineNumberTableAttribute.class);

			if (ca != null) {
				List<Instruction> insns = reader.read(ca.getCode());
				// populate maps
				TreeMap<Integer, Label> labels = new TreeMap<>();
				TreeMap<Integer, Instruction> instructions = new TreeMap<>();

				int pos = 0;
				int label = 0;
				for (Instruction insn : insns) {
					int opcode = insn.getOpcode();
					if ((opcode >= IFEQ && opcode <= JSR)) {
						IntOperandInstruction ioi = (IntOperandInstruction) insn;
						int offset = ioi.getOperand();
						int target = pos + offset;
						if (!labels.containsKey(target)) {
							String name = StringUtil.generateName(StringUtil.ALPHABET, label++);
							labels.put(target, new Label(name, target));
						}
					}
					instructions.put(pos, insn);
					pos += insn.computeSize();
				}
				// add lines to labels
				if(lnta != null) {
					for (LineNumberTableAttribute.LineEntry entry : lnta.getEntries()) {
						Label lab = labels.get(entry.getStartPc());
						if (lab != null) {
							lab.addLineNumber(entry.getLine());
						}
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
