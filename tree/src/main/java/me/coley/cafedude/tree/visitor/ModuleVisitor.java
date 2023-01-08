package me.coley.cafedude.tree.visitor;

import org.jetbrains.annotations.Nullable;

/**
 * Visitor for visiting module information.
 */
public interface ModuleVisitor {

	/**
	 * Return the delegate visitor for pass through implementations.
	 * @return Delegate visitor.
	 */
	@Nullable
	default ModuleVisitor moduleDelegate() {
		return null;
	}

	/**
	 * Visit a module requirement.
	 *
	 * @param module
	 * 		Name of the module.
	 * @param flags
	 * 		Access flags.
	 * @param version
	 * 		Module version.
	 * 		{@code null} if not specified.
	 */
	default void visitRequires(String module, int flags, @Nullable String version) {
		ModuleVisitor delegate = moduleDelegate();
		if(delegate != null) delegate.visitRequires(module, flags, version);
	}

	/**
	 * Visit a module export.
	 *
	 * @param exportPackage
	 * 		Exported package.
	 * @param flags
	 * 		Access flags.
	 * @param modules
	 * 		Modules to export to.
	 */
	default void visitExports(String exportPackage, int flags, String... modules) {
		ModuleVisitor delegate = moduleDelegate();
		if(delegate != null) delegate.visitExports(exportPackage, flags, modules);
	}

	/**
	 * Visit a module open.
	 *
	 * @param openPackage
	 * 		Opened package.
	 * @param flags
	 * 		Access flags.
	 * @param modules
	 * 		Modules to open to.
	 */
	default void visitOpens(String openPackage, int flags, String... modules) {
		ModuleVisitor delegate = moduleDelegate();
		if(delegate != null) delegate.visitOpens(openPackage, flags, modules);
	}

	/**
	 * Visit a module use.
	 *
	 * @param service
	 * 		Service to use.
	 */
	default void visitUses(String service) {
		ModuleVisitor delegate = moduleDelegate();
		if(delegate != null) delegate.visitUses(service);
	}

	/**
	 * Visit a module provide.
	 *
	 * @param service
	 * 		Service to provide.
	 * @param providers
	 * 		Providers of the service.
	 */
	default void visitProvides(String service, String... providers) {
		ModuleVisitor delegate = moduleDelegate();
		if(delegate != null) delegate.visitProvides(service, providers);
	}

	/**
	 * Visit a module main class.
	 *
	 * @param mainClass
	 * 		Main class.
	 */
	default void visitMainClass(String mainClass) {
		ModuleVisitor delegate = moduleDelegate();
		if(delegate != null) delegate.visitMainClass(mainClass);
	}

	/**
	 * Visit a module package.
	 *
	 * @param packageName
	 * 		Package name.
	 */
	default void visitPackage(String packageName) {
		ModuleVisitor delegate = moduleDelegate();
		if(delegate != null) delegate.visitPackage(packageName);
	}

	/**
	 * End of module.
	 */
	default void visitModuleEnd() {
		ModuleVisitor delegate = moduleDelegate();
		if(delegate != null) delegate.visitModuleEnd();
	}

}