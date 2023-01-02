package me.coley.cafedude.transform;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.Modifiers;
import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.attribute.CodeAttribute;
import me.coley.cafedude.classfile.instruction.Instruction;
import me.coley.cafedude.classfile.instruction.IntOperandInstruction;
import me.coley.cafedude.io.InstructionReader;
import me.coley.cafedude.tree.Label;
import me.coley.cafedude.util.StringUtil;

import java.util.*;

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
			Optional<Attribute> codeAttribute = method.getAttributes().stream()
					.filter(attribute -> attribute instanceof CodeAttribute)
					.findFirst();

			if (codeAttribute.isPresent()) {
				List<Instruction> insns = reader.read(((CodeAttribute) codeAttribute.get()).getCode());
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
						labels.put(offset, new Label(StringUtil.generateName(StringUtil.ALPHABET, label), offset));
					}
					instructions.put(pos, insn);
					pos += insn.computeSize();
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
