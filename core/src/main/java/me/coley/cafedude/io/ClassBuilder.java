package me.coley.cafedude.io;

import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Field;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.Modifiers;
import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.constant.CpClass;

import java.util.ArrayList;
import java.util.List;

import static me.coley.cafedude.classfile.VersionConstants.JAVA1;

/**
 * Builder for a {@link ClassFile}.
 *
 * @author Matt Coley
 */
public class ClassBuilder {
	private final List<Attribute> attributes = new ArrayList<>();
	private final List<CpClass> interfaces = new ArrayList<>();
	private final List<Field> fields = new ArrayList<>();
	private final List<Method> methods = new ArrayList<>();
	private ConstPool pool = new ConstPool();
	private int versionMajor;
	private int versionMinor;
	private int access;
	private CpClass thisClass;
	private CpClass superClass;

	/**
	 * @return {@code true} when the version pattern indicates a pre-java Oak class.
	 */
	public boolean isOakVersion() {
		return (versionMajor == JAVA1 && versionMinor <= 2) || (versionMajor < JAVA1);
	}

	/**
	 * @return {@code true} when the access flags indicate the class is an annotation.
	 */
	public boolean isAnnotation() {
		return Modifiers.has(access, Modifiers.ACC_ANNOTATION);
	}

	/**
	 * @return Class's constant pool.
	 */
	public ConstPool getPool() {
		return pool;
	}

	public void setConstPool(ConstPool pool) {
		this.pool = pool;
	}

	/**
	 * @return Major version.
	 */
	public int getVersionMajor() {
		return versionMajor;
	}

	/**
	 * @param versionMajor
	 * 		Major version.
	 */
	public void setVersionMajor(int versionMajor) {
		this.versionMajor = versionMajor;
	}

	/**
	 * @return Minor version.
	 */
	public int getVersionMinor() {
		return versionMinor;
	}

	/**
	 * @param versionMinor
	 * 		Minor version.
	 */
	public void setVersionMinor(int versionMinor) {
		this.versionMinor = versionMinor;
	}

	/**
	 * @return Access flags.
	 */
	public int getAccess() {
		return access;
	}

	/**
	 * @param access
	 * 		Access flags.
	 */
	public void setAccess(int access) {
		this.access = access;
	}

	/**
	 * @param thisClass
	 * 		CP index of the current class's type.
	 */
	public void setThisClass(CpClass thisClass) {
		this.thisClass = thisClass;
	}

	/**
	 * @param superClass
	 * 		CP index of the parent super-class's type.
	 */
	public void setSuperClass(CpClass superClass) {
		this.superClass = superClass;
	}

	/**
	 * @param interfaceEntry
	 * 		CP index of an interface type for the class.
	 */
	public void addInterface(CpClass interfaceEntry) {
		interfaces.add(interfaceEntry);
	}

	/**
	 * @param field
	 * 		Field to add.
	 */
	public void addField(Field field) {
		fields.add(field);
	}

	/**
	 * @param method
	 * 		Method to add.
	 */
	public void addMethod(Method method) {
		methods.add(method);
	}

	/**
	 * @param attribute
	 * 		Attribute to add.
	 */
	public void addAttribute(Attribute attribute) {
		attributes.add(attribute);
	}

	/**
	 * @return list of interfaces.
	 */
	public List<CpClass> getInterfaces() {
		return interfaces;
	}

	/**
	 * @return list of fields.
	 */
	public List<Field> getFields() {
		return fields;
	}

	/**
	 * @return list of methods.
	 */
	public List<Method> getMethods() {
		return methods;
	}

	/**
	 * @return list of attributes.
	 */
	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @return Build it!
	 */
	public ClassFile build() throws InvalidClassException {
		if(thisClass == null)
			throw new InvalidClassException("Missing this class");
		for(CpClass iface : interfaces)
			if(iface == null)
				throw new InvalidClassException("Missing interface");
		return new ClassFile(
				versionMinor, versionMajor,
				pool,
				access,
				thisClass, superClass,
				interfaces,
				fields,
				methods,
				attributes
		);
	}
}
