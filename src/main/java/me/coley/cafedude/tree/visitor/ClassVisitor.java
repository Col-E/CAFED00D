package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.classfile.Descriptor;
import org.jetbrains.annotations.Nullable;

/**
 * Visitor for accepting class members and attributes.
 */
public interface ClassVisitor extends DeclarationVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	default ClassVisitor classDelegate() {
		return null;
	}

	@Override
	default ClassVisitor declarationDelegate() {
		return classDelegate();
	}

	/**
	 * Visit the class itself
	 * @param name
	 * 			Name of the class.
	 * @param access
	 * 			Access flags of the class.
	 * @param superName
	 * 			Name of the super class.
	 * @param interfaces
	 * 			Names of the interfaces.
	 */
	default void visitClass(String name, int access, String superName, String... interfaces) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) delegate.visitClass(name, access, superName, interfaces);
	}

	/**
	 * Visit a method in this class.
	 * @param name
	 * 			Name of the method.
	 * @param access
	 * 			Access flags of the method.
	 * @param descriptor
	 * 			Descriptor of the method. May be {@link Descriptor.Kind#ILLEGAL} if the method has
	 * 			an illegal type descriptor.
	 * @return A visitor for the method.
	 */
	@Nullable
	default MethodVisitor visitMethod(String name, int access, Descriptor descriptor) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) return delegate.visitMethod(name, access, descriptor);
		return null;
	}

	/**
	 * Visit a field in this class.
	 * @param name
	 * 			Name of the field.
	 * @param access
	 * 			Access flags of the field.
	 * @param descriptor
	 * 			Descriptor of the field. May be {@link Descriptor.Kind#ILLEGAL} if the field has
	 * 			an illegal type descriptor.
	 * @return A visitor for the field.
	 */
	@Nullable
	default FieldVisitor visitField(String name, int access, Descriptor descriptor) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) return delegate.visitField(name, access, descriptor);
		return null;
	}

	/**
	 * Visit a record component in this class.
	 *
	 * @param name
	 * 		  Name of the record component.
	 * @param descriptor
	 * 		  Descriptor of the record component. May be {@link Descriptor.Kind#ILLEGAL} if the
	 * 		  record component has an illegal type descriptor.
	 * @return A visitor for the record component.
	 */
	@Nullable
	default RecordComponentVisitor visitRecordComponent(String name, Descriptor descriptor) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) return delegate.visitRecordComponent(name, descriptor);
		return null;
	}

	/**
	 * Visit a module in this class.
	 *
	 * @param name
	 * 		  Name of the module.
	 * @param access
	 * 		  Access flags of the module.
	 * @param version
	 * 		  Version of the module.
	 * 		  {@code null} if the module has no version.
	 * @return A visitor for the module.
	 */
	@Nullable
	default ModuleVisitor visitModule(String name, int access, @Nullable String version) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) return delegate.visitModule(name, access, version);
		return null;
	}

	/**
	 * Visits the enclosing class of the class.
	 *
	 * @param owner
	 * 			Name of the enclosing class.
	 * @param name
	 * 			Name of the enclosing method.
	 * @param descriptor
	 * 			Descriptor of the enclosing method.
	 */
	default void visitOuterClass(String owner, String name, Descriptor descriptor) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) delegate.visitOuterClass(owner, name, descriptor);
	}

	/**
	 * Visit an inner class.
	 *
	 * @param name
	 * 			Name of the inner class.
	 * @param outerName
	 * 			Name of the outer class.
	 * 			{@code null} if the inner class is not a member of another class.
	 * @param innerName
	 * 			Name of the inner class inside its enclosing class.
	 * 			{@code null} if the inner class is anonymous.
	 * @param access
	 * 			Access flags of the inner class.
	 */
	default void visitInnerClass(String name, @Nullable String outerName, @Nullable String innerName, int access) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) delegate.visitInnerClass(name, outerName, innerName, access);
	}

	/**
	 * Visit a source file.
	 * @param source
	 * 			Name of the source file.
	 * 			{@code null} if the source file name is unknown.
	 * @param debug
	 * 			Debug information.
	 * 			{@code null} if the debug information is unknown.
	 */
	default void visitSource(@Nullable String source, byte @Nullable [] debug) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) delegate.visitSource(source, debug);
	}

	/**
	 * Visit the nest host class of this class.
	 *
	 * @param nestHost
	 * 			Name of the nest host class.
	 */
	default void visitNestHost(String nestHost) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) delegate.visitNestHost(nestHost);
	}

	/**
	 * Visit a nest member class of this class.
	 *
	 * @param nestMember
	 * 			Name of the nest member class.
	 */
	default void visitNestMember(String nestMember) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) delegate.visitNestMember(nestMember);
	}

	/**
	 * Visit a permitted subclass of this class.
	 *
	 * @param permittedSubclass
	 * 			Name of the permitted subclass.
	 */
	default void visitPermittedSubclass(String permittedSubclass) {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) delegate.visitPermittedSubclass(permittedSubclass);
	}

	/**
	 * End of the class.
	 */
	default void visitClassEnd() {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) delegate.visitClassEnd();
	}

}
