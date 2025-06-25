package software.coley.cafedude.classfile.attribute;

import software.coley.cafedude.classfile.behavior.CpAccessor;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Attribute describing the inner classes of a class.
 *
 * @author JCWasmx86
 */
public non-sealed class InnerClassesAttribute extends Attribute {
	private List<InnerClass> innerClasses;

	/**
	 * @param name
	 * 		Constant pool entry holding the attribute name.
	 * @param classes
	 * 		All inner classes.
	 */
	public InnerClassesAttribute(@Nonnull CpUtf8 name, @Nonnull List<InnerClass> classes) {
		super(name);
		this.innerClasses = classes;
	}

	/**
	 * @return The inner classes of this class.
	 */
	@Nonnull
	public List<InnerClass> getInnerClasses() {
		return innerClasses;
	}

	/**
	 * @param innerClasses
	 * 		The new inner classes of this class.
	 */
	public void setInnerClasses(@Nonnull List<InnerClass> innerClasses) {
		this.innerClasses = innerClasses;
	}

	@Nonnull
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
		 * 		Constant pool entry holding the type of this inner class.
		 * @param outerClassInfo
		 * 		Constant pool entry holding the type of the outer class.
		 * 		<br>
		 *        {@code null} if this is a local or anonymous class.
		 * @param innerName
		 * 		Constant pool entry holding the type of the name of this inner class.
		 * 		<br>
		 *        {@code null} if this class is anonymous.
		 * @param innerClassAccessFlags
		 * 		Access flags of the inner class.
		 */
		public InnerClass(@Nonnull CpClass innerClassInfo, @Nullable CpClass outerClassInfo, @Nullable CpUtf8 innerName,
		                  int innerClassAccessFlags) {
			this.innerClassInfo = innerClassInfo;
			this.outerClassInfo = outerClassInfo;
			this.innerName = innerName;
			this.innerClassAccessFlags = innerClassAccessFlags;
		}

		/**
		 * @return Constant pool entry holding the type of this inner class.
		 */
		@Nonnull
		public CpClass getInnerClassInfo() {
			return innerClassInfo;
		}

		/**
		 * @param innerClassInfo
		 * 		New constant pool entry holding the type of this inner class.
		 */
		public void setInnerClassInfo(@Nonnull CpClass innerClassInfo) {
			this.innerClassInfo = innerClassInfo;
		}

		/**
		 * @return Constant pool entry holding the type of the outer class.
		 * <br>
		 * {@code null} if this is a local or anonymous class.
		 */
		@Nullable
		public CpClass getOuterClassInfo() {
			return outerClassInfo;
		}

		/**
		 * @param outerClassInfo
		 * 		Constant pool entry holding the type of the outer class.
		 * 		<br>
		 *        {@code null} if this is a local or anonymous class.
		 */
		public void setOuterClassInfo(@Nullable CpClass outerClassInfo) {
			this.outerClassInfo = outerClassInfo;
		}

		/**
		 * @return Constant pool entry holding the type of the name of this inner class.
		 * <br>
		 * {@code null} if this class is anonymous.
		 */
		@Nullable
		public CpUtf8 getInnerName() {
			return innerName;
		}

		/**
		 * @param innerName
		 * 		Constant pool entry holding the type of the name of this inner class.
		 * 		<br>
		 *        {@code null} if this class is anonymous.
		 */
		public void setInnerName(@Nullable CpUtf8 innerName) {
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

		@Nonnull
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
