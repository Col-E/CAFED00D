package software.coley.cafedude.tree.visitor.writer;

import software.coley.cafedude.classfile.AttributeConstants;
import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.attribute.ModuleAttribute;
import software.coley.cafedude.classfile.attribute.ModuleMainClassAttribute;
import software.coley.cafedude.classfile.attribute.ModulePackagesAttribute;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpModule;
import software.coley.cafedude.classfile.constant.CpPackage;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.tree.visitor.ModuleVisitor;
import software.coley.cafedude.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static software.coley.cafedude.classfile.attribute.ModuleAttribute.*;

/**
 * Module visitor implementation for writing back to {@link ModulePackagesAttribute} / {@link ModuleAttribute}.
 *
 * @author Justus Garbe
 */
public class ModuleWriter implements ModuleVisitor {
	private final CpModule name;
	private final int flags;
	private final CpUtf8 version;
	private final Symbols symbols;
	private final List<Exports> exports = new ArrayList<>();
	private final List<Opens> opens = new ArrayList<>();
	private final List<Provides> provides = new ArrayList<>();
	private final List<Requires> requires = new ArrayList<>();
	private final List<CpClass> uses = new ArrayList<>();
	private final List<CpPackage> modulePackages = new ArrayList<>();
	private final List<Attribute> attributes = new ArrayList<>();
	private final Consumer<List<Attribute>> callback;

	ModuleWriter(Symbols symbols, CpModule name, int access, CpUtf8 version,
						Consumer<List<Attribute>> callback) {
		this.symbols = symbols;
		this.callback = callback;
		this.name = name;
		this.flags = access;
		this.version = version;
	}

	@Override
	public void visitExports(@Nonnull String exportPackage, int flags, String... modules) {
		CpPackage packageEntry = symbols.newPackage(exportPackage);
		List<CpModule> moduleIndexes = new ArrayList<>();
		for (String module : modules) {
			moduleIndexes.add(symbols.newModule(module));
		}
		exports.add(new Exports(packageEntry, flags, moduleIndexes));
	}

	@Override
	public void visitOpens(@Nonnull String openPackage, int flags, String... modules) {
		CpPackage packageEntry = symbols.newPackage(openPackage);
		List<CpModule> moduleIndexes = new ArrayList<>();
		for (String module : modules) {
			moduleIndexes.add(symbols.newModule(module));
		}
		opens.add(new Opens(packageEntry, flags, moduleIndexes));
	}

	@Override
	public void visitProvides(@Nonnull String service, String... providers) {
		CpClass serviceEntry = symbols.newClass(service);
		List<CpClass> providerIndexes = new ArrayList<>();
		for (String provider : providers) {
			providerIndexes.add(symbols.newClass(provider));
		}
		provides.add(new Provides(serviceEntry, providerIndexes));
	}

	@Override
	public void visitUses(@Nonnull String service) {
		uses.add(symbols.newClass(service));
	}

	@Override
	public void visitRequires(@Nonnull String module, int flags, @Nullable String version) {
		CpModule moduleRef = symbols.newModule(module);
		CpUtf8 versionRef = Optional.orNull(module, symbols::newUtf8);
		requires.add(new Requires(moduleRef, flags, versionRef));
	}

	@Override
	public void visitMainClass(@Nonnull String mainClass) {
		attributes.add(new ModuleMainClassAttribute(
				symbols.newUtf8(AttributeConstants.MODULE_MAIN_CLASS),
				symbols.newClass(mainClass)));
	}

	@Override
	public void visitPackage(@Nonnull String packageName) {
		modulePackages.add(symbols.newPackage(packageName));
	}

	@Override
	public void visitModuleEnd() {
		attributes.add(new ModulePackagesAttribute(
				symbols.newUtf8(AttributeConstants.MODULE_PACKAGES),
				modulePackages));
		attributes.add(new ModuleAttribute(
				symbols.newUtf8(AttributeConstants.MODULE),
				name,
				flags,
				version,
				requires, exports, opens, uses, provides));
		callback.accept(attributes);
	}
}
