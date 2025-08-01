package software.coley.cafedude.classfile.attribute;

import jakarta.annotation.Nonnull;
import software.coley.cafedude.classfile.behavior.CpAccessor;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used during the process of verification by type checking.
 * <br>
 * There may be at most one StackMapTable attribute in the attributes table of
 * a Code attribute.
 * <br>
 * In a class file whose version number is 50.0 or above, if a method's Code
 * attribute does not have a StackMapTable attribute, it has an implicit stack
 * map attribute (§4.10.1). This implicit stack map attribute is equivalent to
 * a StackMapTable attribute with number_of_entries equal to zero.
 *
 * @author x4e
 */
public non-sealed class StackMapTableAttribute
		extends Attribute
		implements StackMapTableConstants {
	/**
	 * List of this table's stack map frames.
	 */
	private List<StackMapFrame> frames;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param frames
	 * 		Stack map frames of a method.
	 */
	public StackMapTableAttribute(@Nonnull CpUtf8 name, @Nonnull List<StackMapFrame> frames) {
		super(name);
		this.frames = frames;
	}

	/**
	 * @return Stack map frames of a method.
	 */
	@Nonnull
	public List<StackMapFrame> getFrames() {
		return frames;
	}

	/**
	 * @param frames
	 * 		Stack map frames of a method.
	 */
	public void setFrames(@Nonnull List<StackMapFrame> frames) {
		this.frames = frames;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		for (StackMapFrame frame : frames)
			set.addAll(frame.cpAccesses());
		return set;
	}

	@Override
	public int computeInternalLength() {
		// u2: number_of_entries
		int length = 2;
		// ??: attribute_entries
		for (StackMapFrame frame : frames) {
			length += frame.getLength();
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
	public sealed abstract static class TypeInfo implements CpAccessor permits DoubleVariableInfo, FloatVariableInfo,
			IntegerVariableInfo, LongVariableInfo, NullVariableInfo, ObjectVariableInfo, TopVariableInfo,
			UninitializedThisVariableInfo, UninitializedVariableInfo {
		/**
		 * @return The one byte tag representing this type.
		 */
		public abstract int getTag();

		/**
		 * @return Size in bytes of the serialized type info.
		 */
		public int getLength() {
			// u1: tag
			return 1;
		}

		@Nonnull
		@Override
		public Set<CpEntry> cpAccesses() {
			return Collections.emptySet();
		}
	}

	/**
	 * Indicates that the local variable has the verification type top.
	 */
	public static non-sealed class TopVariableInfo extends TypeInfo {
		/**
		 * @return The one byte tag representing this type.
		 */
		@Override
		public int getTag() {
			return ITEM_TOP;
		}
	}

	/**
	 * Indicates that the location has the verification type int.
	 */
	public static non-sealed class IntegerVariableInfo extends TypeInfo {
		/**
		 * @return The one byte tag representing this type.
		 */
		@Override
		public int getTag() {
			return ITEM_INTEGER;
		}
	}

	/**
	 * Indicates that the location has the verification type float.
	 */
	public static non-sealed class FloatVariableInfo extends TypeInfo {
		/**
		 * @return The one byte tag representing this type.
		 */
		@Override
		public int getTag() {
			return ITEM_FLOAT;
		}
	}

	/**
	 * Indicates that the location has the verification type null.
	 */
	public static non-sealed class NullVariableInfo extends TypeInfo {
		/**
		 * @return The one byte tag representing this type.
		 */
		@Override
		public int getTag() {
			return ITEM_NULL;
		}
	}

	/**
	 * Indicates that the location has the verification type uninitializedThis.
	 */
	public static non-sealed class UninitializedThisVariableInfo extends TypeInfo {
		/**
		 * @return The one byte tag representing this type.
		 */
		@Override
		public int getTag() {
			return ITEM_UNINITIALIZED_THIS;
		}
	}

	/**
	 * Indicates that the location has the verification type which is the class
	 * represented by the CONSTANT_Class_info found at classIndex.
	 */
	public static non-sealed class ObjectVariableInfo extends TypeInfo {
		/**
		 * @return The one byte tag representing this type.
		 */
		@Override
		public int getTag() {
			return ITEM_OBJECT;
		}

		private CpClass classEntry;

		/**
		 * @param classEntry
		 * 		Constant pool entry holding the type of this variable.
		 */
		public ObjectVariableInfo(@Nonnull CpClass classEntry) {
			this.classEntry = classEntry;
		}

		@Nonnull
		@Override
		public Set<CpEntry> cpAccesses() {
			return Collections.singleton(getClassEntry());
		}

		/**
		 * @return Size in bytes of the serialized type info.
		 */
		@Override
		public int getLength() {
			// u1: tag
			// u2: cpool_index
			return 1 + 2;
		}

		/**
		 * @return Constant pool entry holding the type of this variable.
		 */
		@Nonnull
		public CpClass getClassEntry() {
			return classEntry;
		}

		/**
		 * @param classEntry
		 * 		New constant pool entry holding the type of this variable.
		 */
		public void setClassEntry(@Nonnull CpClass classEntry) {
			this.classEntry = classEntry;
		}
	}

	/**
	 * Indicates that the location has the verification type uninitialized.
	 */
	public static non-sealed class UninitializedVariableInfo extends TypeInfo {
		/**
		 * @return The one byte tag representing this type.
		 */
		@Override
		public int getTag() {
			return ITEM_UNINITIALIZED;
		}

		private int offset;

		/**
		 * @param offset
		 * 		The offset in the code of the new instruction that
		 * 		created the object being stored in the location.
		 */
		public UninitializedVariableInfo(int offset) {
			this.offset = offset;
		}

		/**
		 * @return Size in bytes of the serialized type info.
		 */
		@Override
		public int getLength() {
			// u1 tag
			// u2 offset
			return 1 + 2;
		}

		/**
		 * @return The offset in the code of the new instruction that
		 * created the object being stored in the location.
		 */
		public int getOffset() {
			return offset;
		}

		/**
		 * @param offset
		 * 		New offset in the code of the new instruction that
		 * 		created the object being stored in the location.
		 */
		public void setOffset(int offset) {
			this.offset = offset;
		}
	}

	/**
	 * Indicates the verification type long.
	 */
	public static non-sealed class LongVariableInfo extends TypeInfo {
		/**
		 * @return The one byte tag representing this type.
		 */
		@Override
		public int getTag() {
			return ITEM_LONG;
		}
	}

	/**
	 * Indicates the verification type double.
	 */
	public static non-sealed class DoubleVariableInfo extends TypeInfo {
		/**
		 * @return The one byte tag representing this type.
		 */
		@Override
		public int getTag() {
			return ITEM_DOUBLE;
		}
	}

	/**
	 * A stack map frame specifies <i>(either explicitly or implicitly)</i> the
	 * bytecode offset at which it applies, and the verification types of local
	 * variables and operand stack entries for that offset.
	 * <br>
	 * The bytecode offset at which a stack map frame applies is calculated by taking
	 * the {@code offset_delta} of the frame, and {@code adding offset_delta + 1} to
	 * the bytecode offset of the previous frame, unless the previous frame is the
	 * initial frame of the method. In that case, the bytecode offset at which the
	 * stack map frame applies is the value {@code offset_delta} specified in the frame.
	 */
	public sealed abstract static class StackMapFrame implements CpAccessor permits AppendFrame, ChopFrame, FullFrame,
			SameFrame, SameFrameExtended, SameLocalsOneStackItem, SameLocalsOneStackItemExtended {
		private int offsetDelta;

		/**
		 * @param offsetDelta
		 * 		The offset delta of this frame.
		 */
		public StackMapFrame(int offsetDelta) {
			this.offsetDelta = offsetDelta;
		}

		/**
		 * @return The one byte frame type representing this frame.
		 */
		public abstract int getFrameType();

		/**
		 * @return Size in bytes of the serialized frame.
		 */
		public int getLength() {
			// u1 frame_type
			return 1;
		}

		@Nonnull
		@Override
		public Set<CpEntry> cpAccesses() {
			return Collections.emptySet();
		}

		/**
		 * @return The offset delta of this frame.
		 */
		public int getOffsetDelta() {
			return offsetDelta;
		}

		/**
		 * @param offsetDelta
		 * 		New offset delta of this frame.
		 */
		public void setOffsetDelta(int offsetDelta) {
			this.offsetDelta = offsetDelta;
		}
	}

	/**
	 * This frame type indicates that the frame has exactly the same local
	 * variables as the previous frame and that the operand stack is empty.
	 */
	public static non-sealed class SameFrame extends StackMapFrame {
		/**
		 * @param offsetDelta
		 * 		The offset delta of this frame.
		 */
		public SameFrame(int offsetDelta) {
			super(offsetDelta);
		}

		/**
		 * @return The one byte frame type representing this frame.
		 */
		public int getFrameType() {
			return SAME_FRAME_MIN + getOffsetDelta();
		}
	}

	/**
	 * This frame type indicates that the frame has exactly the same local
	 * variables as the previous frame and that the operand stack has one entry.
	 * The {@code offset_delta} value for the frame is given by the formula:
	 * {@code frame_type - 64}
	 * <br>
	 * The verification type of the one stack entry appears after the frame type.
	 */
	public static non-sealed class SameLocalsOneStackItem extends StackMapFrame {
		private TypeInfo stack;

		/**
		 * @param offsetDelta
		 * 		The offset delta of this frame.
		 * @param stack
		 * 		The singular stack item.
		 */
		public SameLocalsOneStackItem(int offsetDelta, @Nonnull TypeInfo stack) {
			super(offsetDelta);
			this.stack = stack;
		}

		@Nonnull
		@Override
		public Set<CpEntry> cpAccesses() {
			return getStack().cpAccesses();
		}

		/**
		 * @return Size in bytes of the serialized frame.
		 */
		@Override
		public int getLength() {
			// u1 frame_type
			// verification_type_info stack
			return 1 + getStack().getLength();
		}

		/**
		 * @return The one byte frame type representing this frame.
		 */
		public int getFrameType() {
			return SAME_LOCALS_ONE_STACK_ITEM_MIN + getOffsetDelta();
		}

		/**
		 * @return The singular stack item.
		 */
		@Nonnull
		public TypeInfo getStack() {
			return stack;
		}

		/**
		 * @param stack
		 * 		New singular stack item.
		 */
		public void setStack(@Nonnull TypeInfo stack) {
			this.stack = stack;
		}
	}

	/**
	 * Same as {@link SameLocalsOneStackItem} except has an explicit {@code offsetDelta}.
	 */
	public static non-sealed class SameLocalsOneStackItemExtended extends StackMapFrame {
		private TypeInfo stack;

		/**
		 * @param offsetDelta
		 * 		The offset delta of this frame.
		 * @param stack
		 * 		The singular stack item.
		 */
		public SameLocalsOneStackItemExtended(int offsetDelta, @Nonnull TypeInfo stack) {
			super(offsetDelta);
			this.stack = stack;
		}

		@Nonnull
		@Override
		public Set<CpEntry> cpAccesses() {
			return getStack().cpAccesses();
		}

		/**
		 * @return Size in bytes of the serialized frame.
		 */
		@Override
		public int getLength() {
			// u1: frame_type
			// u2: offset_delta
			// verification_type_info stack
			return 1 + 2 + getStack().getLength();
		}

		/**
		 * @return The one byte frame type representing this frame.
		 */
		public int getFrameType() {
			return SAME_LOCALS_ONE_STACK_ITEM_EXTENDED_MIN;
		}

		/**
		 * @return The singular stack item.
		 */
		@Nonnull
		public TypeInfo getStack() {
			return stack;
		}

		/**
		 * @param stack
		 * 		New singular stack item.
		 */
		public void setStack(@Nonnull TypeInfo stack) {
			this.stack = stack;
		}
	}

	/**
	 * This frame type indicates that the frame has the same local variables as
	 * the previous frame except that a given number of the last local variables
	 * are absent, and that the operand stack is empty.
	 */
	public static non-sealed class ChopFrame extends StackMapFrame {
		private int absentVariables;

		/**
		 * @param offsetDelta
		 * 		The offset delta of this frame.
		 * @param absentVariables
		 * 		The number of chopped local variables.
		 * 		absent.
		 */
		public ChopFrame(int offsetDelta, int absentVariables) {
			super(offsetDelta);
			this.absentVariables = absentVariables;
		}

		/**
		 * @return Size in bytes of the serialized frame.
		 */
		@Override
		public int getLength() {
			// u1: frame_type
			// u2: offset_delta
			return 1 + 2;
		}

		/**
		 * @return The one byte frame type representing this frame.
		 */
		public int getFrameType() {
			// 1 needs to be added, format starts at 1 instead of 0 as having a
			// chop frame that chops 0 locals would be redundant
			return CHOP_FRAME_MAX - getAbsentVariables() + 1;
		}

		/**
		 * @return The number of the last local variables that are now absent.
		 */
		public int getAbsentVariables() {
			return absentVariables;
		}

		/**
		 * @param absentVariables
		 * 		New number of the last local variables that are now absent.
		 */
		public void setAbsentVariables(int absentVariables) {
			this.absentVariables = absentVariables;
		}
	}

	/**
	 * This frame type indicates that the frame has exactly the same local
	 * variables as the previous frame and that the operand stack is empty.
	 */
	public static non-sealed class SameFrameExtended extends StackMapFrame {
		/**
		 * @param offsetDelta
		 * 		The offset delta of this frame.
		 */
		public SameFrameExtended(int offsetDelta) {
			super(offsetDelta);
		}

		/**
		 * @return Size in bytes of the serialized frame.
		 */
		@Override
		public int getLength() {
			// u1: frame_type
			// u2: offset_delta
			return 1 + 2;
		}

		/**
		 * @return The one byte frame type representing this frame.
		 */
		public int getFrameType() {
			return SAME_FRAME_EXTENDED_MIN;
		}
	}

	/**
	 * This frame type indicates that the frame has the same locals as the
	 * previous frame except that a number of additional locals are defined, and
	 * that the operand stack is empty.
	 */
	public static non-sealed class AppendFrame extends StackMapFrame {
		private List<TypeInfo> additionalLocals;

		/**
		 * @param offsetDelta
		 * 		The offset delta of this frame.
		 * @param additionalLocals
		 * 		The additional locals defined in the frame.
		 */
		public AppendFrame(int offsetDelta, @Nonnull List<TypeInfo> additionalLocals) {
			super(offsetDelta);
			this.additionalLocals = additionalLocals;
		}

		@Nonnull
		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			for (TypeInfo info : getAdditionalLocals())
				set.addAll(info.cpAccesses());
			return set;
		}

		/**
		 * @return Size in bytes of the serialized frame.
		 */
		@Override
		public int getLength() {
			// u1: frame_type
			// u2: offset_delta
			int length = 1 + 2;
			// verification_type_info locals[frame_type - 251]
			for (TypeInfo local : getAdditionalLocals()) {
				length += local.getLength();
			}
			return length;
		}

		/**
		 * @return The one byte frame type representing this frame.
		 */
		public int getFrameType() {
			return getAdditionalLocals().size() + APPEND_FRAME_MIN - 1;
		}

		/**
		 * @return Additional locals defined in the current frame.
		 */
		@Nonnull
		public List<TypeInfo> getAdditionalLocals() {
			return additionalLocals;
		}

		/**
		 * @param additionalLocals
		 * 		New additional locals defined in the current frame.
		 */
		public void setAdditionalLocals(@Nonnull List<TypeInfo> additionalLocals) {
			this.additionalLocals = additionalLocals;
		}
	}

	/**
	 * Contains the full types of the current frame.
	 */
	public static non-sealed class FullFrame extends StackMapFrame {
		private List<TypeInfo> locals;
		private List<TypeInfo> stack;

		/**
		 * @param offsetDelta
		 * 		The offset delta of this frame.
		 * @param locals
		 * 		The local variable types of the current frame.
		 * @param stack
		 * 		The types of the current frame's stack.
		 */
		public FullFrame(int offsetDelta, @Nonnull List<TypeInfo> locals, @Nonnull List<TypeInfo> stack) {
			super(offsetDelta);
			this.locals = locals;
			this.stack = stack;
		}

		@Nonnull
		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			for (TypeInfo info : getLocals())
				set.addAll(info.cpAccesses());
			for (TypeInfo info : getStack())
				set.addAll(info.cpAccesses());
			return set;
		}

		/**
		 * @return Size in bytes of the serialized frame.
		 */
		@Override
		public int getLength() {
			// u1: frame_type
			// u2: offset_delta
			int length = 1 + 2;
			// u2 number_of_locals
			// verification_type_info locals[number_of_locals]
			length += 2;
			for (TypeInfo local : getLocals()) {
				length += local.getLength();
			}
			// u2 number_of_stack_items
			// verification_type_info stack[number_of_stack_items]
			length += 2;
			for (TypeInfo stackType : getStack()) {
				length += stackType.getLength();
			}
			return length;
		}

		/**
		 * @return The one byte frame type representing this frame.
		 */
		public int getFrameType() {
			return FULL_FRAME_MIN;
		}

		/**
		 * @return The local variable types of the current frame.
		 */
		@Nonnull
		public List<TypeInfo> getLocals() {
			return locals;
		}

		/**
		 * @param locals
		 * 		New local variable types of the current frame.
		 */
		public void setLocals(@Nonnull List<TypeInfo> locals) {
			this.locals = locals;
		}

		/**
		 * @return The types of the current frame's stack.
		 */
		@Nonnull
		public List<TypeInfo> getStack() {
			return stack;
		}

		/**
		 * @param stack
		 * 		New types of the current frame's stack.
		 */
		public void setStack(@Nonnull List<TypeInfo> stack) {
			this.stack = stack;
		}
	}
}
