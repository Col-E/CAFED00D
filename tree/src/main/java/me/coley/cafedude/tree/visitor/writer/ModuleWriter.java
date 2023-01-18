package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.AttributeConstants;
import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.attribute.ModuleAttribute;
import me.coley.cafedude.classfile.attribute.ModuleMainClassAttribute;
import me.coley.cafedude.classfile.attribute.ModulePackagesAttribute;
import me.coley.cafedude.tree.visitor.ModuleVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static me.coley.cafedude.classfile.attribute.ModuleAttribute.*;

public class ModuleWriter implements ModuleVisitor {

	private int nameIndex;
	private int flags;
	private int versionIndex;
	private Symbols symbols;
	private List<Exports> exports = new ArrayList<>();
	private List<Opens> opens = new ArrayList<>();
	private List<Provides> provides = new ArrayList<>();
	private List<Requires> requires = new ArrayList<>();
	private List<Integer> uses = new ArrayList<>();
	private List<Integer> modulePackages = new ArrayList<>();
	private List<Attribute> attributes = new ArrayList<>();
	private Consumer<List<Attribute>> callback;

	public ModuleWriter(Symbols symbols, int nameIndex, int access, int versionIndex,
						Consumer<List<Attribute>> callback) {
		this.symbols = symbols;
		this.callback = callback;
		this.nameIndex = nameIndex;
		this.flags = access;
		this.versionIndex = versionIndex;
	}

	@Override
	public void visitExports(String exportPackage, int flags, String... modules) {
		int packageIndex = symbols.newPackage(exportPackage);
		List<Integer> moduleIndexes = new ArrayList<>();
		for (String module : modules) {
			moduleIndexes.add(symbols.newModule(module));
		}
		exports.add(new Exports(packageIndex, flags, moduleIndexes));
	}

	@Override
	public void visitOpens(String openPackage, int flags, String... modules) {
		int packageIndex = symbols.newPackage(openPackage);
		List<Integer> moduleIndexes = new ArrayList<>();
		for (String module : modules) {
			moduleIndexes.add(symbols.newModule(module));
		}
		opens.add(new Opens(packageIndex, flags, moduleIndexes));
	}

	@Override
	public void visitProvides(String service, String... providers) {
		int serviceIndex = symbols.newClass(service);
		List<Integer> providerIndexes = new ArrayList<>();
		for (String provider : providers) {
			providerIndexes.add(symbols.newClass(provider));
		}
		provides.add(new Provides(serviceIndex, providerIndexes));
	}

	@Override
	public void visitUses(String service) {
		int serviceIndex = symbols.newClass(service);
		uses.add(serviceIndex);
	}

	@Override
	public void visitRequires(String module, int flags, String version) {
		int moduleIndex = symbols.newModule(module);
		int versionIndex = symbols.newUtf8(version);
		requires.add(new Requires(moduleIndex, flags, versionIndex));
	}

	@Override
	public void visitMainClass(String mainClass) {
		int mainClassIndex = symbols.newClass(mainClass);
		attributes.add(new ModuleMainClassAttribute(
				symbols.newUtf8(AttributeConstants.MODULE_MAIN_CLASS),
				mainClassIndex));
	}

	@Override
	public void visitPackage(String packageName) {
		int packageIndex = symbols.newPackage(packageName);
		modulePackages.add(packageIndex);
	}

	@Override
	public void visitModuleEnd() {
		attributes.add(new ModulePackagesAttribute(
				symbols.newUtf8(AttributeConstants.MODULE_PACKAGES),
				modulePackages));
		attributes.add(new ModuleAttribute(
				symbols.newUtf8(AttributeConstants.MODULE),
				nameIndex,
				flags,
				versionIndex,
				requires, exports, opens, uses, provides));
		callback.accept(attributes);
	}
}
