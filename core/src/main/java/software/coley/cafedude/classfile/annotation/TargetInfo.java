package software.coley.cafedude.classfile.annotation;

import software.coley.cafedude.classfile.attribute.ExceptionsAttribute;
import software.coley.cafedude.classfile.attribute.LocalVariableTableAttribute;
import software.coley.cafedude.classfile.behavior.CpAccessor;
import software.coley.cafedude.classfile.constant.CpEntry;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Indicates which type in a declaration or expression is annotated.
 *
 * @author Matt Coley
 */
public abstract class TargetInfo implements CpAccessor {
	private final TargetInfoType targetTypeKind;
	private final int targetType;

	/**
	 * @param targetTypeKind
	 * 		Info type, indicating the union layout. Abstraction of {@link #targetType}.
	 * @param targetType
	 * 		Target type, the {@code target_type} of a {@code type_annotation}
	 */
	protected TargetInfo(@Nonnull TargetInfoType targetTypeKind, int targetType) {
		this.targetTypeKind = targetTypeKind;
		this.targetType = targetType;
	}

	/**
	 * @return Info type, indicating the union layout. Abstraction of {@link #targetType}.
	 */
	@Nonnull
	public TargetInfoType getTargetTypeKind() {
		return targetTypeKind;
	}

	/**
	 * @return Target type, the {@code target_type} of a {@code type_annotation}
	 */
	public int getTargetType() {
		return targetType;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		return Collections.emptySet();
	}

	/**
	 * @return Length of info.
	 */
	public abstract int computeLength();

	/**
	 * Indicates the annotation appears on the n'th type parameter of a generic type or member.
	 */
	public static class TypeParameterTargetInfo extends TargetInfo {
		private final int typeParameterIndex;

		/**
		 * @param targetType
		 * 		Value of {@code target_type} of the enclosing {@code type_annotation},
		 * 		indicating the purpose of the {@code target_info}.
		 * @param typeParameterIndex
		 * 		Index of the type parameter annotated.
		 */
		public TypeParameterTargetInfo(int targetType, int typeParameterIndex) {
			super(TargetInfoType.TYPE_PARAMETER_TARGET, targetType);
			this.typeParameterIndex = typeParameterIndex;
		}

		/**
		 * @return Index of the type parameter annotated.
		 */
		public int getTypeParameterIndex() {
			return typeParameterIndex;
		}

		@Override
		public int computeLength() {
			return 1; // type_parameter_index
		}
	}

	/**
	 * Indicates the annotation appears on a type in the {@code extends} or {@code implements} clause
	 * of a class or interface declaration.
	 */
	public static class SuperTypeTargetInfo extends TargetInfo {
		/**
		 * Case for the super-type index to appear on the extends clause.
		 */
		public static final int EXTENDS = 65535;
		private final int superTypeIndex;

		/**
		 * @param targetType
		 * 		Value of {@code target_type} of the enclosing {@code type_annotation},
		 * 		indicating the purpose of the {@code target_info}.
		 * @param superTypeIndex
		 * 		For {@code extends} index is 65535.
		 * 		Otherwise, the index indicates the interface index of the associated class.
		 */
		public SuperTypeTargetInfo(int targetType, int superTypeIndex) {
			super(TargetInfoType.SUPERTYPE_TARGET, targetType);
			this.superTypeIndex = superTypeIndex;
		}

		/**
		 * @return {@code} true when {@link #getSuperTypeIndex()} is 65535.
		 */
		public boolean isExtends() {
			return superTypeIndex == EXTENDS;
		}

		/**
		 * @return For {@code extends} index is 65535.
		 * Otherwise the index indicates the interface index of the associated class.
		 */
		public int getSuperTypeIndex() {
			return superTypeIndex;
		}

		@Override
		public int computeLength() {
			return 2; // supertype_index
		}
	}

	/**
	 * Indicates the annotation appears on the n'th bound of the j'th parameter of a generic type or member.
	 */
	public static class TypeParameterBoundTargetInfo extends TargetInfo {
		private final int typeParameterIndex;
		private final int boundIndex;

		/**
		 * @param targetType
		 * 		Value of {@code target_type} of the enclosing {@code type_annotation},
		 * 		indicating the purpose of the {@code target_info}.
		 * @param typeParameterIndex
		 * 		Index of type parameter declaration.
		 * @param boundIndex
		 * 		Index of the bound of the parameter.
		 */
		public TypeParameterBoundTargetInfo(int targetType, int typeParameterIndex, int boundIndex) {
			super(TargetInfoType.TYPE_PARAMETER_BOUND_TARGET, targetType);
			this.typeParameterIndex = typeParameterIndex;
			this.boundIndex = boundIndex;
		}

