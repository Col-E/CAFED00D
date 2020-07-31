package me.coley.cafedude;

import me.coley.cafedude.attribute.Attribute;
import me.coley.cafedude.constant.ConstPoolEntry;
import me.coley.cafedude.constant.CpClass;
import me.coley.cafedude.constant.CpUtf8;

import java.util.List;

/**
 * Class file format.
 *
 * @author Matt Coley
 */
public class ClassFile {
	private final ConstPool pool;
	private List<Integer> interfaceIndices;
	private List<Field> fields;
	private List<Method> methods;
	private List<Attribute> attributes;
	private int access;
	private int versionMinor;
	private int versionMajor;
	private int classIndex;
	private int superIndex;

	/**
	 * @param versionMinor
	 * 		Class minor version.
	 * @param versionMajor
	 * 		Class major version.
	 * @param pool
	 * 		Pool entries.
	 * @param access
	 * 		Class access flags.
	 * @param classIndex
	 * 		Index in pool for the current class.
	 * @param superIndex
	 * 		Index in pool for the super class.
	 * @param interfaceIndices
	 * 		Indices in pool for interfaces.
	 * @param fields
	 * 		Fields.
	 * @param methods
	 * 		Methods.
	 * @param attributes
	 * 		Attributes.
	 */
	public ClassFile(int versionMinor, int versionMajor,
					 ConstPool pool, int access, int classIndex, int superIndex,
					 List<Integer> interfaceIndices, List<Field> fields, List<Method> methods,
					 List<Attribute> attributes) {
		this.versionMinor = versionMinor;
		this.versionMajor = versionMajor;
		this.pool = pool;
		this.access = access;
		this.classIndex = classIndex;
		this.superIndex = superIndex;
		this.interfaceIndices = interfaceIndices;
		this.fields = fields;
		this.methods = methods;
		this.attributes = attributes;
	}

	/**
	 * @return Class name.
	 */
	public String getName() {
		return getClassName(classIndex);
	}

	/**
	 * @return Parent class name.
	 */
	public String getSuperName() {
		return getClassName(superIndex);
	}

	/**
	 * @param classIndex
	 * 		CP index pointing to a class.
	 *
	 * @return Name of class.
	 */
	private String getClassName(int classIndex) {
		CpClass cpClass = (CpClass) getCp(classIndex);
		CpUtf8 cpClassName = (CpUtf8) getCp(cpClass.getIndex());
		return cpClassName.getText();
	}

	/**
	 * @param index
	 * 		CP index, which is indexed starting at {@code 1}.
	 *
	 * @return Constant pool value at index.
	 */
	public ConstPoolEntry getCp(int index) {
		return pool.get(index);
	}

	/**
	 * @param index
	 * 		CP index, which is indexed starting at {@code 1}.
	 * @param entry
	 * 		New constant pool value at index.
	 */
	public void setCp(int index, ConstPoolEntry entry) {
		pool.set(index, entry);
	}

	/**
	 * @return Pool entries.
	 */
	public ConstPool getPool() {
		return pool;
	}

	/**
	 * @return Indices in pool for interfaces.
	 */
	public List<Integer> getInterfaceIndices() {
		return interfaceIndices;
	}

	/**
	 * @param interfaceIndices
	 * 		New indices in pool for interfaces.
	 */
	public void setInterfaceIndices(List<Integer> interfaceIndices) {
		this.interfaceIndices = interfaceIndices;
	}

	/**
	 * @return Fields.
	 */
	public List<Field> getFields() {
		return fields;
	}

	/**
	 * @param fields
	 * 		New list of fields.
	 */
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	/**
	 * @return Methods.
	 */
	public List<Method> getMethods() {
		return methods;
	}

	/**
	 * @param methods
	 * 		New list of methods.
	 */
	public void setMethods(List<Method> methods) {
		this.methods = methods;
	}

	/**
	 * @return Attributes.
	 */
	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 * 		New list of attributes.
	 */
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return Class access flags.
	 */
	public int getAccess() {
		return access;
	}

	/**
	 * @param access
	 * 		Class access flags.
	 */
	public void setAccess(int access) {
		this.access = access;
	}

	/**
	 * @return Class minor version.
	 */
	public int getVersionMinor() {
		return versionMinor;
	}

	/**
	 * @param versionMinor
	 * 		Class minor version.
	 */
	public void setVersionMinor(int versionMinor) {
		this.versionMinor = versionMinor;
	}

	/**
	 * @return Class major version.
	 */
	public int getVersionMajor() {
		return versionMajor;
	}

	/**
	 * @param versionMajor
	 * 		Class major version.
	 */
	public void setVersionMajor(int versionMajor) {
		this.versionMajor = versionMajor;
	}

	/**
	 * @return Index in pool for the current class.
	 */
	public int getClassIndex() {
		return classIndex;
	}

	/**
	 * @param classIndex
	 * 		Index in pool for the current class.
	 */
	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}

	/**
	 * @return Index in pool for the super class.
	 */
	public int getSuperIndex() {
		return superIndex;
	}

	/**
	 * @param superIndex
	 * 		Index in pool for the super class.
	 */
	public void setSuperIndex(int superIndex) {
		this.superIndex = superIndex;
	}
}
