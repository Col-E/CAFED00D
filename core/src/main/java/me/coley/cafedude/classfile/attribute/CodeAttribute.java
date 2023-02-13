package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.AttributeHolder;
import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.classfile.instruction.Instruction;
import me.coley.cafedude.io.AttributeContext;
import me.coley.cafedude.io.InstructionWriter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Method code attribute.
 *
 * @author Matt Coley
 */
public class CodeAttribute extends Attribute implements AttributeHolder {
	private List<ExceptionTableEntry> exceptionTable;
	private List<Attribute> attributes;
	private List<Instruction> instructions;
	private int maxStack;
	private int maxLocals;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param maxStack
	 * 		Maximum number of values on the stack in the method.
	 * @param maxLocals
	 * 		Maximum number of local variables used in the method.
	 * @param instructions
	 * 		Instruction code data.
	 * @param exceptionTable
	 * 		Exception table entries.
	 * @param attributes
	 * 		List of other attributes.
	 */
	public CodeAttribute(CpUtf8 name, int maxStack, int maxLocals, List<Instruction> instructions,
						 List<ExceptionTableEntry> exceptionTable, List<Attribute> attributes) {
		super(name);
		this.maxStack = maxStack;
		this.maxLocals = maxLocals;
		this.instructions = instructions;
		this.exceptionTable = exceptionTable;
		this.attributes = attributes;
	}

	/**
	 * @return Instruction code data.
	 */
	public List<Instruction> getInstructions() {
		return instructions;
	}

	/**
	 * @param instructions
	 * 		New instruction code data.
	 */
	public void setInstructions(List<Instruction> instructions) {
		this.instructions = instructions;
	}

	/**
	 * @return Maximum number of values on the stack in the method.
	 */
	public int getMaxStack() {
		return maxStack;
	}

	/**
	 * @param maxStack
	 * 		New maximum number of values on the stack in the method.
	 */
	public void setMaxStack(int maxStack) {
		this.maxStack = maxStack;
	}

	/**
	 * @return Maximum number of local variables used in the method.
	 */
	public int getMaxLocals() {
		return maxLocals;
	}

	/**
	 * @param maxLocals
	 * 		New maximum number of local variables used in the method.
	 */
	public void setMaxLocals(int maxLocals) {
		this.maxLocals = maxLocals;
	}

	/**
	 * @return Exception table entries.
	 */
	public List<ExceptionTableEntry> getExceptionTable() {
		return exceptionTable;
	}

	/**
	 * @param exceptionTable
	 * 		New exception table entries.
	 */
	public void setExceptionTable(List<ExceptionTableEntry> exceptionTable) {
		this.exceptionTable = exceptionTable;
	}

	@Override
	public List<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public <T extends Attribute> @Nullable T getAttribute(Class<T> type) {
		for (Attribute attribute : attributes) {
			if (type.isInstance(attribute))
				return type.cast(attribute);
		}
		return null;
	}

	@Override
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public AttributeContext getHolderType() {
		return AttributeContext.ATTRIBUTE;
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		for (Attribute attribute : getAttributes())
			set.addAll(attribute.cpAccesses());
		for (ExceptionTableEntry ex : getExceptionTable())
			set.addAll(ex.cpAccesses());
		for(Instruction instruction : instructions) {
			if(instruction instanceof CpAccessor) {
				set.addAll(((CpAccessor) instruction).cpAccesses());
			}
		}
		return set;
	}

	@Override
	public int computeInternalLength() {
		// u2: max_stack
		// u2: max_locals
		int len = 4;
		// u4: code_length
		// u1 * X: CODE
		len += 4;
		int insnSize = 0;
		for (Instruction instruction : instructions)
			insnSize += instruction.computeSize();
		len += insnSize;
		// u2: exception_table_length
		// u2 * 4 * X: EXCEPTIONS
		len += 2;
		len += 8 * exceptionTable.size();
		// u2: attributes_count
		// ??: ATTRIBS
		len += 2;
		for (Attribute attribute : attributes)
			len += attribute.computeCompleteLength();
		return len;
	}

	/**
	 * Exception table entry representation.
	 *
	 * @author Matt Coley
	 */
	public static class ExceptionTableEntry implements CpAccessor {
		private int startPc;
		private int endPc;
		private int handlerPc;
		private CpClass catchType;

		/**
		 * @param startPc
		 * 		Instruction offset for start of try-catch range.
		 * @param endPc
		 * 		Instruction offset for end of try-catch range.
		 * @param handlerPc
		 * 		Instruction offset for start of catch handler range.
		 * @param catchType
		 * 		Index in constant pool of class type to catch.
		 */
		public ExceptionTableEntry(int startPc, int endPc, int handlerPc, CpClass catchType) {
			this.startPc = startPc;
			this.endPc = endPc;
			this.handlerPc = handlerPc;
			this.catchType = catchType;
		}

		/**
		 * @return Instruction offset for start of try-catch range.
		 */
		public int getStartPc() {
			return startPc;
		}

		/**
		 * @param startPc
		 * 		New instruction offset for start of try-catch range.
		 */
		public void setStartPc(int startPc) {
			this.startPc = startPc;
		}

		/**
		 * @return Instruction offset for end of try-catch range.
		 */
		public int getEndPc() {
			return endPc;
		}

		/**
		 * @param endPc
		 * 		New instruction offset for end of try-catch range.
		 */
		public void setEndPc(int endPc) {
			this.endPc = endPc;
		}

		/**
		 * @return Instruction offset for start of catch handler range.
		 */
		public int getHandlerPc() {
			return handlerPc;
		}

		/**
		 * @param handlerPc
		 * 		New instruction offset for start of catch handler range.
		 */
		public void setHandlerPc(int handlerPc) {
			this.handlerPc = handlerPc;
		}

		/**
		 * @return Index in constant pool of class type to catch.
		 */
		public CpClass getCatchType() {
			return catchType;
		}

		/**
		 * @param catchType
		 * 		Index in constant pool of class type to catch.
		 */
		public void setCatchType(CpClass catchType) {
			this.catchType = catchType;
		}

		@Override
		public Set<CpEntry> cpAccesses() {
			return Collections.singleton(getCatchType());
		}
	}
}
