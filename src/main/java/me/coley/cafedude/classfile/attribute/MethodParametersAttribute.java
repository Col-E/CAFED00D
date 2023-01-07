package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodParametersAttribute extends Attribute {

	private List<Parameter> parameters;

	/**
	 * @param nameIndex Name index in constant pool.
	 * @param parameters Parameters.
	 */
	public MethodParametersAttribute(int nameIndex, List<Parameter> parameters) {
		super(nameIndex);
		this.parameters = parameters;
	}

	/**
	 * @return Parameters.
	 */
	public List<Parameter> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters New parameters.
	 */
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public int computeInternalLength() {
		// U1: parameterCount
		// (U2: nameIndex, U2: accessFlags) * parameterCount
		return 1 + (parameters.size() * 4);
	}

	public static class Parameter implements CpAccessor {

		private int accessFlags;
		private int nameIndex;

		/**
		 * @param accessFlags Access flags.
		 * @param nameIndex Name index in constant pool.
		 */
		public Parameter(int accessFlags, int nameIndex) {
			this.accessFlags = accessFlags;
			this.nameIndex = nameIndex;
		}

		/**
		 * @return Access flags.
		 */
		public int getAccessFlags() {
			return accessFlags;
		}

		/**
		 * @param accessFlags New access flags.
		 */
		public void setAccessFlags(int accessFlags) {
			this.accessFlags = accessFlags;
		}

		/**
		 * @return Name index in constant pool.
		 */
		public int getNameIndex() {
			return nameIndex;
		}

		/**
		 * @param nameIndex New name index in constant pool.
		 */
		public void setNameIndex(int nameIndex) {
			this.nameIndex = nameIndex;
		}

		@Override
		public Set<Integer> cpAccesses() {
			Set<Integer> set = new HashSet<>();
			set.add(nameIndex);
			return set;
		}

	}

}