		/**
		 * @return Index of type parameter declaration
		 */
		public int getTypeParameterIndex() {
			return typeParameterIndex;
		}

		/**
		 * @return Index of the bound of the parameter.
		 */
		public int getBoundIndex() {
			return boundIndex;
		}

		@Override
		public int computeLength() {
			return 2; // type_parameter_index + bound_index
		}
	}

	/**
	 * Indicates that an annotation appears on either the type in a field declaration,
	 * the return type of a method, the type of a newly constructed object,
	 * or the receiver type of a method or constructor.
	 */
	public static class EmptyTargetInfo extends TargetInfo {
		/**
		 * @param targetType
		 * 		Value of {@code target_type} of the enclosing {@code type_annotation},
		 * 		indicating the purpose of the {@code target_info}.
		 */
		public EmptyTargetInfo(int targetType) {
			super(TargetInfoType.EMPTY_TARGET, targetType);
		}

		@Override
		public int computeLength() {
			return 0; // nothing
		}
	}

	/**
	 * Indicates that an annotation appears on the type in a formal parameter declaration of
	 * a method, constructor, or lambda expression.
	 */
	public static class FormalParameterTargetInfo extends TargetInfo {
		private final int formalParameterIndex;

		/**
		 * @param targetType
		 * 		Value of {@code target_type} of the enclosing {@code type_annotation},
		 * 		indicating the purpose of the {@code target_info}.
		 * @param formalParameterIndex
		 * 		Index of the formal parameter.
		 */
		public FormalParameterTargetInfo(int targetType, int formalParameterIndex) {
			super(TargetInfoType.FORMAL_PARAMETER_TARGET, targetType);
			this.formalParameterIndex = formalParameterIndex;
		}

		/**
		 * This is technically not a one-to-one mapping to the method descriptor's parameters according to the specs.
		 * It does not give a concrete example, but instead to refer to a similar case for parameter annotations.
		 *
		 * @return Index of the formal parameter.
		 */
		public int getFormalParameterIndex() {
			return formalParameterIndex;
		}

		@Override
		public int computeLength() {
			return 1; // formal_parameter_index
		}
	}

	/**
	 * Indicates that an annotation appears on the n'th type in the {@code throws} clause
	 * of a method or constructor declaration.
	 */
	public static class ThrowsTargetInfo extends TargetInfo {
		private final int throwsTypeIndex;

		/**
		 * @param targetType
		 * 		Value of {@code target_type} of the enclosing {@code type_annotation},
		 * 		indicating the purpose of the {@code target_info}.
		 * @param throwsTypeIndex
		 * 		Index of the thrown type in the associated {@code exception_index_table}
		 * 		of the {@link ExceptionsAttribute}.
		 */
		public ThrowsTargetInfo(int targetType, int throwsTypeIndex) {
			super(TargetInfoType.THROWS_TARGET, targetType);
			this.throwsTypeIndex = throwsTypeIndex;
		}

		/**
		 * @return Index of the thrown type in the associated {@code exception_index_table}
		 * of the {@code Exceptions} attribute.
		 */
		public int getThrowsTypeIndex() {
			return throwsTypeIndex;
		}

		@Override
		public int computeLength() {
			return 2; // throws_type_index
		}
	}


	/**
	 * Indicates that an annotation appears on the type of a local variable.
	 * <br>
	 * Marked variables types are annotated but are not listed directly.
	 * The information provided should be matched with what appears in the {@link LocalVariableTableAttribute}.
	 */
	public static class LocalVarTargetInfo extends TargetInfo {
		private final List<Variable> variableTable;

		/**
		 * @param targetType
		 * 		Value of {@code target_type} of the enclosing {@code type_annotation},
		 * 		indicating the purpose of the {@code target_info}.
		 * @param variableTable
		 * 		The minimal local variable table of values annotated.
		 */
		public LocalVarTargetInfo(int targetType, List<Variable> variableTable) {
			super(TargetInfoType.LOCALVAR_TARGET, targetType);
			this.variableTable = variableTable;
		}

		/**
		 * @return The minimal local variable table of values annotated.
		 */
		public List<Variable> getVariableTable() {
			return variableTable;
		}

