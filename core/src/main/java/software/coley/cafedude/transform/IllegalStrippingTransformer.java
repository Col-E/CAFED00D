package software.coley.cafedude.transform;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.ConstantPoolConstants;
import software.coley.cafedude.classfile.Descriptor;
import software.coley.cafedude.classfile.Field;
import software.coley.cafedude.classfile.Method;
import software.coley.cafedude.classfile.Modifiers;
import software.coley.cafedude.classfile.annotation.Annotation;
import software.coley.cafedude.classfile.annotation.AnnotationElementValue;
import software.coley.cafedude.classfile.annotation.ArrayElementValue;
import software.coley.cafedude.classfile.annotation.ClassElementValue;
import software.coley.cafedude.classfile.annotation.ElementValue;
import software.coley.cafedude.classfile.annotation.EnumElementValue;
import software.coley.cafedude.classfile.annotation.PrimitiveElementValue;
import software.coley.cafedude.classfile.annotation.TargetInfo;
import software.coley.cafedude.classfile.annotation.TargetInfo.CatchTargetInfo;
import software.coley.cafedude.classfile.annotation.TargetInfo.OffsetTargetInfo;
import software.coley.cafedude.classfile.annotation.TargetInfo.SuperTypeTargetInfo;
import software.coley.cafedude.classfile.annotation.TargetInfo.TypeArgumentTargetInfo;
import software.coley.cafedude.classfile.annotation.TypeAnnotation;
import software.coley.cafedude.classfile.attribute.AnnotationDefaultAttribute;
import software.coley.cafedude.classfile.attribute.AnnotationsAttribute;
import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.attribute.AttributeConstants;
import software.coley.cafedude.classfile.attribute.AttributeContexts;
import software.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute;
import software.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute.BootstrapMethod;
import software.coley.cafedude.classfile.attribute.CodeAttribute;
import software.coley.cafedude.classfile.attribute.CodeAttribute.ExceptionTableEntry;
import software.coley.cafedude.classfile.attribute.CodeUtilities;
import software.coley.cafedude.classfile.attribute.ConstantValueAttribute;
import software.coley.cafedude.classfile.attribute.DefaultAttribute;
import software.coley.cafedude.classfile.attribute.EnclosingMethodAttribute;
import software.coley.cafedude.classfile.attribute.ExceptionsAttribute;
import software.coley.cafedude.classfile.attribute.InnerClassesAttribute;
import software.coley.cafedude.classfile.attribute.InnerClassesAttribute.InnerClass;
import software.coley.cafedude.classfile.attribute.LineNumberTableAttribute;
import software.coley.cafedude.classfile.attribute.LocalVariableTableAttribute;
import software.coley.cafedude.classfile.attribute.LocalVariableTableAttribute.VarEntry;
import software.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute;
import software.coley.cafedude.classfile.attribute.LocalVariableTypeTableAttribute.VarTypeEntry;
import software.coley.cafedude.classfile.attribute.ModuleAttribute;
import software.coley.cafedude.classfile.attribute.ModuleAttribute.Requires;
import software.coley.cafedude.classfile.attribute.NestHostAttribute;
import software.coley.cafedude.classfile.attribute.NestMembersAttribute;
import software.coley.cafedude.classfile.attribute.ParameterAnnotationsAttribute;
import software.coley.cafedude.classfile.attribute.PermittedClassesAttribute;
import software.coley.cafedude.classfile.attribute.RecordAttribute;
import software.coley.cafedude.classfile.attribute.SignatureAttribute;
import software.coley.cafedude.classfile.attribute.SourceFileAttribute;
import software.coley.cafedude.classfile.behavior.AttributeHolder;
import software.coley.cafedude.classfile.constant.ConstDynamic;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpFieldRef;
import software.coley.cafedude.classfile.constant.CpInterfaceMethodRef;
import software.coley.cafedude.classfile.constant.CpMethodHandle;
import software.coley.cafedude.classfile.constant.CpMethodRef;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.classfile.constant.LoadableConstant;
import software.coley.cafedude.classfile.constant.Placeholders;
import software.coley.cafedude.classfile.instruction.BasicInstruction;
import software.coley.cafedude.classfile.instruction.CpRefInstruction;
import software.coley.cafedude.classfile.instruction.Instruction;
import software.coley.cafedude.classfile.instruction.IntOperandInstruction;
import software.coley.cafedude.classfile.instruction.LookupSwitchInstruction;
import software.coley.cafedude.classfile.instruction.TableSwitchInstruction;
import software.coley.cafedude.io.AttributeHolderType;
import software.coley.cafedude.io.AttributeReader;
import software.coley.cafedude.io.IndexableByteStream;
import software.coley.cafedude.io.InstructionReader;
import software.coley.cafedude.io.InstructionWriter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static software.coley.cafedude.classfile.attribute.CodeUtilities.*;
import static software.coley.cafedude.classfile.instruction.Opcodes.*;

/**
 * A transformer to remove illegal attributes and data from a class.
 *
 * @author Matt Coley
 */
public class IllegalStrippingTransformer extends Transformer implements ConstantPoolConstants {
	private static final Pattern UTF8_WORD = Pattern.compile("[<>;/$\\w]+");
	private static final Logger logger = LoggerFactory.getLogger(IllegalStrippingTransformer.class);
	protected int reinterpretationPasses = 4;

	/**
	 * @param clazz
	 * 		Class to strip.
	 */
	public IllegalStrippingTransformer(ClassFile clazz) {
		super(clazz);
	}

	// TODO: Make it possible to track if calling transform did remove anything

	@Override
	public void transform() {
		logger.debug("Transforming '{}'", clazz.getName());

		// Record existing CP refs.
		Set<CpEntry> cpAccesses = clazz.cpAccesses();

		// Remove members with illegal descriptors.
		// If any of these exist the class isn't going to ever be loaded (without throwing a LinkageError)
		// so its fair game to toss this stuff just to make ASM happy.
		clazz.getFields().removeIf(field -> isInvalidDesc(field.getType()));
		clazz.getMethods().removeIf(method -> isInvalidDesc(method.getType()));

		// Strip attributes that are not valid.
		clazz.getAttributes().removeIf(attribute -> !isValidWrapped(clazz, attribute));
		deduplicateAttributes(clazz);
		for (Field field : clazz.getFields()) {
			field.getAttributes().removeIf(attribute -> !isValidWrapped(field, attribute));
			deduplicateAttributes(field);
		}
		Set<ConstDynamic> dynamicCpReferences = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Method method : clazz.getMethods()) {
			method.getAttributes().removeIf(attribute -> !isValidWrapped(method, attribute));
			deduplicateAttributes(method);
			CodeAttribute code = method.getAttribute(CodeAttribute.class);
			if (code != null) {
				removeInvalidInstructions(code);
				removeInvalidVariables(code);
				removeInvalidLineNumbers(code);
				removeInvalidTypeAnnotations(code);
				//removeInstructionReinterpretation(code, 0);
				//removeDeadInstructions(code);
				collectDynamicCpReferences(code, dynamicCpReferences);
			}
		}

		// Strip BSM class attribute if it is unused.
		removeInvalidBootstrapMethodAttribute(dynamicCpReferences);

		// Invalid handles are unusable even when they are otherwise unreferenced.
		pool.removeIf(entry -> entry instanceof CpMethodHandle handle && !isValidMethodHandle(handle));

