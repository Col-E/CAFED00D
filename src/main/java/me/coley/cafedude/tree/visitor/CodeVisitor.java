package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.Handle;
import me.coley.cafedude.tree.Label;
import org.jetbrains.annotations.Nullable;

/**
 * Visitor to visit the various instructions of a method.
 */
@SuppressWarnings("unused")
public interface CodeVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	@Nullable
	default CodeVisitor codeDelegate() {
		return null;
	}

	/**
	 * Visit a {@link Opcodes#NOP} instruction.
	 */
	default void visitNop() {
		CodeVisitor cv = codeDelegate();
		if(cv != null) cv.visitNop();
	}

	/**
	 * Visit a {@link Opcodes#ATHROW} instruction.
	 */
	default void visitThrow() {
		CodeVisitor cv = codeDelegate();
		if(cv != null) cv.visitThrow();
	}

	/**
	 * Visit a monitor control instruction.
	 *
	 * @param opcode
	 * 			{@link Opcodes#MONITORENTER} or {@link Opcodes#MONITOREXIT}.
	 */
	default void visitMonitorInsn(int opcode) {
		CodeVisitor cv = codeDelegate();
		if(cv != null) cv.visitMonitorInsn(opcode);
	}

	/**
	 * Visit a return instruction which returns a value from a method.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of: RETURN, IRETURN, LRETURN, FRETURN,
	 * 			DRETURN or ARETURN
	 */
	default void visitReturnInsn(int opcode) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitReturnInsn(opcode);
	}

	/**
	 * Visit a constant pushing instruction with no operand.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of: ACONST_NULL, ICONST_M1, ICONST_0,
	 * 			ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0,
	 * 			FCONST_1, FCONST_2, DCONST_0 or DCONST_1
	 */
	default void visitConstantInsn(int opcode) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitConstantInsn(opcode);
	}

	/**
	 * Visit an arithmetic instruction which performs an operation on two values on the stack.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of: IADD, LADD, FADD, DADD, ISUB, LSUB,
	 * 			FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM,
	 * 			INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR,
	 * 			IXOR, LXOR, I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C,
	 * 			I2S, LCMP, FCMPL, FCMPG, DCMPL or DCMPG
	 */
	default void visitArithmeticInsn(int opcode) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitArithmeticInsn(opcode);
	}

	/**
	 * Visit an array operation instruction which performs an operation on an array.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of: IALOAD, LALOAD, FALOAD, DALOAD,
	 * 			AALOAD, BALOAD, CALOAD, SALOAD, IASTORE, LASTORE, FASTORE, DASTORE, AASTORE,
	 * 			BASTORE, CASTORE or SASTORE
	 */
	default void visitArrayInsn(int opcode) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitArrayInsn(opcode);
	}

	/**
	 * Visit a stack operation instruction which performs an operation on the stack.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of: POP, POP2, DUP, DUP_X1, DUP_X2,
	 * 			DUP2, DUP2_X1, DUP2_X2 or SWAP
	 */
	default void visitStackInsn(int opcode) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitStackInsn(opcode);
	}

	/**
	 * Visit a label which is designated as a jump target.
	 *
	 * @param label
	 * 			Label to visit.
	 */
	default void visitLabel(Label label) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitLabel(label);
	}

	/**
	 * Visit a line number
	 *
	 * @param line
	 * 			Line number.
	 * @param start
	 * 			Label of the instruction which starts the line.
	 */
	default void visitLineNumber(int line, Label start) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitLineNumber(line, start);
	}

	/**
	 * Visit an integer operand instruction.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of: BIPUSH, SIPUSH, NEWARRAY or RET
	 * @param operand
	 * 			operand of the instruction. For BI/SIPUSH, the operand is a constant value to push
	 * 			onto the stack, for NEWARRAY it is the type of array to create, and for RET it is
	 * 			the local variable index of the return address.
	 */
	default void visitIntInsn(int opcode, int operand) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitIntInsn(opcode, operand);
	}

	/**
	 * Visit a variable instruction which loads or stores a value from a local variable.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of: ILOAD, LLOAD, FLOAD, DLOAD, ALOAD,
	 * 			ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, ILOAD_N, LLOAD_N, FLOAD_N, DLOAD_N, ALOAD_N,
	 * 			ISTORE_N, LSTORE_N, FSTORE_N, DSTORE_N, ASTORE_N where N is a number from 0 to 3.
	 * 			Generally XLOAD_N and XSTORE_N are used for the first 4 local variables, and XLOAD
	 * 			and XSTORE are used for the rest.
	 * @param var
	 * 			local variable index to load/store from/to.
	 */
	default void visitVarInsn(int opcode, int var) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitVarInsn(opcode, var);
	}

	/**
	 * Visit a type instruction which accepts a class type as an operand.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of: NEW, ANEWARRAY, CHECKCAST or INSTANCEOF
	 * @param type
	 * 			type of the instruction. For NEW it is the type of object to create, for ANEWARRAY it
	 * 			is the type of array to create, for CHECKCAST it is the type to cast to, and for INSTANCEOF
	 * 			it is the type to check if the object is an instance of.
	 */
	default void visitTypeInsn(int opcode, String type) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitTypeInsn(opcode, type);
	}

	/**
	 * Visit a field instruction which loads or stores a value from a field.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of: GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD
	 * @param owner
	 * 			owner of the field. Owner is the class which contains this field.
	 * @param name
	 * 			name of the field.
	 * @param type
	 * 			type descriptor of the field.
	 */
	default void visitFieldInsn(int opcode, String owner, String name, Descriptor type) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitFieldInsn(opcode, owner, name, type);
	}

	/**
	 * Visit a method instruction which invokes a method.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of:
	 * 			INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or INVOKEINTERFACE
	 * @param owner
	 * 			owner of the method. Owner is the class which contains this method.
	 * @param name
	 * 			name of the method.
	 * @param descriptor
	 * 			descriptor of the method.
	 */
	default void visitMethodInsn(int opcode, String owner, String name, Descriptor descriptor) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitMethodInsn(opcode, owner, name, descriptor);
	}

	/**
	 * Visit an invoke dynamic instruction for dynamic method invocation.
	 *
	 * @param name
	 * 			name of the call site.
	 * @param descriptor
	 * 			descriptor of the call site.
	 * @param bootstrapMethod
	 * 			handle to the bootstrap method.
	 * @param bootstrapArgs
	 * 			{@link Constant} array of arguments to the bootstrap method.
	 * @see Opcodes#INVOKEDYNAMIC
	 * @see Handle
	 * @see Constant
	 */
	default void visitInvokeDynamicInsn(String name, Descriptor descriptor, Handle bootstrapMethod,
										Constant... bootstrapArgs) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitInvokeDynamicInsn(name, descriptor, bootstrapMethod, bootstrapArgs);
	}

	/**
	 * Visit a jump instruction which jumps to a label.
	 *
	 * @param opcode
	 * 			opcode of the instruction. The opcode should be one of: IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE,
	 * 			IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
	 * 			GOTO, JSR, IFNULL or IFNONNULL
	 * @param label
	 * 			label to jump to.
	 */
	default void visitFlowInsn(int opcode, Label label) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitFlowInsn(opcode, label);
	}

	/**
	 * Visit a LDC instruction which pushes the constant operand onto the stack.
	 *
	 * @param constant
	 * 			constant to push onto the stack. The type of the constant can be all types of
	 * 			{@link Constant.Type}.
	 * @see Constant
	 */
	default void visitLdcInsn(Constant constant) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitLdcInsn(constant);
	}

	/**
	 * Visit an IINC instruction which increments a local variable by a constant.
	 *
	 * @param var
	 * 			local variable to increment.
	 * @param increment
	 * 			amount to increment the local variable by.
	 */
	default void visitIIncInsn(int var, int increment) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitIIncInsn(var, increment);
	}

	/**
	 * Visit a table switch instruction which jumps to one of a set of labels depending on the value
	 * which lies between a minimum and maximum value.
	 *
	 * @param min
	 * 			minimum value of the switch.
	 * @param max
	 * 			maximum value of the switch.
	 * @param defaultLabel
	 * 			label to jump to if the value is not between the minimum and maximum value.
	 * @param labels
	 * 			array of labels to jump to depending on the value.
	 */
	default void visitTableSwitchInsn(int min, int max, Label defaultLabel, Label... labels) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitTableSwitchInsn(min, max, defaultLabel, labels);
	}

	/**
	 * Visit a lookup switch instruction which jumps to one of a set of labels depending on a lookup in a keys array.
	 *
	 * @param defaultLabel
	 * 			label to jump to if the value is not in the keys array.
	 * @param keys
	 * 			array of keys to lookup in.
	 * @param labels
	 * 			array of labels to jump to depending on the value.
	 */
	default void visitLookupSwitchInsn(Label defaultLabel, int[] keys, Label... labels) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitLookupSwitchInsn(defaultLabel, keys, labels);
	}

	/**
	 * Visit a MULTIANEWARRAY instruction which creates a new multidimensional array.
	 *
	 * @param type
	 * 			class type of the array.
	 * @param dimensions
	 * 			number of dimensions of the array.
	 */
	default void visitMultiANewArrayInsn(String type, int dimensions) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitMultiANewArrayInsn(type, dimensions);
	}

	/**
	 * Visit the codes max stack and local sizes.
	 * @param maxStack
	 * 			maximum stack size.
	 * @param maxLocal
	 * 			maximum local variable size.
	 */
	default void visitMaxs(int maxStack, int maxLocal) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitMaxs(maxStack, maxLocal);
	}

	/**
	 * Visit a local variable declaration.
	 *
	 * @param index
	 * 			index of the local variable.
	 * @param name
	 * 			name of the local variable.
	 * @param descriptor
	 * 			descriptor of the local variable.
	 * @param signature
	 * 			signature of the local variable.
	 * 			{@code null} if the local variable type does not use generic types.
	 * @param start
	 * 			label of the first instruction where the local variable is defined.
	 * @param end
	 * 			label of the last instruction where the local variable is defined.
	 */
	default void visitLocalVariable(int index, String name, Descriptor descriptor, @Nullable String signature,
									Label start, Label end) {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitLocalVariable(index, name, descriptor, signature, start, end);
	}

	/**
	 * Visit the end of the code.
	 */
	default void visitCodeEnd() {
		CodeVisitor cv = codeDelegate();
		if (cv != null) cv.visitCodeEnd();
	}

}
