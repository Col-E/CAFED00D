package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.AttributeConstants;
import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.attribute.ModuleAttribute;
import me.coley.cafedude.classfile.attribute.ModuleMainClassAttribute;
import me.coley.cafedude.classfile.attribute.ModulePackagesAttribute;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpModule;
import me.coley.cafedude.classfile.constant.CpPackage;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.tree.visitor.ModuleVisitor;
import me.coley.cafedude.util.Optional;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static me.coley.cafedude.classfile.attribute.ModuleAttribute.*;

public class ModuleWriter implements ModuleVisitor {

	private CpModule name;
	private int flags;
	private CpUtf8 version;
	private Symbols symbols;
	private List<Exports> exports = new ArrayList<>();
	private List<Opens> opens = new ArrayList<>();
	private List<Provides> provides = new ArrayList<>();
	private List<Requires> requires = new ArrayList<>();
	private List<CpClass> uses = new ArrayList<>();
	private List<CpPackage> modulePackages = new ArrayList<>();
	private List<Attribute> attributes = new ArrayList<>();
	private Consumer<List<Attribute>> callback;

	public ModuleWriter(Symbols symbols, CpModule name, int access, CpUtf8 version,
						Consumer<List<Attribute>> callback) {
		this.symbols = symbols;
		this.callback = callback;
		this.name = name;
		this.flags = access;
		this.version = version;
	}

	@Override
	public void visitExports(String exportPackage, int flags, String... modules) {
		CpPackage packageEntry = symbols.newPackage(exportPackage);
		List<CpModule> moduleIndexes = new ArrayList<>();
		for (String module : modules) {
			moduleIndexes.add(symbols.newModule(module));
		}
		exports.add(new Exports(packageEntry, flags, moduleIndexes));
	}

	@Override
	public void visitOpens(String openPackage, int flags, String... modules) {
		CpPackage packageEntry = symbols.newPackage(openPackage);
		List<CpModule> moduleIndexes = new ArrayList<>();
		for (String module : modules) {
			moduleIndexes.add(symbols.newModule(module));
		}
		opens.add(new Opens(packageEntry, flags, moduleIndexes));
	}

	@Override
	public void visitProvides(String service, String... providers) {
		CpClass serviceEntry = symbols.newClass(service);
		List<CpClass> providerIndexes = new ArrayList<>();
		for (String provider : providers) {
			providerIndexes.add(symbols.newClass(provider));
		}
		provides.add(new Provides(serviceEntry, providerIndexes));
	}

	@Override
	public void visitUses(String service) {
		uses.add(symbols.newClass(service));
	}

	@Override
	public void visitRequires(String module, int flags, @Nullable String version) {
		CpModule moduleRef = symbols.newModule(module);
		CpUtf8 versionRef = Optional.orNull(module, symbols::newUtf8);
		requires.add(new Requires(moduleRef, flags, versionRef));
	}

	@Override
	public void visitMainClass(String mainClass) {
		attributes.add(new ModuleMainClassAttribute(
				symbols.newUtf8(AttributeConstants.MODULE_MAIN_CLASS),
				symbols.newClass(mainClass)));
	}

	@Override
	public void visitPackage(String packageName) {
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
