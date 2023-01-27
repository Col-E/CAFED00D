package me.coley.cafedude.tree.visitor.reader;

import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.*;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.constant.*;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.transform.LabelTransformer;
import me.coley.cafedude.tree.visitor.ClassVisitor;
import me.coley.cafedude.tree.visitor.ModuleVisitor;
import me.coley.cafedude.tree.visitor.RecordComponentVisitor;
import me.coley.cafedude.util.Optional;

import java.util.List;

/**
 * Class to read a {@link ClassFile} into a {@link ClassVisitor}.
 * @author Justus Garbe
 */
public class ClassReader {

	private final ClassFile classFile;

	/**
	 * Construct a class reader using a byte array which will be read.
	 *
	 * @param bytes
	 * 			Byte array of the class file.
	 * @throws InvalidClassException if the class bytes are invalid
	 */
	public ClassReader(byte[] bytes) throws InvalidClassException {
		ClassFileReader reader = new ClassFileReader();
		this.classFile = reader.read(bytes);
	}

	/**
	 * Create a new class reader using an existing class file.
	 *
	 * @param file
	 * 			Class file.
	 */
	public ClassReader(ClassFile file) {
		this.classFile = file;
	}

	/**
	 * Accept a class visitor to be visited using this class file.
	 *
	 * @param visitor
	 * 			Visitor to accept.
	 */
	public void accept(ClassVisitor visitor) {
		LabelTransformer transformer = new LabelTransformer(classFile);
		transformer.transform();
		List<CpClass> interfaces = classFile.getInterfaceClasses();
		String[] interfaceNames = new String[interfaces.size()];
		// convert interface indices to names
		for (int i = 0; i < interfaces.size(); i++) {
			interfaceNames[i] = interfaces.get(i).getName().getText();
		}
		// visit class
		visitor.visitClass(classFile.getName(), classFile.getAccess(), classFile.getSuperName(), interfaceNames);
		// visit annotations, signature and deprecated
		MemberReader.visitDeclaration(visitor, classFile);
		// outer class
		EnclosingMethodAttribute enclosingMethod = classFile.getAttribute(EnclosingMethodAttribute.class);
		if (enclosingMethod != null) {
			String owner = enclosingMethod.getClassEntry().getName().getText();
			String name = null;
			Descriptor desc = null;
			if(enclosingMethod.getMethodEntry() != null) {
				CpNameType nameType = enclosingMethod.getMethodEntry();
				name = nameType.getName().getText();
				desc = Descriptor.from(nameType.getType().getText());
			}
			visitor.visitOuterClass(owner, name, desc);
		}
		// inner classes
		InnerClassesAttribute innerClasses = classFile.getAttribute(InnerClassesAttribute.class);
		if (innerClasses != null) {
			for (InnerClassesAttribute.InnerClass innerClass : innerClasses.getInnerClasses()) {
				// inner name must be given
				String innerName = innerClass.getInnerClassInfo().getName().getText();
				// outer name only for non-anonymous classes
				String outerName = Optional.orNull(innerClass.getOuterClassInfo(), t -> t.getName().getText());
				// inner simple name only for non-anonymous classes
				String innerSimpleName = Optional.orNull(innerClass.getInnerName(), CpUtf8::getText);
				visitor.visitInnerClass(innerName, outerName, innerSimpleName, innerClass.getInnerClassAccessFlags());
			}
		}
		// source and debug
		SourceFileAttribute sourceFile = classFile.getAttribute(SourceFileAttribute.class);
		SourceDebugExtensionAttribute sourceDebug = classFile.getAttribute(SourceDebugExtensionAttribute.class);
		String source = Optional.orNull(sourceFile, t -> t.getSourceFilename().getText());
		byte[] debug = Optional.orNull(sourceDebug, SourceDebugExtensionAttribute::getDebugExtension);
		visitor.visitSource(source, debug);
		// nests
		NestHostAttribute nestHost = classFile.getAttribute(NestHostAttribute.class);
		NestMembersAttribute nestMembers = classFile.getAttribute(NestMembersAttribute.class);
		if (nestHost != null) {
			visitor.visitNestHost(nestHost.getHostClass().getName().getText());
		}
		if (nestMembers != null) {
			for (CpClass member : nestMembers.getMemberClasses()) {
				visitor.visitNestMember(member.getName().getText());
			}
		}
		// permitted subclasses
		PermittedClassesAttribute permittedClasses = classFile.getAttribute(PermittedClassesAttribute.class);
		if (permittedClasses != null) {
			for (CpClass permitted : permittedClasses.getClasses()) {
				visitor.visitPermittedSubclass(permitted.getName().getText());
			}
		}
		// read records
		RecordAttribute record = classFile.getAttribute(RecordAttribute.class);
		if (record != null) {
			for (RecordAttribute.RecordComponent component : record.getComponents()) {
				String name = component.getName().getText();
				String type = component.getDesc().getText();
				RecordComponentVisitor rcv = visitor.visitRecordComponent(name, Descriptor.from(type));
				if(rcv == null) continue;
				MemberReader.visitDeclaration(rcv, component);
				rcv.visitRecordComponentEnd();
			}
		}
		// read module
		ModuleAttribute module = classFile.getAttribute(ModuleAttribute.class);
		if(module != null) {
			String name = module.getName().getText();
			int flags = module.getFlags();
			String version = Optional.orNull(module.getVersion(), CpUtf8::getText);
			ModuleVisitor mv = visitor.visitModule(name, flags, version);
			if(mv != null) {
				visitModule(mv, module);
				mv.visitModuleEnd();
			}
		}
		// read members
		MemberReader memberReader = new MemberReader(classFile, transformer);
		// read methods
		for (Method method : classFile.getMethods()) {
			memberReader.visitMethod(visitor.visitMethod(
					method.getName().getText(),
					method.getAccess(),
					Descriptor.from(method.getType().getText())
			), method);
		}
		// read fields
		for (Field field : classFile.getFields()) {
			memberReader.visitField(visitor.visitField(
					field.getName().getText(),
					field.getAccess(),
					Descriptor.from(field.getType().getText())
			), field);
		}
		visitor.visitClassEnd();
	}

