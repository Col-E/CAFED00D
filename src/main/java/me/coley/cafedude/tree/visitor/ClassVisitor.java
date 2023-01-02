package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.classfile.Descriptor;
import org.jetbrains.annotations.Nullable;

/**
 * Visitor for accepting class members and attributes.
 */
public interface ClassVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	default ClassVisitor classDelegate() {
		return null;
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
	 * End of the class.
	 */
	default void visitClassEnd() {
		ClassVisitor delegate = classDelegate();
		if(delegate != null) delegate.visitClassEnd();
	}

}
