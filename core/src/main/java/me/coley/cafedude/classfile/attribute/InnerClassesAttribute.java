package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Attribute describing the inner classes of a class.
 *
 * @author JCWasmx86
 */
public class InnerClassesAttribute extends Attribute {
	private List<InnerClass> innerClasses;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param classes
	 * 		All inner classes.
	 */
	public InnerClassesAttribute(CpUtf8 name, List<InnerClass> classes) {
		super(name);
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
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
		for (InnerClass inner : getInnerClasses())
			set.addAll(inner.cpAccesses());
		return set;
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
	public static class InnerClass implements CpAccessor {
		private CpClass innerClassInfo;
		private CpClass outerClassInfo;
		private CpUtf8 innerName;
		private int innerClassAccessFlags;

		/**
		 * @param innerClassInfo
		 * 		Index into the constant pool describing this inner class.
		 * @param outerClassInfo
		 * 		Index into the constant pool describing the outer class. 0 if this
		 * 		is a local or anonymous class.
		 * @param innerName
		 * 		Index into the constant pool. At this index, the name of this inner class
		 * 		will be specified. 0 if this class is anonymous.
		 * @param innerClassAccessFlags
		 * 		Access flags of the inner class.
		 */
		public InnerClass(CpClass innerClassInfo, CpClass outerClassInfo, CpUtf8 innerName,
						  int innerClassAccessFlags) {
			this.innerClassInfo = innerClassInfo;
			this.outerClassInfo = outerClassInfo;
			this.innerName = innerName;
			this.innerClassAccessFlags = innerClassAccessFlags;
		}

		/**
		 * @return Index into the constant pool describing this inner class.
		 */
		public CpClass getInnerClassInfo() {
			return innerClassInfo;
		}

		/**
		 * @param innerClassInfo
		 * 		New index into the constant pool describing this inner class.
		 */
		public void setInnerClassInfo(CpClass innerClassInfo) {
			this.innerClassInfo = innerClassInfo;
		}

		/**
		 * @return Index into the constant pool describing the outer class. 0 if this
		 * is a local or anonymous class.
		 */
		public CpClass getOuterClassInfo() {
			return outerClassInfo;
		}

		/**
		 * @param outerClassInfo
		 * 		New index into the constant pool describing the outer class. 0 if this
		 * 		is a local or anonymous class.
		 */
		public void setOuterClassInfo(CpClass outerClassInfo) {
			this.outerClassInfo = outerClassInfo;
		}

		/**
		 * @return Index into the constant pool. At this index, the name of this inner class
		 * will be specified. 0 if this class is anonymous.
		 */
		public CpUtf8 getInnerName() {
			return innerName;
		}

		/**
		 * @param innerName
		 * 		New index into the constant pool. At this index, the name of this inner class
		 * 		will be specified. 0 if this class is anonymous.
		 */
		public void setInnerName(CpUtf8 innerName) {
			this.innerName = innerName;
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

		@Override
		public Set<CpEntry> cpAccesses() {
			Set<CpEntry> set = new HashSet<>();
			set.add(getOuterClassInfo());
			set.add(getInnerClassInfo());
			set.add(getInnerName());
			return set;
		}
	}
}
