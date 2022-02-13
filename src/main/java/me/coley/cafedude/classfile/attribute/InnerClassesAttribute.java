package me.coley.cafedude.classfile.attribute;

import java.util.List;

/**
 * Attribute describing the inner classes of a class.
 *
 * @author JCWasmx86
 */
public class InnerClassesAttribute extends Attribute {
	private List<InnerClass> innerClasses;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param classes
	 * 		All inner classes.
	 */
	public InnerClassesAttribute(int nameIndex, List<InnerClass> classes) {
		super(nameIndex);
		this.innerClasses = classes;
	}

	/**
	 * @return The inner classes of this class.
	 */
	public List<InnerClass> getInnerClasses() {
		return innerClasses;
	}

	/**
	 * @param innerClasses
	 * 		The new inner classes of this class.
	 */
	public void setInnerClasses(List<InnerClass> innerClasses) {
		this.innerClasses = innerClasses;
	}

	@Override
	public int computeInternalLength() {
		return 2 + this.innerClasses.size() * 8;
	}

	/**
	 * An inner class.
	 *
	 * @author JCWasmx86
	 */
	public static class InnerClass {
		private int innerClassInfoIndex;
		private int outerClassInfoIndex;
		private int innerNameIndex;
		private int innerClassAccessFlags;

		/**
		 * @param innerClassInfoIndex
		 * 		Index into the constant pool describing this inner class.
		 * @param outerClassInfoIndex
		 * 		Index into the constant pool describing the outer class. 0 if this
		 * 		is a local or anonymous class.
		 * @param innerNameIndex
		 * 		Index into the constant pool. At this index, the name of this inner class
		 * 		will be specified. 0 if this class is anonymous.
		 * @param innerClassAccessFlags
		 * 		Access flags of the inner class.
		 */
		public InnerClass(int innerClassInfoIndex, int outerClassInfoIndex, int innerNameIndex,
						  int innerClassAccessFlags) {
			this.innerClassInfoIndex = innerClassInfoIndex;
			this.outerClassInfoIndex = outerClassInfoIndex;
			this.innerNameIndex = innerNameIndex;
			this.innerClassAccessFlags = innerClassAccessFlags;
		}

		/**
		 * @return Index into the constant pool describing this inner class.
		 */
		public int getInnerClassInfoIndex() {
			return innerClassInfoIndex;
		}

		/**
		 * @param innerClassInfoIndex
		 * 		New index into the constant pool describing this inner class.
		 */
		public void setInnerClassInfoIndex(int innerClassInfoIndex) {
			this.innerClassInfoIndex = innerClassInfoIndex;
		}

		/**
		 * @return Index into the constant pool describing the outer class. 0 if this
		 * is a local or anonymous class.
		 */
		public int getOuterClassInfoIndex() {
			return outerClassInfoIndex;
		}

		/**
		 * @param outerClassInfoIndex
		 * 		New index into the constant pool describing the outer class. 0 if this
		 * 		is a local or anonymous class.
		 */
		public void setOuterClassInfoIndex(int outerClassInfoIndex) {
			this.outerClassInfoIndex = outerClassInfoIndex;
		}

		/**
		 * @return Index into the constant pool. At this index, the name of this inner class
		 * will be specified. 0 if this class is anonymous.
		 */
		public int getInnerNameIndex() {
			return innerNameIndex;
		}

		/**
		 * @param innerNameIndex
		 * 		New index into the constant pool. At this index, the name of this inner class
		 * 		will be specified. 0 if this class is anonymous.
		 */
		public void setInnerNameIndex(int innerNameIndex) {
			this.innerNameIndex = innerNameIndex;
		}

		/**
		 * @return Access flags of the inner class.
		 */
		public int getInnerClassAccessFlags() {
			return innerClassAccessFlags;
		}

		/**
		 * @param innerClassAccessFlags
		 * 		Access flags of the inner class.
		 */
		public void setInnerClassAccessFlags(int innerClassAccessFlags) {
			this.innerClassAccessFlags = innerClassAccessFlags;
		}
	}
}