	private void visitModule(ModuleVisitor visitor, ModuleAttribute module) {
		for (ModuleAttribute.Requires require : module.getRequires()) {
			String name = require.getModule().getName().getText();
			int flags = require.getFlags();
			visitor.visitRequires(name, flags, Optional.orNull(require.getVersion(), CpUtf8::getText));
		}
		for (ModuleAttribute.Exports export : module.getExports()) {
			String name = export.getPackageEntry().getPackageName().getText();
			int flags = export.getFlags();
			String[] targets = new String[export.getTo().size()];
			int i = 0;
			for (CpModule to : export.getTo()) {
				 targets[i] = to.getName().getText();
				 i++;
			}
			visitor.visitExports(name, flags, targets);
		}
		for (ModuleAttribute.Opens open : module.getOpens()) {
			String name = open.getPackageEntry().getPackageName().getText();
			int flags = open.getFlags();
			String[] targets = new String[open.getTo().size()];
			int i = 0;
			for (CpModule to : open.getTo()) {
				targets[i] = to.getName().getText();
				i++;
			}
			visitor.visitOpens(name, flags, targets);
		}
		for (CpClass use : module.getUses()) {
			visitor.visitUses(use.getName().getText());
		}
		for (ModuleAttribute.Provides provide : module.getProvides()) {
			String name = provide.getModule().getName().getText();
			String[] targets = new String[provide.getWith().size()];
			int i = 0;
			for (CpClass with : provide.getWith()) {
				targets[i] = with.getName().getText();
				i++;
			}
			visitor.visitProvides(name, targets);
		}
		ModuleMainClassAttribute mainClass = classFile.getAttribute(ModuleMainClassAttribute.class);
		if (mainClass != null) {
			visitor.visitMainClass(mainClass.getMainClass().getName().getText());
		}
		ModulePackagesAttribute packages = classFile.getAttribute(ModulePackagesAttribute.class);
		if (packages != null) {
			for (CpPackage pkg : packages.getPackages()) {
				visitor.visitPackage(pkg.getPackageName().getText());
			}
		}
	}

	private String getClassName(int classIndex) {
		CpClass cpClass = (CpClass) classFile.getCp(classIndex);
		CpUtf8 cpClassName = (CpUtf8) classFile.getCp(cpClass.getIndex());
		return cpClassName.getText();
	}
}
