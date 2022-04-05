package me.coley.cafedude.classfile;

/**
 * Constants for the stack map table.
 *
 * @author Matt Coley
 */
public interface StackMapTableConstants {
	/**
	 * Indicates the verification type {@code top}.
	 */
	int ITEM_TOP = 0;
	/**
	 * Indicates the verification type {@code int}.
	 */
	int ITEM_INTEGER = 1;
	/**
	 * Indicates the verification type {@code float}.
	 */
	int ITEM_FLOAT = 2;
	/**
	 * Indicates the verification type {@code double}.
	 */
	int ITEM_DOUBLE = 3;
	/**
	 * Indicates the verification type {@code long}.
	 */
	int ITEM_LONG = 4;
	/**
	 * Indicates the verification type {@code null}.
	 */
	int ITEM_NULL = 5;
	/**
	 * Indicates the verification type {@code uninitializedThis}.
	 */
	int ITEM_UNINITIALIZED_THIS = 6;
	/**
	 * Indicates the verification type of a class reference.
	 */
	int ITEM_OBJECT = 7;
	/**
	 * Indicates the verification type {@code uninitialized}.
	 */
	int ITEM_UNINITIALIZED = 8;
	/**
	 * The lower bound of the {@code same_frame}'s {@code frame_type}.
	 */
	int SAME_FRAME_MIN = 0;
	/**
	 * The upper bound of the {@code same_frame}'s {@code frame_type}.
	 */
	int SAME_FRAME_MAX = 63;
	/**
	 * The lower bound of the {@code same_locals_1_stack_item_frame}'s {@code frame_type}.
	 */
	int SAME_LOCALS_ONE_STACK_ITEM_MIN = 64;
	/**
	 * The upper bound of the {@code same_locals_1_stack_item_frame}'s {@code frame_type}.
	 */
	int SAME_LOCALS_ONE_STACK_ITEM_MAX = 127;
	/**
	 * The lower bound of the {@code same_locals_1_stack_item_frame_extended}'s {@code frame_type}.
	 */
	int SAME_LOCALS_ONE_STACK_ITEM_EXTENDED_MIN = 247;
	/**
	 * The upper bound of the {@code same_locals_1_stack_item_frame_extended}'s {@code frame_type}.
	 */
	int SAME_LOCALS_ONE_STACK_ITEM_EXTENDED_MAX = 247;
	/**
	 * The lower bound of the {@code chop_frame}'s {@code frame_type}.
	 */
	int CHOP_FRAME_MIN = 248;
	/**
	 * The upper bound of the {@code chop_frame}'s {@code frame_type}.
	 */
	int CHOP_FRAME_MAX = 250;
	/**
	 * The lower bound of the {@code same_frame_extended}'s {@code frame_type}.
	 */
	int SAME_FRAME_EXTENDED_MIN = 251;
	/**
	 * The upper bound of the {@code same_frame_extended}'s {@code frame_type}.
	 */
	int SAME_FRAME_EXTENDED_MAX = 251;
	/**
	 * The lower bound of the {@code append_frame}'s {@code frame_type}.
	 */
	int APPEND_FRAME_MIN = 252;
	/**
	 * The upper bound of the {@code append_frame}'s {@code frame_type}.
	 */
	int APPEND_FRAME_MAX = 254;
	/**
	 * The lower bound of the {@code full_frame}'s {@code frame_type}.
	 */
	int FULL_FRAME_MIN = 255;
	/**
	 * The upper bound of the {@code full_frame}'s {@code frame_type}.
	 */
	int FULL_FRAME_MAX = 255;
}
