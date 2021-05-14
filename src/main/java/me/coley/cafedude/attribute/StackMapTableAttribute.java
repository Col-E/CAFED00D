package me.coley.cafedude.attribute;

/**
 * <p>
 * Used during the process of verification by type checking.
 * </p>
 * 
 * <p>
 * There may be at most one StackMapTable attribute in the attributes table of
 * a Code attribute.
 * </p>
 * 
 * <p>
 * In a class file whose version number is 50.0 or above, if a method's Code
 * attribute does not have a StackMapTable attribute, it has an implicit stack
 * map attribute (§4.10.1). This implicit stack map attribute is equivalent to
 * a StackMapTable attribute with number_of_entries equal to zero.
 * </p>
 * 
 * @author x4e
 */
public class StackMapTableAttribute extends Attribute {
	public final StackMapFrame[] frames;

	/**
	 * @param nameIndex Name index in constant pool.
	 * @param frames An array of stack map frames.
	 */
	public StackMapTableAttribute(int nameIndex, StackMapFrame[] frames) {
		super(nameIndex);
		this.frames = frames;
	}

	@Override
	public int computeInternalLength() {
		// u2 number_of_entries
		int length = 2;
		for (StackMapFrame frame : frames) {
			length += frame.computeCompleteLength();
		}
		return length;
	}

	/**
	 * A verification type specifies the type of either one or two locations,
	 * where a location is either a single local variable or a single operand
	 * stack entry. A verification type consists of a one-byte tag, indicating
	 * which type is in use, followed by zero or more bytes, giving more
	 * information about the tag.
	 */
	public abstract static class TypeInfo {
		protected int computeInternalLength() {
			// By default no contents
			return 0;
		}

		/**
		 * @return Size in bytes of the serialized type info.
		 */
		public int computeCompleteLength() {
			// u1 tag
			return 1 + computeInternalLength();
		}
	}

	/**
	 * Indicates that the local variable has the verification type top.
	 */
	public static class TopVariableInfo extends TypeInfo {
	}

	/**
	 * Indicates that the location has the verification type int.
	 */
	public static class IntegerVariableInfo extends TypeInfo {
	}

	/**
	 * Indicates that the location has the verification type float.
	 */
	public static class FloatVariableInfo extends TypeInfo {
	}

	/**
	 * Indicates that the location has the verification type null.
	 */
	public static class NullVariableInfo extends TypeInfo {
	}

	/**
	 * Indicates that the location has the verification type uninitializedThis.
	 */
	public static class UninitializedThisVariableInfo extends TypeInfo {
	}

	/**
	 * Indicates that the location has the verification type which is the class
	 * represented by the CONSTANT_Class_info found at classIndex.
	 */
	public static class ObjectVariableInfo extends TypeInfo {
		/**
		 */
		public int classIndex;

		/**
		 * @param classIndex Index of the ClassConstant representing type of this
		 * variable.
		 */
		public ObjectVariableInfo(int classIndex) {
			this.classIndex = classIndex;
		}

		@Override
		protected int computeInternalLength() {
			// u2 cpool_index
			return 2;
		}
	}

	/**
	 * Indicates that the location has the verification type uninitialized.
	 */
	public static class UninitializedVariableInfo extends TypeInfo {
		/**
		 * Indicates the offset in the code of the new instruction that created
		 * the object being stored in the location.
		 */
		public int offset;

		/**
		 * @param offset The offset in the code of the new instruction that
		 * created the object being stored in the location.
		 */
		public UninitializedVariableInfo(int offset) {
			this.offset = offset;
		}

		@Override
		protected int computeInternalLength() {
			// u2 offset
			return 2;
		}
	}

	/**
	 * Indicates the verification type long.
	 */
	public static class LongVariableInfo extends TypeInfo {
	}

	/**
	 * Indicates the verification type double.
	 */
	public static class DoubleVariableInfo extends TypeInfo {
	}

	/**
	 * <p>
	 * A stack map frame specifies (either explicitly or implicitly) the bytecode
	 * offset at which it applies, and the verification types of local variables
	 * and operand stack entries for that offset.
	 * </p>
	 * 
	 * <p>
	 * The bytecode offset at which a stack map frame applies is calculated by
	 * taking the offset_delta of the frame, and adding offset_delta + 1 to the
	 * bytecode offset of the previous frame, unless the previous frame is the
	 * initial frame of the method. In that case, the bytecode offset at which
	 * the stack map frame applies is the value offset_delta specified in the
	 * frame.
	 * </p>
	 */
	public abstract static class StackMapFrame {
		/**
		 * The offset delta of this frame.
		 */
		public int offsetDelta;

		/**
		 * @param offsetDelta The offset delta of this frame.
		 */
		public StackMapFrame(int offsetDelta) {
			this.offsetDelta = offsetDelta;
		}

		protected int computeInternalLength() {
			// By default no contents
			return 0;
		}

