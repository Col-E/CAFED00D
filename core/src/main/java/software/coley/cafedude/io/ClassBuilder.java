package software.coley.cafedude.io;

import software.coley.cafedude.InvalidClassException;
import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.Field;
import software.coley.cafedude.classfile.Method;
import software.coley.cafedude.classfile.Modifiers;
import software.coley.cafedude.classfile.VersionConstants;
import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.Placeholders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
	private CpClass thisClass = Placeholders.CLASS;
	private CpClass superClass = Placeholders.CLASS;

	/**
	 * @return {@code true} when the version pattern indicates a pre-java Oak class.
	 */
	public boolean isOakVersion() {
		return (versionMajor == VersionConstants.JAVA1 && versionMinor <= 2) || (versionMajor < VersionConstants.JAVA1);
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
	@Nonnull
	public ConstPool getPool() {
		return pool;
	}

	/**
	 * @param pool
	 * 		Class's constant pool.
	 */
	public void setConstPool(@Nonnull ConstPool pool) {
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
	 * 		Constant pool entry holding the current class's type.
	 */
	public void setThisClass(@Nonnull CpClass thisClass) {
		this.thisClass = thisClass;
	}

	/**
	 * @param superClass
	 * 		Constant pool entry holding the super-type's class type.
	 */
	public void setSuperClass(@Nullable CpClass superClass) {
		this.superClass = superClass;
	}

	/**
	 * @param interfaceEntry
	 * 		Constant pool entry holding an interface type to implement.
	 */
	public void addInterface(@Nonnull CpClass interfaceEntry) {
		interfaces.add(interfaceEntry);
	}

	/**
	 * @param field
	 * 		Field to add.
	 */
	public void addField(@Nonnull Field field) {
		fields.add(field);
	}

	/**
	 * @param method
	 * 		Method to add.
	 */
	public void addMethod(@Nonnull Method method) {
		methods.add(method);
	}

	/**
	 * @param attribute
	 * 		Attribute to add.
	 */
	public void addAttribute(@Nonnull Attribute attribute) {
		attributes.add(attribute);
	}

	/**
	 * @return List of interfaces implemented.
	 */
	@Nonnull
	public List<CpClass> getInterfaces() {
		return interfaces;
	}

	/**
	 * @return List of declared fields.
	 */
	@Nonnull
	public List<Field> getFields() {
		return fields;
	}

	/**
	 * @return List of declared methods.
	 */
	@Nonnull
	public List<Method> getMethods() {
		return methods;
	}

	/**
	 * @return List of attributes.
	 */
	@Nonnull
	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @return Built class file.
	 *
	 * @throws InvalidClassException
	 * 		When the class cannot be built.
	 */
	@Nonnull
	public ClassFile build() throws InvalidClassException {
		for (CpClass iface : interfaces)
			if (iface == null)
				throw new InvalidClassException("Interface entry was null");
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
