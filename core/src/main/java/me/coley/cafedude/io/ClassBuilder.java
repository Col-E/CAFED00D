package me.coley.cafedude.io;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Field;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.Modifiers;
import me.coley.cafedude.classfile.attribute.Attribute;

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
	private final List<Integer> interfaces = new ArrayList<>();
	private final List<Field> fields = new ArrayList<>();
	private final List<Method> methods = new ArrayList<>();
	private ConstPool pool = new ConstPool();
	private int versionMajor;
	private int versionMinor;
	private int access;
	private int classIndex;
	private int superIndex;

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
	 * @param classIndex
	 * 		CP index of the current class's type.
	 */
	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}

	/**
	 * @param superIndex
	 * 		CP index of the parent super-class's type.
	 */
	public void setSuperIndex(int superIndex) {
		this.superIndex = superIndex;
	}

	/**
	 * @param interfaceIndex
	 * 		CP index of an interface type for the class.
	 */
	public void addInterface(int interfaceIndex) {
		interfaces.add(interfaceIndex);
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
	public List<Integer> getInterfaces() {
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
	public ClassFile build() {
		return new ClassFile(
				versionMinor, versionMajor,
				pool,
				access,
				classIndex, superIndex,
				interfaces,
				fields,
				methods,
				attributes
		);
	}
}