		/**
		 * @return Size in bytes of the serialized frame.
		 */
		public int computeCompleteLength() {
			// u1 frame_type
			return 1 + computeInternalLength();
		}
	}

	/**
	 * This frame type indicates that the frame has exactly the same local
	 * variables as the previous frame and that the operand stack is empty.
	 */
	public static class SameFrame extends StackMapFrame {		
		/**
		 * @param offsetDelta The offset delta of this frame.
		 */
		public SameFrame(int offsetDelta) {
			super(offsetDelta);
		}
	}

	/**
	 * This frame type indicates that the frame has exactly the same local
	 * variables as the previous frame and that the operand stack has one entry.
	 * The offset_delta value for the frame is given by the formula frame_type -
	 * 64. The verification type of the one stack entry appears after the frame
	 * type.
	*/
	public static class SameLocalsOneStackItem extends StackMapFrame {
		/**
		 * The singular stack item.
		 */
		public TypeInfo stack;

		/**
		 * @param offsetDelta The offset delta of this frame.
	 	 * @param stack The singular stack item.
		 */
		public SameLocalsOneStackItem(int offsetDelta, TypeInfo stack) {
			super(offsetDelta);
			this.stack = stack;
		}

		@Override
		protected int computeInternalLength() {
			return stack.computeCompleteLength();
		}
	}

	/**
	 * Same as {@link SameLocalsOneStackItem} except has an explicit offsetDelta.
	 */
	public static class SameLocalsOneStackItemExtended extends StackMapFrame {
		/**
		 * The singular stack item.
		 */
		public TypeInfo stack;

		/**
		 * @param offsetDelta The offset delta of this frame.
	 	 * @param stack The singular stack item.
		 */
		public SameLocalsOneStackItemExtended(int offsetDelta, TypeInfo stack) {
			super(offsetDelta);
			this.stack = stack;
		}

		@Override
		protected int computeInternalLength() {
			// u2 offset_delta
			return 2 + stack.computeCompleteLength();
		}
	}

	/**
	 * This frame type indicates that the frame has the same local variables as
	 * the previous frame except that a given number of the last local variables
	 * are absent, and that the operand stack is empty.
	 */
	public static class ChopFrame extends StackMapFrame {
		/**
		 * The number of the last local variables that are now absent.
		 */
		public int absentVariables;

		/**
		 * @param offsetDelta The offset delta of this frame.
		 * @param absentVariables The number of chopped local variables.
		 * absent.
		 */
		public ChopFrame(int offsetDelta, int absentVariables) {
			super(offsetDelta);
			this.absentVariables = absentVariables;
		}

		@Override
		protected int computeInternalLength() {
			// u2 offset_delta
			return 2;
		}
	}

	/**
	 * This frame type indicates that the frame has exactly the same local
	 * variables as the previous frame and that the operand stack is empty.
	 */
	public static class SameFrameExtended extends StackMapFrame {
		/**
		 * @param offsetDelta The offset delta of this frame.
		 */
		public SameFrameExtended(int offsetDelta) {
			super(offsetDelta);
		}

		@Override
		protected int computeInternalLength() {
			// u2 offset_delta
			return 2;
		}
	}

	/**
	 * This frame type indicates that the frame has the same locals as the
	 * previous frame except that a number of additional locals are defined, and
	 * that the operand stack is empty.
	 */
	public static class AppendFrame extends StackMapFrame {
		/**
		 * Additional locals defined in the current frame.
		 */
		public TypeInfo[] additionalLocals;

		/**
		 * @param offsetDelta The offset delta of this frame.
		 * @param additionalLocals The additional locals defined in the frame.
		 */
		public AppendFrame(int offsetDelta, TypeInfo[] additionalLocals) {
			super(offsetDelta);
			this.additionalLocals = additionalLocals;
		}

		@Override
		protected int computeInternalLength() {
			// u2 offset_delta
			int length = 2;
			for (TypeInfo local : additionalLocals) {
				length += local.computeCompleteLength();
			}
			return length;
		}
	}

	/**
	 * Contains the full types of the current frame.
	 */
	public static class FullFrame extends StackMapFrame {
		/**
		 * The local variable types of the current frame.
		 */
		public TypeInfo[] locals;
		/**
		 * The types of the current frame's stack.
		 */
		public TypeInfo[] stack;

		/**
		 * @param offsetDelta The offset delta of this frame.
		 * @param locals The local variable types of the current frame.
		 * @param stack The types of the current frame's stack.
		 */
		public FullFrame(int offsetDelta, TypeInfo[] locals, TypeInfo[] stack) {
			super(offsetDelta);
			this.locals = locals;
			this.stack = stack;
		}

		@Override
		protected int computeInternalLength() {
			// u2 offset_delta
			// u2 number_of_locals
			// u2 number_of_stack_items
			int length = 2 + 2 + 2;
			for (TypeInfo local : locals) {
				length += local.computeCompleteLength();
			}
			for (TypeInfo stackType : stack) {
				length += stackType.computeCompleteLength();
			}
			return length;
		}
	}
}
