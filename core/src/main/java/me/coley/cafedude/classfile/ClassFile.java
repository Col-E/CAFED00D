package me.coley.cafedude.classfile;

import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.behavior.AttributeHolder;
import me.coley.cafedude.classfile.behavior.CpAccessor;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.io.AttributeContext;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class file format.
 *
 * @author Matt Coley
 */
public class ClassFile implements AttributeHolder, CpAccessor {
	private final ConstPool pool;
	private List<CpClass> interfaceClasses;
	private List<Field> fields;
	private List<Method> methods;
	private List<Attribute> attributes;
	private int access;
	private int versionMinor;
	private int versionMajor;
	private CpClass thisClass;
	private CpClass superClass;

	/**
	 * @param versionMinor
	 * 		Class minor version.
	 * @param versionMajor
	 * 		Class major version.
	 * @param pool
	 * 		Pool entries.
	 * @param access
	 * 		Class access flags.
	 * @param thisClass
	 * 		Constant pool entry for this class.
	 * @param superClass
	 * 		Constant pool entry for parent class.
	 * @param interfaceClasses
	 * 		Entries for interfaces.
	 * @param fields
	 * 		Fields.
	 * @param methods
	 * 		Methods.
	 * @param attributes
	 * 		Attributes.
	 */
	public ClassFile(int versionMinor, int versionMajor,
					 ConstPool pool, int access, CpClass thisClass, CpClass superClass,
					 List<CpClass> interfaceClasses, List<Field> fields, List<Method> methods,
					 List<Attribute> attributes) {
		this.versionMinor = versionMinor;
		this.versionMajor = versionMajor;
		this.pool = pool;
		this.access = access;
		this.thisClass = thisClass;
		this.superClass = superClass;
		this.interfaceClasses = interfaceClasses;
		this.fields = fields;
		this.methods = methods;
		this.attributes = attributes;
	}

	/**
	 * @return Class name.
	 */
	public String getName() {
		return thisClass.getName().getText();
	}

	/**
	 * @return Parent class name.
	 */
	public String getSuperName() {
		return superClass.getName().getText();
	}

	/**
	 * @param index
	 * 		CP index, which is indexed starting at {@code 1}.
	 *
	 * @return Constant pool value at index.
	 */
	public CpEntry getCp(int index) {
		return pool.get(index);
	}

	/**
	 * @param index
	 * 		CP index, which is indexed starting at {@code 1}.
	 * @param entry
	 * 		New constant pool value at index.
	 */
	public void setCp(int index, CpEntry entry) {
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
	public List<CpClass> getInterfaceClasses() {
		return interfaceClasses;
	}

	/**
	 * @param interfaceClasses
	 * 		New indices in pool for interfaces.
	 */
	public void setInterfaceClasses(List<CpClass> interfaceClasses) {
		this.interfaceClasses = interfaceClasses;
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
	public CpClass getThisClass() {
		return thisClass;
	}

	/**
	 * @param thisClass
	 * 		Index in pool for the current class.
	 */
	public void setThisClass(CpClass thisClass) {
		this.thisClass = thisClass;
	}

	/**
	 * @return Index in pool for the super class.
	 */
	public CpClass getSuperClass() {
		return superClass;
	}

	/**
	 * @param superClass
	 * 		Index in pool for the super class.
	 */
	public void setSuperClass(CpClass superClass) {
		this.superClass = superClass;
	}

	@Nonnull
	@Override
	public List<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(@Nonnull List<Attribute> attributes) {
		this.attributes = attributes;
	}

	@Nonnull
	@Override
	public AttributeContext getHolderType() {
		return AttributeContext.CLASS;
	}

	/**
	 * Get an attribute by class
	 *
	 * @param <T> The type of attribute to search for.
	 * @param type The type of attribute to search for.
	 * @return The attribute, or null if not found.
	 */
	public <T extends Attribute> T getAttribute(Class<T> type) {
		for (Attribute attribute : attributes) {
			if (type.isInstance(attribute)) {
				return type.cast(attribute);
			}
		}
		return null;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = new HashSet<>();
		set.add(getThisClass());
		set.add(getSuperClass());
		set.addAll(getInterfaceClasses());
		for (Attribute attribute : getAttributes())
			set.addAll(attribute.cpAccesses());
		for (ClassMember field : getFields())
			set.addAll(field.cpAccesses());
		for (ClassMember method : getMethods())
			set.addAll(method.cpAccesses());
		return set;
	}
}