		@Override
		public int computeLength() {
			return 2 + (6 * variableTable.size()); // u2: table_length + (u6 * varCount)
		}

		/**
		 * Minimal local variable outline for {@link LocalVarTargetInfo}.
		 */
		public static class Variable {
			private final int startPc;
			private final int length;
			private final int index;

			/**
			 * @param startPc
			 * 		Initial offset in the code attribute the variable starts at.
			 * @param length
			 * 		Duration in the code attribute the variable persists for.
			 * @param index
			 * 		Index of the variable in the current frame's local variable array.
			 */
			public Variable(int startPc, int length, int index) {
				this.startPc = startPc;
				this.length = length;
				this.index = index;
			}

			/**
			 * @return Initial offset in the code attribute the variable starts at.
			 */
			public int getStartPc() {
				return startPc;
			}

			/**
			 * @return Duration in the code attribute the variable persists for.
			 */
			public int getLength() {
				return length;
			}

			/**
			 * @return Index of the variable in the current frame's local variable array.
			 */
			public int getIndex() {
				return index;
			}
		}
	}

	/**
	 * Indicates that an annotation appears on the n'th type in an exception parameter declaration.
	 */
	public static class CatchTargetInfo extends TargetInfo {
		private final int exceptionTableIndex;

		/**
		 * @param targetType
		 * 		Value of {@code target_type} of the enclosing {@code type_annotation},
		 * 		indicating the purpose of the {@code target_info}.
		 * @param exceptionTableIndex
		 * 		Index of exception parameter type.
		 */
		public CatchTargetInfo(int targetType, int exceptionTableIndex) {
			super(TargetInfoType.CATCH_TARGET, targetType);
			this.exceptionTableIndex = exceptionTableIndex;
		}

		/**
		 * @return Index of exception parameter type.
		 */
		public int getExceptionTableIndex() {
			return exceptionTableIndex;
		}

		@Override
		public int computeLength() {
			return 2; // exception_table_index
		}
	}

	/**
	 * Indicates that an annotation appears on either the type in an {@code instanceof} expression or
	 * a {@code new} expression, or the type before the :: in a method reference expression.
	 */
	public static class OffsetTargetInfo extends TargetInfo {
		private final int offset;

		/**
		 * @param targetType
		 * 		Value of {@code target_type} of the enclosing {@code type_annotation},
		 * 		indicating the purpose of the {@code target_info}.
		 * @param offset
		 * 		Offset in the code attribute byte array of the annotated type instruction.
		 */
		public OffsetTargetInfo(int targetType, int offset) {
			super(TargetInfoType.OFFSET_TARGET, targetType);
			this.offset = offset;
		}

		/**
		 * @return Offset in the code attribute byte array of the annotated type instruction.
		 */
		public int getOffset() {
			return offset;
		}

		@Override
		public int computeLength() {
			return 2; // offset
		}
	}

	/**
	 * Indicates that an annotation appears either on the n'th type in a cast expression,
	 * or on the n'th type argument in the explicit type argument list for any of the following:
	 * <ul>
	 *     <li>A {@code new} expression</li>
	 *     <li>An explicit constructor invocation statement</li>
	 *     <li>A method invocation expression</li>
	 *     <li>A method reference expression</li>
	 * </ul>
	 */
	public static class TypeArgumentTargetInfo extends TargetInfo {
		private final int offset;
		private final int typeArgumentIndex;

		/**
		 * @param targetType
		 * 		Value of {@code target_type} of the enclosing {@code type_annotation},
		 * 		indicating the purpose of the {@code target_info}.
		 * @param offset
		 * 		Offset in the code attribute byte array of the annotated type instruction.
		 * @param typeArgumentIndex
		 * 		Index of the type in the cast operator that is annotated.
		 */
		public TypeArgumentTargetInfo(int targetType, int offset, int typeArgumentIndex) {
			super(TargetInfoType.TYPE_ARGUMENT_TARGET, targetType);
			this.offset = offset;
			this.typeArgumentIndex = typeArgumentIndex;
		}

		/**
		 * @return Offset in the code attribute byte array of the annotated type instruction.
		 */
		public int getOffset() {
			return offset;
		}

		/**
		 * @return Index of the type in the cast operator that is annotated.
		 */
		public int getTypeArgumentIndex() {
			return typeArgumentIndex;
		}

		@Override
		public int computeLength() {
			return 3; // offset + type_argument_index
		}
	}
}