		// Record filtered CP refs, the difference of the sets are the indices that were referenced
		// by removed attributes/data.
		Set<CpEntry> filteredCpAccesses = clazz.cpAccesses();
		cpAccesses.removeAll(filteredCpAccesses);
	}

	/**
	 * Replaces dead code with {@code NOP} instructions.
	 *
	 * @param code
	 * 		Code to visit.
	 */
	protected void removeDeadInstructions(@Nonnull CodeAttribute code) {
		// Compute which instructions are visited by walking the method's control flow.
		Set<Instruction> visited = Collections.newSetFromMap(new IdentityHashMap<>());

		// Visit from the start + try-catch handler blocks
		Queue<Integer> offsetsToVisit = new ArrayDeque<>();
		offsetsToVisit.add(0);
		for (ExceptionTableEntry e : code.getExceptionTable())
			offsetsToVisit.add(e.getHandlerPc());

		// Visit flow
		List<Instruction> instructions = code.getInstructions();
		int instructionCount = instructions.size();
		while (!offsetsToVisit.isEmpty()) {
			int offset = offsetsToVisit.remove();
			Instruction instruction = code.getInstructionAtOffset(offset);
			if (instruction == null)
				continue;
			int insnIndex = code.indexOf(instruction);
			if (insnIndex < 0)
				continue;
			for (int i = insnIndex; i < instructionCount; i++) {
				instruction = instructions.get(i);
				if (!visited.add(instruction))
					break;

				// Add new offsets for control flow branches.
				if (isBranch(instruction) && instruction instanceof IntOperandInstruction jump) {
					int base = code.computeOffsetOf(jump);
					offsetsToVisit.add(base + jump.getOperand());
				} else if (instruction instanceof TableSwitchInstruction table) {
					int base = code.computeOffsetOf(table);
					offsetsToVisit.add(base + table.getDefault());
					table.getOffsets().forEach(off -> offsetsToVisit.add(base + off));
				} else if (instruction instanceof LookupSwitchInstruction lookup) {
					int base = code.computeOffsetOf(lookup);
					offsetsToVisit.add(base + lookup.getDefault());
					lookup.getOffsets().forEach(off -> offsetsToVisit.add(base + off));
				}

				// Abort sequential stepping if this instruction is terminal or always-take branching.
				if (isTerminalOrAlwaysTakeFlowControl(instruction))
					break;
			}
		}

		// Replace any unvisited instructions with NOP
		BasicInstruction nop = new BasicInstruction(NOP);
		for (int i = instructions.size() - 1; i >= 0; i--) {
			Instruction instruction = instructions.get(i);
			if (!visited.contains(instruction)) {
				int extraNops = instruction.computeSize() - 1;
				instructions.set(i, nop);
				for (int j = 0; j < extraNops; j++) {
					instructions.add(i, nop);
				}
			}
		}
	}

	/**
	 * Replaces illegal jumps into instruction operand bytes with valid instruction sequences.
	 * This technique is only viable with {@code noverify}.
	 *
	 * @param code
	 * 		Code to visit.
	 * @param passCount
	 * 		Current visit pass count.
	 */
	protected void removeInstructionReinterpretation(@Nonnull CodeAttribute code, int passCount) {
		List<Instruction> instructions = code.getInstructions();
		int instructionCount = instructions.size();
		for (int i = 0; i < instructionCount; i++) {
			Instruction instruction = instructions.get(i);
			if (isBranch(instruction) && instruction instanceof IntOperandInstruction jump) {
				// Compute jump destination
				int currentOffset = code.computeOffsetOf(instruction);
				int relativeJumpOffset = jump.getOperand();
				int absoluteJumpOffset = currentOffset + relativeJumpOffset;
				Instruction instructionAtOffset = code.getInstructionAtOffset(absoluteJumpOffset);

				// If no such instruction starts at the given offset, we've jumped into the middle of an instruction
				// and this is illegally re-interpreting the operand bytes as its own instruction.
				if (instructionAtOffset == null) {
					// Get the instruction we've jumped into
					instructionAtOffset = code.getContainingInstructionAtOffset(absoluteJumpOffset);
					if (instructionAtOffset == null) {
						logger.warn("Reinterpretation lookup went out-of-bounds");
						return;
					}

					// Compute how many bytes into the instruction we've jumped into
					int offsetOfContainingInstruction = code.computeOffsetOf(instructionAtOffset);
					int offsetDifference = absoluteJumpOffset - offsetOfContainingInstruction;

					// Reinterpret the bytecode with our illegal offset
					byte[] methodBytecode = new InstructionWriter().writeCode(instructions);
					int methodBytecodeOffset = offsetOfContainingInstruction + offsetDifference;
					List<Instruction> reinrerpreted = Collections.emptyList();
					try {
						// Continue expanding the reinterpreted block until:
						// - We encounter a failure with reinterpretation
						// - Hit the end of the method
						// - Hit code that aligns with our existing instruction model
						int next = i + 1;
						int tempSequenceLength = instructionAtOffset.computeSize();
						int tempInsnIndexToCheckForAlignment = 1;
						while (next <= instructionCount) {
							IndexableByteStream is = new IndexableByteStream(methodBytecode);
							is.moveTo(methodBytecodeOffset);
							reinrerpreted = new InstructionReader().read(is, pool, tempSequenceLength - offsetDifference);

							// If we observe that we hit the end of the method, this block is complete.
							if (next >= instructionCount) break;

							// Step forward and try again.
							tempSequenceLength += instructions.get(next++).computeSize();

							// If we observe that we hit code that aligns with the normal interpretation then this block is complete.
							CodeAttribute tmpCode = new CodeAttribute(Placeholders.UTF8, 0, 0, reinrerpreted, Collections.emptyList(), Collections.emptyList());
							for (int j = tempInsnIndexToCheckForAlignment; j < reinrerpreted.size(); j++) {
								// Get the offset of this reinterpreted instruction in terms of the original method bytecode
								Instruction reinterpretedInsn = reinrerpreted.get(j);
								int tmpOffset = tmpCode.computeOffsetOf(reinterpretedInsn);
								int offsetInOriginalMethod = tmpOffset + offsetOfContainingInstruction + offsetDifference;

								// If the offset of this instruction is a match for an offset in the original code
								// then we've completed the block of reinterpreted instructions.
								if (code.getInstructionAtOffset(offsetInOriginalMethod) != null) {
									if (j < reinrerpreted.size())
										reinrerpreted = reinrerpreted.subList(0, j);
									next = Integer.MAX_VALUE;
									break;
								}
								tempInsnIndexToCheckForAlignment = Math.max(tempInsnIndexToCheckForAlignment, j);
							}
						}
					} catch (Throwable ignored) {
						// TODO: Once this pass is finalized, we will actually ignore this.
						//  - For now with the samples on-hand there are still some cases where they
						//    seemingly have the wrong offsets on reinterpreted 'goto' instructions.
						//  - These problems get ignored at runtime since they usually are within opaque-predicate
						//    dead code which prevents the VM from freaking out.
						logger.warn("Reinterpretation encountered an exception", ignored);
					}

					// Sanity check we have at least one reinterpreted instruction.
					int reinrerpretedBlockSize = wrap(reinrerpreted).computeSize();
					if (reinrerpreted.isEmpty()) {
						logger.error("Reinterpretation block fill failure");
						return;
					}

					// Modify the jump target to point to the end of the method (current code size in bytes).
					// We will copy the patched block to the end of the method.
					int reinterpretedBlockOffset = code.computeSize();
					int newRelativeJumpOffset = reinterpretedBlockOffset - code.computeOffsetOf(jump);
					jump.setOperand(newRelativeJumpOffset);

					// TODO: Rewrite 'goto' as 'goto_w' when the distance between the original location
					//  and the rewritten block location is too large.
					//  - Ensure our handling of 'reinterpretedBlockOffset' allows for this
					instructions.addAll(reinrerpreted);

					// Sanity check
					if (code.getInstructionAtOffset(newRelativeJumpOffset + currentOffset) != reinrerpreted.get(0)) {
						logger.error("Reinterpretation block redirect failure");
						return;
					}

					// Now that the reinterpreted code block has been copied to the end of the method,
					// we need to fix any relative offsets in the block that are outside the bounds of the block
					// to point back to where the reinterpreted code originates from.
					//
					//  1. Compute how many bytes into the reinterpreted instruction we are (mid-insn-shift)
					//  2. Based on the current position, plus the mid-insn-shift so that we are "relative"
					//     to the reinterpreted jump's base instruction offset,
					//     compute difference from offset where reinterpreted block was added.
					//  3. For any relative jump in the reinterpreted block that is not self-contained in the block
					//     add the difference to each jump so that they are now relative to where they would have
					//     been from the reinterpreted instruction.
					shiftJumps(reinrerpreted,
							// Filter: Only shift jumps that are outside the reinterpreted block boundaries
							blockRelativeOffset -> blockRelativeOffset < 0 || blockRelativeOffset >= reinrerpretedBlockSize,
							// Shift calculator: Add the block-shift to each affected jump
							//  minus the bytes of preceding instructions in the block (since we are retargeting to jump backwards)
							(jumpInsnOffset, jumpOperand) ->
							{
								// Compute where the jump originally was going to land in the context of it being
								// evaluated from the reinterpreted instructions.
								int originalJumpTarget = (absoluteJumpOffset + jumpInsnOffset + jumpOperand);
								int thisJumpInsnOffset = reinterpretedBlockOffset + jumpInsnOffset;

								// The new jump operand will be the difference between the current jump instruction
								// offset and the original destination the jump at its original location would
								// have landed at.
								return (originalJumpTarget) - (thisJumpInsnOffset);
							}
					);

					// Debug sanity checks
					//  checkInvalidJumpUpTo(code, currentOffset);
					//  checkInvalidJumpFrom(code, reinterpretedBlockOffset);

					// Append trailing instructions to make sure we don't fall off the end of the method.
					Instruction lastReinterpretedBlockInsn = reinrerpreted.get(reinrerpreted.size() - 1);
					if (isTerminalOrAlwaysTakeFlowControl(lastReinterpretedBlockInsn)) {
						// Execution naturally ends, no need to make changes
					} else if (isBranch(lastReinterpretedBlockInsn)) {
						// Handle fall-through cases of branches (these are likely bogus opaque-predicates, but just in case)
						int currentCodeSize = code.computeSize();
						int nextExpectedOffset = absoluteJumpOffset + reinrerpretedBlockSize;
						int diff = nextExpectedOffset - currentCodeSize;
						instructions.add(new IntOperandInstruction(GOTO_W, diff));
					} else {
						// Handle natural fall-through
						int reinterpretedInstructionIndex = code.indexOf(instructionAtOffset);
						int nextExpectedOffset = code.computeOffsetOf(instructions.get(reinterpretedInstructionIndex + 1));
						int diff = nextExpectedOffset - code.computeSize();
						instructions.add(new IntOperandInstruction(GOTO_W, diff));
						// TODO: These two blocks are *very* similar and can probably be combined.
						//  - Need more test cases to verify for certain
					}
				}
			}
		}

		// It is possible to stack reinterpretation, so we will allow multiple passes.
		if (checkInvalidJumpUpTo(code, Integer.MAX_VALUE) && passCount < reinterpretationPasses)
			removeInstructionReinterpretation(code, passCount + 1);
	}

	/**
	 * Removes instructions with illegal operands <i>(That won't work even with {@code noverify})</i>.
	 * These generally are included in dead-code blocks and thus don't cause problems since
	 * they are not eagerly verified.
	 *
	 * @param code
	 * 		Code to visit.
	 */
	protected void removeInvalidInstructions(@Nonnull CodeAttribute code) {
		List<Instruction> instructions = code.getInstructions();
		int codeLength = code.computeSize();

		// Exception ranges and handlers must point to instruction boundaries.
		List<ExceptionTableEntry> exceptions = code.getExceptionTable();
		exceptions.removeIf(entry ->
				entry.getStartPc() >= entry.getEndPc() ||
						entry.getStartPc() >= codeLength ||
						entry.getHandlerPc() >= codeLength ||
						code.getInstructionAtOffset(entry.getStartPc()) == null ||
						code.getInstructionAtOffset(entry.getHandlerPc()) == null ||
						(entry.getEndPc() != codeLength && code.getInstructionAtOffset(entry.getEndPc()) == null));

		// Skip if there is only one instruction.
		if (instructions.size() <= 1)
			return;
		int maxPc = code.computeOffsetOf(instructions.get(instructions.size() - 1));

		// Remove any jump instructions that point to bogus offsets
		// - The code is already unverifiable, so when we stub out the instruction with
		//   a 'return' the code is still unverifiable but fixes the ASM crash.
		// - The number of bytes if kept the same, so valid jump instructions elsewhere
		//   in the method are not broken.
		// - This de-syncs the stack-frame entries for some reason I haven't looked into but
		//   since the code that relies on these tricks already requires use of -noverify
		//   I don't really feel like looking into it. Just use SKIP_FRAMES with ASM on these classes.
		for (int i = instructions.size() - 1; i >= 0; i--) {
			Instruction instruction = instructions.get(i);
			int op = instruction.getOpcode();

			// Jumps that go out of bounds of the method
			if (isBranch(instruction) && instruction instanceof IntOperandInstruction jump) {
				int jumpOffset = code.computeOffsetOf(jump) + jump.getOperand();
				if (jumpOffset > maxPc || jumpOffset < 0) {
					int size = instruction.computeSize();
					instructions.set(i, new BasicInstruction(RETURN));
					for (int j = 0; j < size - 1; j++)
						instructions.add(i, new BasicInstruction(NOP));
				}
			} else if (instruction instanceof TableSwitchInstruction tswitch) {
				int switchOffset = code.computeOffsetOf(tswitch);
				if (switchOffset + tswitch.getDefault() > maxPc
						|| switchOffset + tswitch.getDefault() < 0
						|| tswitch.getOffsets().stream().anyMatch(o -> switchOffset + o > maxPc)
						|| tswitch.getOffsets().stream().anyMatch(o -> switchOffset + o < 0)) {
					int size = instruction.computeSize();
					instructions.set(i, new BasicInstruction(RETURN));
					for (int j = 0; j < size - 1; j++)
						instructions.add(i, new BasicInstruction(NOP));
				}
			} else if (instruction instanceof LookupSwitchInstruction lswitch) {
				int switchOffset = code.computeOffsetOf(lswitch);
				if (switchOffset + lswitch.getDefault() > maxPc
						|| switchOffset + lswitch.getDefault() < 0
						|| lswitch.getOffsets().stream().anyMatch(o -> switchOffset + o > maxPc)
						|| lswitch.getOffsets().stream().anyMatch(o -> switchOffset + o < 0)) {
					int size = instruction.computeSize();
					instructions.set(i, new BasicInstruction(RETURN));
					for (int j = 0; j < size - 1; j++)
						instructions.add(i, new BasicInstruction(NOP));
				}
			}

			// Dynamic constants must point to an existing, valid bootstrap method.
			else if (instruction instanceof CpRefInstruction cpRef
					&& cpRef.getEntry() instanceof ConstDynamic dynamic
					&& !isValidDynamic(dynamic)) {
				int size = instruction.computeSize();
				instructions.set(i, new BasicInstruction(RETURN));
				for (int j = 0; j < size - 1; j++)
					instructions.add(i, new BasicInstruction(NOP));
			}

			// LDC content must be loadable and method handles must match their reference kind.
			else if ((op == LDC || op == LDC_W || op == LDC2_W)
					&& instruction instanceof CpRefInstruction ldc
					&& (!(ldc.getEntry() instanceof LoadableConstant) ||
					ldc.getEntry() instanceof CpMethodHandle handle && !isValidMethodHandle(handle))) {
				int size = instruction.computeSize();
				instructions.set(i, new BasicInstruction(RETURN));
				for (int j = 0; j < size - 1; j++)
					instructions.add(i, new BasicInstruction(NOP));
			}
		}
	}

	/**
	 * Removes line number entries that point outside the instruction stream.
	 *
	 * @param code
	 * 		Code to visit.
	 */
	protected void removeInvalidLineNumbers(@Nonnull CodeAttribute code) {
		LineNumberTableAttribute lines = code.getAttribute(LineNumberTableAttribute.class);
		if (lines == null)
			return;

		int codeLength = code.computeSize();
		lines.getEntries().removeIf(entry ->
				!isInstructionBoundary(code, codeLength, entry.getStartPc()));
	}

	/**
	 * Removes local variable table entries with invalid data.
	 *
	 * @param code
	 * 		Code to visit.
	 */
	protected void removeInvalidVariables(@Nonnull CodeAttribute code) {
		// In ASM's ClassReader.readCode(...) the startPc + length are used as labels[pc+length] which can be OOB.
		int codeLength = code.computeSize();

		// Strip invalid entries from the local variable table.
		//  - The range must be bounded by the code length.
		//  - The local variable index must be within the max locals of the method.
		LocalVariableTableAttribute lvt = code.getAttribute(LocalVariableTableAttribute.class);
		if (lvt != null)
			lvt.getEntries().removeIf(entry ->
					!isValidCodeRange(code, codeLength, entry.getStartPc(), entry.getLength())
							|| entry.getIndex() >= code.getMaxLocals());

		// Same rules apply to the local variable type table.
		LocalVariableTypeTableAttribute lvtt = code.getAttribute(LocalVariableTypeTableAttribute.class);
		if (lvtt != null)
			lvtt.getEntries().removeIf(entry ->
					!isValidCodeRange(code, codeLength, entry.getStartPc(), entry.getLength())
							|| entry.getIndex() >= code.getMaxLocals());
	}

	/**
	 * Removes type annotations with invalid code ranges.
	 *
	 * @param code
	 * 		Code to visit.
	 */
	protected void removeInvalidTypeAnnotations(@Nonnull CodeAttribute code) {
		int codeLength = code.computeSize();
		List<Attribute> attributes = code.getAttributes();
		for (int i = attributes.size() - 1; i >= 0; i--) {
			// Only care about type annotations.
			Attribute attribute = attributes.get(i);
			String name = attribute.getName().getText();
			if (!AttributeConstants.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS.equals(name) &&
					!AttributeConstants.RUNTIME_VISIBLE_TYPE_ANNOTATIONS.equals(name))
				continue;
			if (!(attribute instanceof AnnotationsAttribute annotations))
				continue;

			annotations.getAnnotations().removeIf(annotation -> {
				// Skip non-type annotations.
				if (!(annotation instanceof TypeAnnotation typeAnnotation))
					return false;
				TargetInfo targetInfo = typeAnnotation.getTargetInfo();

				// Local-variable targets duplicate the range rules of LocalVariableTable.
				//  - The range must be bounded by the code length.
				//  - The local variable index must be within the max locals of the method.
				if (targetInfo instanceof TargetInfo.LocalVarTargetInfo localVarTargetInfo) {
					for (TargetInfo.LocalVarTargetInfo.Variable variable : localVarTargetInfo.getVariableTable()) {
						if (!isValidCodeRange(code, codeLength, variable.getStartPc(), variable.getLength())
								|| variable.getIndex() >= code.getMaxLocals()) {
							return true;
						}
					}
				}

				// Code offsets must be aligned to instruction boundaries.
				if (targetInfo instanceof OffsetTargetInfo offsetTarget)
					return !isInstructionBoundary(code, codeLength, offsetTarget.getOffset());

				// Catch targets must be aligned to instruction boundaries.
				if (targetInfo instanceof TypeArgumentTargetInfo typeArgumentTarget)
					return !isInstructionBoundary(code, codeLength, typeArgumentTarget.getOffset());

				return false;
			});

			// Drop the attribute if it has no annotations left.
			if (annotations.getAnnotations().isEmpty())
				attributes.remove(i);
		}
	}

	/**
	 * Finds any {@link ConstDynamic} usage in the given method bytecode, and adds them to the given set.
	 *
	 * @param code
	 * 		Code to visit.
	 * @param dynamicCpReferences
	 * 		Set to add results into.
	 */
	protected void collectDynamicCpReferences(@Nonnull CodeAttribute code, @Nonnull Set<ConstDynamic> dynamicCpReferences) {
		for (Instruction instruction : code.getInstructions())
			if (instruction instanceof CpRefInstruction cpRefInstruction
					&& cpRefInstruction.getEntry() instanceof ConstDynamic referencedDynamic)
				dynamicCpReferences.add(referencedDynamic);
	}

	/**
	 * Checks whether a bootstrap method entry can be retained.
	 *
	 * @param bsm
	 * 		Bootstrap method entry to check.
	 *
	 * @return {@code true} when the entry has a valid method handle and only loadable arguments.
	 */
	private boolean isValidBootstrapMethod(@Nullable BootstrapMethod bsm) {
		if (bsm == null || bsm.getBsmMethodRef() == null || !isValidMethodHandle(bsm.getBsmMethodRef()))
			return false;

		for (CpEntry arg : bsm.getArgs()) {
			// Bootstrap arguments must be constants that the VM can load.
			// JVMS 4.4-C:
			//  - 3:  CONSTANT_Integer
			//  - 4:  CONSTANT_Float
			//  - 5:  CONSTANT_Long
			//  - 6:  CONSTANT_Double
			//  - 7:  CONSTANT_Class
			//  - 8:  CONSTANT_String
			//  - 15: CONSTANT_MethodHandle
			//  - 16: CONSTANT_MethodType
			//  - 17: CONSTANT_Dynamic
			if (arg == null) return false;
			int tag = arg.getTag();
			if (!((tag >= INTEGER && tag <= STRING) || (tag >= METHOD_HANDLE && tag <= DYNAMIC)))
				return false;

			// Method handles and dynamic constants have additional bootstrap-specific validity rules.
			if (arg instanceof CpMethodHandle handle && !isValidMethodHandle(handle))
				return false;
			if (arg instanceof ConstDynamic dynamic && !isValidDynamic(dynamic))
				return false;
		}
		return true;
	}

	/**
	 * Rewrites dynamic constant bootstrap indices after invalid table entries are removed.
	 *
	 * @param retained
	 * 		Flags identifying the original table entries that remain.
	 */
	private void remapBootstrapMethodIndices(@Nonnull boolean[] retained) {
		remapBootstrapMethodIndices(retained, pool);
	}

	/**
	 * Rewrites dynamic constant bootstrap indices after invalid table entries are removed.
	 *
	 * @param retained
	 * 		Flags identifying the original table entries that remain.
	 * @param pool
	 * 		Constant pool to rewrite.
	 */
	public static void remapBootstrapMethodIndices(@Nonnull boolean[] retained, @Nonnull ConstPool pool) {
		// Build an old-index to new-index map, using -1 for entries that no longer exist.
		int[] indexMap = new int[retained.length];
		int nextIndex = 0;
		for (int i = 0; i < retained.length; i++)
			indexMap[i] = retained[i] ? nextIndex++ : -1;

		// Rewrite every dynamic pool entry before bytecode validation checks its bootstrap target.
		for (CpEntry entry : pool) {
			if (!(entry instanceof ConstDynamic dynamic))
				continue;
			int oldIndex = dynamic.getBsmIndex();
			int newIndex = oldIndex >= 0 && oldIndex < indexMap.length ? indexMap[oldIndex] : -1;
			dynamic.setBsmIndex(newIndex);
		}
	}

	/**
	 * Remove any bootstrap method content that is unused.
	 *
	 * @param dynamicCpReferences
	 * 		Set of dynamic CP references that were found in method bytecode.
	 */
	protected void removeInvalidBootstrapMethodAttribute(@Nonnull Set<ConstDynamic> dynamicCpReferences) {
		// ASM will try to read this attribute if any CP entry exists for DYNAMIC or INVOKE_DYNAMIC.
		// If no methods actually refer to those CP entries, this attribute can be filled with garbage,
		// in which case we will want to remove it.
		if (dynamicCpReferences.isEmpty()) {
			// There are no dynamic references found in any method's code. Remove the BSM attribute.
			BootstrapMethodsAttribute bsmAttribute = clazz.getAttribute(BootstrapMethodsAttribute.class);
			if (bsmAttribute != null)
				clazz.getAttributes().remove(bsmAttribute);
		}

		// Remove any unused dynamic cp references in the constant pool.
		pool.removeIf(c -> c instanceof ConstDynamic cd && !dynamicCpReferences.contains(cd));
	}

	/**
	 * Checks that a dynamic constant resolves to a usable bootstrap method.
	 *
	 * @param dynamic
	 * 		Dynamic constant to check.
	 *
	 * @return {@code true} when the bootstrap method exists and has a valid handle.
	 */
	private boolean isValidDynamic(@Nonnull ConstDynamic dynamic) {
		// If there isn't a bootstrap method attribute, then there is no way to resolve the dynamic constant.
		BootstrapMethodsAttribute attribute = clazz.getAttribute(BootstrapMethodsAttribute.class);
		if (attribute == null)
			return false;

		// Index must be in bounds and the method handle must match its reference kind.
		int index = dynamic.getBsmIndex();
		List<BootstrapMethod> methods = attribute.getBootstrapMethods();
		return index >= 0
				&& index < methods.size()
				&& isValidMethodHandle(methods.get(index).getBsmMethodRef());
	}

	/**
	 * Checks that a method handle kind matches its referenced constant type.
	 *
	 * @param handle
	 * 		Method handle to check.
	 *
	 * @return {@code true} when the kind and reference type agree.
	 */
	private boolean isValidMethodHandle(@Nonnull CpMethodHandle handle) {
		return switch (handle.getKind()) {
			case CpMethodHandle.REF_GET_FIELD,
			     CpMethodHandle.REF_GET_STATIC,
			     CpMethodHandle.REF_PUT_FIELD,
			     CpMethodHandle.REF_PUT_STATIC -> handle.getReference() instanceof CpFieldRef;
			case CpMethodHandle.REF_INVOKE_VIRTUAL,
			     CpMethodHandle.REF_NEW_INVOKE_SPECIAL -> handle.getReference() instanceof CpMethodRef;
			case CpMethodHandle.REF_INVOKE_STATIC,
			     CpMethodHandle.REF_INVOKE_SPECIAL -> handle.getReference() instanceof CpMethodRef ||
					clazz.getVersionMajor() >= 52 && handle.getReference() instanceof CpInterfaceMethodRef;
			case CpMethodHandle.REF_INVOKE_INTERFACE -> handle.getReference() instanceof CpInterfaceMethodRef;
			default -> false;
		};
	}

	/**
	 * Generic checks for any given attribute.
	 * <p/>
	 * Any exception observed when handling {@link #isValid(AttributeHolder, Attribute)}
	 * results in the dropping of the attribute.
	 *
	 * @param holder
	 * 		The thing declaring the attribute.
	 * @param attribute
	 * 		The attribute to validate/
	 *
	 * @return {@code true} when valid. {@code false} when invalid, and the attribute should be stripped.
	 */
	protected boolean isValidWrapped(@Nonnull AttributeHolder holder, @Nonnull Attribute attribute) {
		try {
			return isValid(holder, attribute);
		} catch (Exception ex) {
			logger.warn("Encountered exception when parsing attribute '{}' in context '{}', dropping it",
					attribute.getClass().getName(),
					holder.getHolderType().name());
			return false;
		}
	}

	/**
	 * Removes duplicate singleton attributes from a holder and its nested tables.
	 *
	 * @param holder
	 * 		Holder whose attribute table should be normalized.
	 */
	private void deduplicateAttributes(@Nonnull AttributeHolder holder) {
		List<Attribute> attributes = holder.getAttributes();
		Map<String, Class<?>> singletonTypes = new HashMap<>();
		Set<String> conflictingSingletons = new HashSet<>();
		List<Attribute> normalized = new ArrayList<>(attributes.size());
		for (Attribute attribute : attributes) {
			String attributeName = attribute.getName().getText();

			// Skip non-singleton attributes, they are allowed to be duplicated.
			if (!AttributeReader.isSingletonAttribute(attributeName)) {
				normalized.add(attribute);
				continue;
			}

			// Skip if we have already seen a conflicting singleton attribute, since they are all discarded.
			if (conflictingSingletons.contains(attributeName))
				continue;

			// Check if we have already seen a singleton attribute of this name, and if so check that the type matches.
			Class<?> firstType = singletonTypes.get(attributeName);
			if (firstType == null) {
				singletonTypes.put(attributeName, attribute.getClass());
				normalized.add(attribute);
			} else if (firstType == attribute.getClass()) {
				logger.debug("Discarding duplicate singleton attribute '{}'", attributeName);
			} else {
				normalized.removeIf(existing -> existing.getName().getText().equals(attributeName));
				singletonTypes.remove(attributeName);
				conflictingSingletons.add(attributeName);
				logger.debug("Discarding incompatible singleton attributes '{}'", attributeName);
			}
		}
		holder.setAttributes(normalized);

		// Recurse only into retained nested tables, which have independent cardinality rules.
		for (Attribute attribute : normalized) {
			if (attribute instanceof CodeAttribute code)
				deduplicateAttributes(code);
			else if (attribute instanceof RecordAttribute record) {
				for (RecordAttribute.RecordComponent component : record.getComponents())
					deduplicateAttributes(component);
			}
		}
	}

	/**
	 * Generic checks for any given attribute.
	 *
	 * @param holder
	 * 		The thing declaring the attribute.
	 * @param attribute
	 * 		The attribute to validate/
	 *
	 * @return {@code true} when valid. {@code false} when invalid, and the attribute should be stripped.
	 */
	protected boolean isValid(@Nonnull AttributeHolder holder, @Nonnull Attribute attribute) {
		Map<CpEntry, Predicate<Integer>> expectedTypeMasks = new HashMap<>();
		Map<CpEntry, Predicate<CpEntry>> cpEntryValidators = new HashMap<>();

		// Check name index
		int maxCpIndex = pool.size();
		if (attribute.getName().getIndex() > maxCpIndex)
			return false;

		// Cannot investigate directly unsupported attributes.
		if (attribute instanceof DefaultAttribute)
			return true;

		// Holder must be allowed to hold the given attribute
		String name = attribute.getName().getText();

		// Check for illegal usage contexts
		EnumSet<AttributeHolderType> allowedContexts = AttributeContexts.getAllowedContexts(name);
		AttributeHolderType holderType = holder.getHolderType();
		if (!allowedContexts.contains(holderType)) {
			logger.debug("Found '{}' declared in illegal context {}, allowed contexts: {}",
					name, holderType.name(), allowedContexts);
			return false;
		} else if (!Modifiers.has(clazz.getAccess(), Modifiers.ACC_MODULE) && AttributeContexts.isModuleAttribute(name)) {
			logger.debug("Found '{}' in non-module class", name);
			return false;
		}

		// Check indices match certain types (key=cp_index, value=mask of allowed cp_tags)
		boolean allow0Case = false;
		switch (name) {
			case AttributeConstants.CONSTANT_VALUE:
				CpEntry valueIndex = ((ConstantValueAttribute) attribute).getConstantValue();
				int requiredTag = constantValueTag(((Field) holder).getType().getText());
				if (requiredTag < 0)
					return false;
				expectedTypeMasks.put(valueIndex, i -> i == requiredTag);
				break;
			case AttributeConstants.RUNTIME_INVISIBLE_ANNOTATIONS:
			case AttributeConstants.RUNTIME_VISIBLE_ANNOTATIONS:
			case AttributeConstants.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
			case AttributeConstants.RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
				AnnotationsAttribute annotations = (AnnotationsAttribute) attribute;
				for (Annotation anno : annotations.getAnnotations())
					addAnnotationValidation(holder, expectedTypeMasks, cpEntryValidators, anno);
				break;
			case AttributeConstants.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			case AttributeConstants.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS: {
				// Sanity check
				if (holderType != AttributeHolderType.METHOD)
					return false;
				ParameterAnnotationsAttribute paramAnnotations = (ParameterAnnotationsAttribute) attribute;

				// Compare against actual number of parameters
				Method method = (Method) holder;
				String desc = method.getType().getText();
				int parameterCount = Descriptor.from(desc).getParameterCount();
				if (paramAnnotations.getParameterAnnotations().keySet().stream()
						.anyMatch(key -> key >= parameterCount)) {
					String methodName = method.getName().getText();
					logger.debug("Out of bounds parameter-annotation indices used on method {}", methodName);
					return false;
				}

				// Filter annotations
				Collection<List<Annotation>> parameterAnnos = paramAnnotations.getParameterAnnotations().values();
				for (List<Annotation> annotationList : parameterAnnos)
					for (Annotation anno : annotationList)
						addAnnotationValidation(holder, expectedTypeMasks, cpEntryValidators, anno);
				break;
			}
			case AttributeConstants.ANNOTATION_DEFAULT:
				AnnotationDefaultAttribute annotationDefault = (AnnotationDefaultAttribute) attribute;
				ElementValue elementValue = annotationDefault.getElementValue();
				addElementValueValidation(expectedTypeMasks, cpEntryValidators, elementValue);
				break;
			case AttributeConstants.NEST_HOST:
				NestHostAttribute nestHost = (NestHostAttribute) attribute;
				cpEntryValidators.put(nestHost.getHostClass(), matchClass());
				break;
			case AttributeConstants.NEST_MEMBERS:
				NestMembersAttribute nestMembers = (NestMembersAttribute) attribute;
				for (CpClass member : nestMembers.getMemberClasses()) {
					cpEntryValidators.put(member, matchClass());
				}
				break;
			case AttributeConstants.ENCLOSING_METHOD:
				EnclosingMethodAttribute enclosingMethod = (EnclosingMethodAttribute) attribute;
				expectedTypeMasks.put(enclosingMethod.getClassEntry(), i -> i == CLASS);
				cpEntryValidators.put(enclosingMethod.getClassEntry(), matchClass());

				// method_index must be zero if the current class was immediately enclosed in source code by an
				//   instance initializer, static initializer, instance variable initializer,
				//   or class variable initializer
				// Otherwise it points to the method name_type value
				expectedTypeMasks.put(enclosingMethod.getMethodEntry(), i -> i == 0 || i == NAME_TYPE);
				allow0Case = (enclosingMethod.getMethodEntry() == null);
				break;
			case AttributeConstants.EXCEPTIONS:
				ExceptionsAttribute exceptions = (ExceptionsAttribute) attribute;
				for (CpClass exceptionTypeIndex : exceptions.getExceptionTable()) {
					cpEntryValidators.put(exceptionTypeIndex, matchClass());
				}
				break;
			case AttributeConstants.INNER_CLASSES:
				InnerClassesAttribute innerClasses = (InnerClassesAttribute) attribute;
				for (InnerClass innerClass : innerClasses.getInnerClasses()) {
					expectedTypeMasks.put(innerClass.getInnerClassInfo(), i -> i == 0 || i == CLASS);
					cpEntryValidators.put(innerClass.getInnerClassInfo(), matchClass());

					// 0 if the defining class is the top-level class
					expectedTypeMasks.put(innerClass.getOuterClassInfo(), i -> i == 0 || i == CLASS);

					// 0 if anonymous, otherwise name index
					expectedTypeMasks.put(innerClass.getInnerName(), i -> i == 0 || i == UTF8);
					allow0Case |= innerClass.getOuterClassInfo() == null || innerClass.getInnerName() == null;
				}
				break;
			case AttributeConstants.CODE: {
				// Sanity check
				if (holderType != AttributeHolderType.METHOD)
					return false;

				// Method cannot be abstract
				Method method = (Method) holder;
				if ((method.getAccess() & Modifiers.ACC_ABSTRACT) > 0) {
					logger.debug("Illegal 'Code' attribute on abstract method {}", method.getName().getText());
					return false;
				}
				CodeAttribute code = (CodeAttribute) attribute;

				// Prune bad code sub-attributes
				code.getAttributes().removeIf(sub -> !isValid(code, sub));

				// Ensure exception indices are valid
				for (ExceptionTableEntry entry : code.getExceptionTable()) {
					CpClass catchType = entry.getCatchType();
					if (catchType == null) {
						allow0Case = true;
					} else {
						cpEntryValidators.put(catchType, matchClass());
						expectedTypeMasks.put(catchType, i -> i == 0 || i == CLASS);
					}
				}
				break;
			}
			case AttributeConstants.SIGNATURE:
				SignatureAttribute signatureAttribute = (SignatureAttribute) attribute;
				cpEntryValidators.put(signatureAttribute.getSignature(), e -> matchSignature((CpUtf8) e, holder));
				break;
			case AttributeConstants.SOURCE_FILE:
				SourceFileAttribute sourceFileAttribute = (SourceFileAttribute) attribute;
				cpEntryValidators.put(sourceFileAttribute.getSourceFilename(), matchUtf8NonEmpty());
				break;
			case AttributeConstants.MODULE:
				ModuleAttribute moduleAttribute = (ModuleAttribute) attribute;
				expectedTypeMasks.put(moduleAttribute.getVersion(), i -> i == 0 || i == UTF8);
				if (moduleAttribute.getVersion() == null)
					allow0Case = true;
				for (Requires requires : moduleAttribute.getRequires()) {
					expectedTypeMasks.put(requires.getVersion(), i -> i == 0 || i == UTF8);
				}
				break;
			case AttributeConstants.BOOTSTRAP_METHODS:
				BootstrapMethodsAttribute bootstrapMethodsAttribute = (BootstrapMethodsAttribute) attribute;
				List<BootstrapMethod> methods = bootstrapMethodsAttribute.getBootstrapMethods();
				boolean[] retained = new boolean[methods.size()];
				List<BootstrapMethod> retainedMethods = new ArrayList<>(methods.size());

				// Filter out invalid bootstrap methods, and retain the valid ones.
				for (int i = 0; i < methods.size(); i++) {
					BootstrapMethod bsm = methods.get(i);
					if (!isValidBootstrapMethod(bsm)) {
						logger.debug("Discarding invalid bootstrap method at index {}", i);
						continue;
					}
					retained[i] = true;
					retainedMethods.add(bsm);
				}

				// Compact the table and rewrite every dynamic constant that used an original index.
				if (retainedMethods.size() != methods.size()) {
					remapBootstrapMethodIndices(retained);
					bootstrapMethodsAttribute.setBootstrapMethods(retainedMethods);
				}

				// Validate references from entries that survived filtering.
				for (BootstrapMethod bsm : retainedMethods) {
					expectedTypeMasks.put(bsm.getBsmMethodRef(), i -> i == METHOD_HANDLE);
					for (CpEntry arg : bsm.getArgs())
						expectedTypeMasks.put(arg, i ->
								(i >= INTEGER && i <= STRING) ||
										(i >= METHOD_HANDLE && i <= DYNAMIC));
				}
				break;
			case AttributeConstants.LOCAL_VARIABLE_TABLE:
				LocalVariableTableAttribute varTable = (LocalVariableTableAttribute) attribute;
				for (VarEntry entry : varTable.getEntries()) {
					expectedTypeMasks.put(entry.getName(), i -> i == UTF8);
					expectedTypeMasks.put(entry.getDesc(), i -> i == UTF8);
					cpEntryValidators.put(entry.getName(), matchUtf8ValidQualifiedName().and(matchUtf8Word()));
					cpEntryValidators.put(entry.getDesc(), matchUtf8FieldDescriptor());
				}
				break;
			case AttributeConstants.LOCAL_VARIABLE_TYPE_TABLE:
				LocalVariableTypeTableAttribute typeTable = (LocalVariableTypeTableAttribute) attribute;
				for (VarTypeEntry entry : typeTable.getEntries()) {
					cpEntryValidators.put(entry.getName(), matchUtf8ValidQualifiedName().and(matchUtf8Word()));
					cpEntryValidators.put(entry.getSignature(), matchUtf8NonEmpty());
				}
				break;
			case AttributeConstants.PERMITTED_SUBCLASSES:
				PermittedClassesAttribute permittedClassesAttribute = (PermittedClassesAttribute) attribute;
				for (CpClass index : permittedClassesAttribute.getClasses()) {
					cpEntryValidators.put(index, matchClass());
				}
				break;
			case AttributeConstants.RECORD:
				RecordAttribute recordAttribute = (RecordAttribute) attribute;
				for (RecordAttribute.RecordComponent component : recordAttribute.getComponents()) {
					cpEntryValidators.put(component.getName(), matchUtf8Word());
					cpEntryValidators.put(component.getDesc(), matchUtf8FieldDescriptor());

					// Prune bad component sub-attributes (annotations)
					component.getAttributes().removeIf(sub -> !isValid(component, sub));
				}
				break;
			case AttributeConstants.LINE_NUMBER_TABLE:
			case AttributeConstants.SOURCE_DEBUG_EXTENSION:
			case AttributeConstants.DEPRECATED:
			case AttributeConstants.SYNTHETIC:
				// no-op
				break;
			case AttributeConstants.CHARACTER_RANGE_TABLE:
			case AttributeConstants.COMPILATION_ID:
			case AttributeConstants.METHOD_PARAMETERS:
			case AttributeConstants.MODULE_HASHES:
			case AttributeConstants.MODULE_MAIN_CLASS:
			case AttributeConstants.MODULE_PACKAGES:
			case AttributeConstants.MODULE_RESOLUTION:
			case AttributeConstants.MODULE_TARGET:
			case AttributeConstants.SOURCE_ID:
			case AttributeConstants.STACK_MAP_TABLE:
			default:
				// TODO: The rest of these
				break;
		}

		// Check referenced entries have the required constant-pool kind.
		for (Map.Entry<CpEntry, Predicate<Integer>> entry : expectedTypeMasks.entrySet()) {
			CpEntry cpEntry = entry.getKey();

			// A null reference is only permitted where the attribute explicitly allows it.
			//  - Yes, the CP doesn't start at 0, but there are special cases where it is allowed.
			if (allow0Case && cpEntry == null)
				continue;
			if (cpEntry == null) {
				logger.debug("Invalid '{}' attribute on {}, contains CP reference to null / 0!", name, holderType.name());
				return false;
			}

			int tag = cpEntry.getTag();
			if (!entry.getValue().test(tag)) {
				logger.debug("Invalid '{}' attribute on {}, contains CP reference to index with wrong type!",
						name, holderType.name());
				return false;
			}
		}

		// Apply content predicates independently of the type masks, so validators without a mask still get applied.
		for (Map.Entry<CpEntry, Predicate<CpEntry>> entry : cpEntryValidators.entrySet()) {
			CpEntry cpEntry = entry.getKey();
			if (cpEntry == null) {
				logger.debug("Invalid '{}' attribute on {}, contains CP reference to null / 0!", name, holderType.name());
				return false;
			}
			if (!entry.getValue().test(cpEntry)) {
				logger.debug("Invalid '{}' attribute on {}, contains CP reference that does not match criteria at index: {}", name, holderType.name(), cpEntry);
				return false;
			}
		}
		return true;
	}

	/**
	 * @param fieldDesc
	 * 		Field descriptor of the field carrying a {@code ConstantValue} attribute.
	 *
	 * @return The constant-pool tag the value must have, or {@code -1} when the field
	 * type cannot carry a {@code ConstantValue} (JVMS 4.7.2, Table 4.7.2-A).
	 */
	private static int constantValueTag(@Nonnull String fieldDesc) {
		return switch (fieldDesc) {
			case "Z", "B", "C", "S", "I" -> INTEGER;
			case "F" -> FLOAT;
			case "J" -> LONG;
			case "D" -> DOUBLE;
			case "Ljava/lang/String;" -> STRING;
			default -> -1;
		};
	}

	protected void addAnnotationValidation(@Nullable AttributeHolder holder,
	                                       @Nonnull Map<CpEntry, Predicate<Integer>> expectedTypeMasks,
	                                       @Nonnull Map<CpEntry, Predicate<CpEntry>> cpEntryValidators,
	                                       @Nonnull Annotation anno) {
		expectedTypeMasks.put(anno.getType(), i -> i == UTF8);
		cpEntryValidators.put(anno.getType(), matchUtf8FieldDescriptor());
		for (Map.Entry<CpUtf8, ElementValue> entry : anno.getValues().entrySet()) {
			CpUtf8 elementTypeIndex = entry.getKey();
			cpEntryValidators.put(elementTypeIndex, matchUtf8ValidQualifiedName());
			addElementValueValidation(expectedTypeMasks, cpEntryValidators, entry.getValue());
		}
		if (anno instanceof TypeAnnotation typeAnnotation) {
			TargetInfo targetInfo = typeAnnotation.getTargetInfo();
			switch (targetInfo.getTargetTypeKind()) {
				case TYPE_PARAMETER_BOUND_TARGET:
					break;
				case TYPE_PARAMETER_TARGET:
					break;
				case FORMAL_PARAMETER_TARGET:
					break;
				case TYPE_ARGUMENT_TARGET:
					break;
				case LOCALVAR_TARGET:
					break;
				case THROWS_TARGET:
					// TODO: Verify with a sample what the target holder type should be
					break;
				case OFFSET_TARGET:
					// TODO: Compare to code instructions / length
					break;
				case SUPERTYPE_TARGET:
					if (holder instanceof ClassFile) {
						SuperTypeTargetInfo superTypeTargetInfo = (SuperTypeTargetInfo) targetInfo;
						if (!superTypeTargetInfo.isExtends()) {
							ClassFile classFile = (ClassFile) holder;
							// Enforce interfaces range
							if (superTypeTargetInfo.getSuperTypeIndex() >= classFile.getInterfaceClasses().size()) {
								expectedTypeMasks.put(null, i -> false);
							}
						}
					} else {
						// Illegal target kind for situation
						expectedTypeMasks.put(null, i -> false);
					}
					break;

				case CATCH_TARGET:
					if (holder instanceof CodeAttribute code) {
						CatchTargetInfo catchTargetInfo = (CatchTargetInfo) targetInfo;

						// Enforce table range
						if (catchTargetInfo.getExceptionTableIndex() >= code.getExceptionTable().size()) {
							expectedTypeMasks.put(null, i -> false);
						}
					} else {
						// Illegal target kind for situation
						expectedTypeMasks.put(null, i -> false);
					}
					break;
				case EMPTY_TARGET:
				default:
					// no-op
					break;
			}
		}
	}

	protected void addElementValueValidation(@Nonnull Map<CpEntry, Predicate<Integer>> expectedTypeMasks,
	                                         @Nonnull Map<CpEntry, Predicate<CpEntry>> cpEntryValidators,
	                                         @Nonnull ElementValue elementValue) {
		if (elementValue instanceof ClassElementValue classElement) {
			CpUtf8 classIndex = classElement.getClassEntry();
			cpEntryValidators.put(classIndex, matchUtf8ReturnDescriptor());
		} else if (elementValue instanceof EnumElementValue enumElement) {
			cpEntryValidators.put(enumElement.getType(), matchUtf8FieldDescriptor());
		} else if (elementValue instanceof PrimitiveElementValue primitive) {
			CpEntry primitiveEntry = primitive.getValue();
			int requiredTag = PrimitiveElementValue.requiredConstantTag(primitive.getTag());
			expectedTypeMasks.put(primitiveEntry, i -> i == requiredTag);
		} else if (elementValue instanceof AnnotationElementValue annotationElement) {
			Annotation annotation = annotationElement.getAnnotation();
			addAnnotationValidation(null, expectedTypeMasks, cpEntryValidators, annotation);
		} else if (elementValue instanceof ArrayElementValue arrayElement) {
			List<ElementValue> array = arrayElement.getArray();
			for (ElementValue arrayValue : array)
				addElementValueValidation(expectedTypeMasks, cpEntryValidators, arrayValue);
		}
	}

	/**
	 * @param e
	 * 		Signature string constant pool entry.
	 * @param context
	 * 		The context of the signature declaration.
	 *
	 * @return {@code true} when the signature is valid and can be kept.
	 */
	protected boolean matchSignature(@Nonnull CpUtf8 e, @Nonnull AttributeHolder context) {
		return matchUtf8NonEmpty().test(e);
	}

	@Nonnull
	protected Predicate<CpEntry> matchClass() {
		return e -> e instanceof CpClass && matchUtf8ValidQualifiedName().test(((CpClass) e).getName());
	}

	@Nonnull
	protected Predicate<CpEntry> matchUtf8ValidQualifiedName() {
		return e -> {
			if (!(e instanceof CpUtf8)) return false;
			String text = ((CpUtf8) e).getText();
			return !text.isEmpty()
					// From #jvms-4.2.2 - Class names cannot contain any of these characters
					&& text.indexOf('.') < 0
					&& text.indexOf(';') < 0
					&& text.indexOf('[') < 0;
		};
	}

	@Nonnull
	protected Predicate<CpEntry> matchUtf8FieldDescriptor() {
		return e -> (e instanceof CpUtf8) && !isInvalidDesc((CpUtf8) e);
	}

	@Nonnull
	protected Predicate<CpEntry> matchUtf8ReturnDescriptor() {
		return e -> {
			if (!(e instanceof CpUtf8 utf8))
				return false;

			// A return descriptor is 'V' or a field type, never a full method descriptor.
			Descriptor d = Descriptor.from(utf8.getText());
			return switch (d.getKind()) {
				case PRIMITIVE, OBJECT, ARRAY -> true;
				default -> false;
			};
		};
	}

	@Nonnull
	protected Predicate<CpEntry> matchUtf8NonEmpty() {
		return e -> e instanceof CpUtf8 && !((CpUtf8) e).getText().isEmpty();
	}

	@Nonnull
	protected Predicate<CpEntry> matchUtf8Word() {
		return e -> e instanceof CpUtf8 utf8 && UTF8_WORD.matcher(utf8.getText()).matches();
	}

	/**
	 * @param descEntry
	 * 		CP entry to validate as a descriptor.
	 *
	 * @return {@code true} when invalid.
	 */
	protected static boolean isInvalidDesc(@Nonnull CpUtf8 descEntry) {
		try {
			String descUtf8 = descEntry.getText();
			Descriptor parsed = Descriptor.from(descUtf8);
			return parsed.getKind() == Descriptor.Kind.ILLEGAL;
		} catch (Throwable t) {
			return true;
		}
	}

	/**
	 * @param code
	 * 		Code to visit.
	 * @param max
	 * 		Max bytecode offset to visit up to.
	 *
	 * @return {@code true} when any visited control flow instruction has an invalid jump target.
	 * IE, does not point to the beginning of a valid instruction.
	 *
	 * @see #checkInvalidJumpFrom(CodeAttribute, int)
	 */
	protected static boolean checkInvalidJumpUpTo(@Nonnull CodeAttribute code, int max) {
		int offset = 0;
		List<Instruction> instructions = code.getInstructions();
		for (int i = 0; i < instructions.size(); i++) {
			Instruction instruction = instructions.get(i);
			if (offset > max)
				break;
			if (isBranch(instruction) && instruction instanceof IntOperandInstruction jump) {
				int target = offset + jump.getOperand();
				if (code.getInstructionAtOffset(target) == null) {
					return true;
				}
			}
			offset += instruction.computeSize();
		}
		return false;
	}

	/**
	 * @param code
	 * 		Code to visit.
	 * @param min
	 * 		Min bytecode offset to visit from.
	 *
	 * @return {@code true} when any visited control flow instruction has an invalid jump target.
	 * IE, does not point to the beginning of a valid instruction.
	 *
	 * @see #checkInvalidJumpUpTo(CodeAttribute, int)
	 */
	protected static boolean checkInvalidJumpFrom(@Nonnull CodeAttribute code, int min) {
		int offset = 0;
		List<Instruction> instructions = code.getInstructions();
		for (int i = 0; i < instructions.size(); i++) {
			Instruction instruction = instructions.get(i);
			if (offset > min)
				if (isBranch(instruction) && instruction instanceof IntOperandInstruction jump) {
					int target = offset + jump.getOperand();
					if (code.getInstructionAtOffset(target) == null) {
						return true;
					}
				}
			offset += instruction.computeSize();
		}
		return false;
	}

	/**
	 * Updates control flow instructions in the given block.
	 *
	 * @param instructions
	 * 		Sequence of instructions to modify.
	 * @param blockRelativeJumpPredicate
	 * 		Predicate operating on jump targets, relative to the start of the block
	 * 		<i>(Defined by the given {@code instructions}, where the first instruction
	 * 		starts at offset 0)</i>
	 * @param rewriter
	 * 		Function that takes in the jump instruction offset and its operand value,
	 * 		and provides a replacement operand value.
	 */
	protected static void shiftJumps(@Nonnull List<Instruction> instructions,
	                                 @Nonnull IntPredicate blockRelativeJumpPredicate,
	                                 @Nonnull JumpRewriter rewriter) {
		int instructionOffset = 0;
		for (Instruction instruction : instructions) {
			if (CodeUtilities.isBranch(instruction)
					&& instruction instanceof IntOperandInstruction jump
					&& blockRelativeJumpPredicate.test(instructionOffset + jump.getOperand())) {
				jump.setOperand(rewriter.rewrite(instructionOffset, jump.getOperand()));
			} else if (instruction instanceof LookupSwitchInstruction switchInsn) {
				int dflt = switchInsn.getDefault();
				if (blockRelativeJumpPredicate.test(instructionOffset + dflt))
					switchInsn.setDefault(rewriter.rewrite(instructionOffset, dflt));
				List<Integer> switchOffsets = switchInsn.getOffsets();
				for (int i = 0; i < switchOffsets.size(); i++) {
					int operand = switchOffsets.get(i);
					if (blockRelativeJumpPredicate.test(instructionOffset + operand))
						switchOffsets.set(i, rewriter.rewrite(instructionOffset, operand));
				}
			} else if (instruction instanceof TableSwitchInstruction switchInsn) {
				int dflt = switchInsn.getDefault();
				if (blockRelativeJumpPredicate.test(instructionOffset + dflt))
					switchInsn.setDefault(rewriter.rewrite(instructionOffset, dflt));
				List<Integer> switchOffsets = switchInsn.getOffsets();
				for (int i = 0; i < switchOffsets.size(); i++) {
					int operand = switchOffsets.get(i);
					if (blockRelativeJumpPredicate.test(instructionOffset + operand))
						switchOffsets.set(i, rewriter.rewrite(instructionOffset, operand));
				}
			}
			instructionOffset += instruction.computeSize();
		}
	}

	/**
	 * Checks a debug range against the decoded instruction boundaries.
	 *
	 * @param code
	 * 		Code containing the range.
	 * @param codeLength
	 * 		Length of the decoded code.
	 * @param startPc
	 * 		Range start.
	 * @param length
	 * 		Range length.
	 *
	 * @return {@code true} when the range is bounded and aligned.
	 */
	protected static boolean isValidCodeRange(@Nonnull CodeAttribute code, int codeLength, int startPc, int length) {
		long endPc = (long) startPc + length;
		return startPc < codeLength &&
				endPc <= codeLength &&
				isInstructionBoundary(code, codeLength, startPc) &&
				(endPc == codeLength || isInstructionBoundary(code, codeLength, (int) endPc));
	}

	/**
	 * Checks whether an offset identifies an instruction start.
	 *
	 * @param code
	 * 		Code containing the offset.
	 * @param codeLength
	 * 		Length of the decoded code.
	 * @param offset
	 * 		Offset to check.
	 *
	 * @return {@code true} when the offset is an instruction boundary.
	 */
	protected static boolean isInstructionBoundary(@Nonnull CodeAttribute code, int codeLength, int offset) {
		return offset >= 0 && offset < codeLength && code.getInstructionAtOffset(offset) != null;
	}

	/**
	 * @param code
	 * 		Code to disassemble.
	 *
	 * @return Disassembly.
	 */
	@Nonnull
	protected static String disassemble(@Nonnull CodeAttribute code) {
		String dis;
		StringBuilder sb = new StringBuilder();
		for (Instruction instruction : code.getInstructions()) {
			sb.append(code.computeOffsetOf(instruction)).append(": ").append(instruction).append('\n');
		}
		dis = sb.toString();
		return dis;
	}

	/**
	 * @see #shiftJumps(List, IntPredicate, JumpRewriter)
	 */
	public interface JumpRewriter {
		int rewrite(int instructionOffset, int jumpOperand);
	}
}
