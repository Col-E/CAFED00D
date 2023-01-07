package me.coley.cafedude.classfile.attribute;

import java.util.Set;

/**
 * Enclosing method attribute
 *
 * @author JCWasmx86
 */
public class EnclosingMethodAttribute extends Attribute {
	private int classIndex;
	private int methodIndex;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param classIndex
	 * 		Index into the constant pool representing the innermost class that encloses
	 * 		the declaration of the current class.
	 * @param methodIndex
	 * 		Used for anonymous classes e.g. in a method or constructor. If not, it is
	 * 		zero.
	 */
	public EnclosingMethodAttribute(int nameIndex, int classIndex, int methodIndex) {
		super(nameIndex);
		this.classIndex = classIndex;
		this.methodIndex = methodIndex;
	}

	/**
	 * @return Class index of the enclosing class.
	 */
	public int getClassIndex() {
		return classIndex;
	}

	/**
	 * @return Index of the enclosing method.
	 */
	public int getMethodIndex() {
		return methodIndex;
	}

	/**
	 * @param classIndex
	 * 		Set the enclosing class index.
	 */
	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}

	/**
	 * @param methodIndex
	 * 		Set the enclosing method index.
	 */
	public void setMethodIndex(int methodIndex) {
		this.methodIndex = methodIndex;
	}

	@Override
	public Set<Integer> cpAccesses() {
		Set<Integer> set = super.cpAccesses();
		set.add(getClassIndex());
		set.add(getMethodIndex());
		return set;
	}

	@Override
	public int computeInternalLength() {
		return 4;
	}
}
