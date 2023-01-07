package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.*;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpNameType;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.transform.LabelTransformer;

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
		List<Integer> interfaces = classFile.getInterfaceIndices();
		String[] interfaceNames = new String[interfaces.size()];
		// convert interface indices to names
		for (int i = 0; i < interfaces.size(); i++) {
			interfaceNames[i] = getClassName(interfaces.get(i));
		}
		ConstPool pool = classFile.getPool();
		// visit class
		visitor.visitClass(classFile.getName(), classFile.getAccess(), classFile.getSuperName(), interfaceNames);
		// visit annotations, signature and deprecated
		MemberReader.visitDeclaration(visitor, classFile, pool);
		// outer class
		EnclosingMethodAttribute enclosingMethod = classFile.getAttribute(EnclosingMethodAttribute.class);
		if (enclosingMethod != null) {
			CpNameType nameType = (CpNameType) pool.get(enclosingMethod.getMethodIndex());
			String owner = getClassName(enclosingMethod.getClassIndex());
			String name = pool.getUtf(nameType.getNameIndex());
			String type = pool.getUtf(nameType.getTypeIndex());
			visitor.visitOuterClass(owner, name, Descriptor.from(type));
		}
		// inner classes
		InnerClassesAttribute innerClasses = classFile.getAttribute(InnerClassesAttribute.class);
		if (innerClasses != null) {
			for (InnerClassesAttribute.InnerClass innerClass : innerClasses.getInnerClasses()) {
				// inner name must be given
				String innerName = getClassName(innerClass.getInnerClassInfoIndex());
				// outer name only for non-anonymous classes
				String outerName = innerClass.getOuterClassInfoIndex() == 0 ?
						null : getClassName(innerClass.getOuterClassInfoIndex());
				// inner simple name only for non-anonymous classes
				String innerSimpleName = innerClass.getInnerNameIndex() == 0 ?
						null : pool.getUtf(innerClass.getInnerNameIndex());
				visitor.visitInnerClass(innerName, outerName, innerSimpleName, innerClass.getInnerClassAccessFlags());
			}
		}
		// source and debug
		SourceFileAttribute sourceFile = classFile.getAttribute(SourceFileAttribute.class);
		SourceDebugExtensionAttribute sourceDebug = classFile.getAttribute(SourceDebugExtensionAttribute.class);
		String source = sourceFile == null ? null : pool.getUtf(sourceFile.getSourceFileNameIndex());
		byte[] debug = sourceDebug == null ? null : sourceDebug.getDebugExtension();
		visitor.visitSource(source, debug);
		// nests
		NestHostAttribute nestHost = classFile.getAttribute(NestHostAttribute.class);
		NestMembersAttribute nestMembers = classFile.getAttribute(NestMembersAttribute.class);
		if (nestHost != null) {
			visitor.visitNestHost(getClassName(nestHost.getHostClassIndex()));
		}
		if (nestMembers != null) {
			for (int member : nestMembers.getMemberClassIndices()) {
				visitor.visitNestMember(getClassName(member));
			}
		}
		// permitted subclasses
		PermittedClassesAttribute permittedClasses = classFile.getAttribute(PermittedClassesAttribute.class);
		if (permittedClasses != null) {
			for (int permitted : permittedClasses.getClasses()) {
				visitor.visitPermittedSubclass(getClassName(permitted));
			}
		}
		// read records
		RecordAttribute record = classFile.getAttribute(RecordAttribute.class);
		if (record != null) {
			for (RecordAttribute.RecordComponent component : record.getComponents()) {
				String name = pool.getUtf(component.getNameIndex());
				String type = pool.getUtf(component.getDescIndex());
				RecordComponentVisitor rcv = visitor.visitRecordComponent(name, Descriptor.from(type));
				if(rcv == null) continue;
				MemberReader.visitDeclaration(rcv, component, pool);
				rcv.visitRecordComponentEnd();
			}
		}
		// read module
		ModuleAttribute module = classFile.getAttribute(ModuleAttribute.class);
		if(module != null) {
			String name = pool.getUtf(module.getNameIndex());
			int flags = module.getFlags();
			String version = module.getVersionIndex() == 0 ? null : pool.getUtf(module.getVersionIndex());
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
					pool.getUtf(method.getNameIndex()),
					method.getAccess(),
					Descriptor.from(pool.getUtf(method.getTypeIndex()))
			), method);
		}
		// read fields
		for (Field field : classFile.getFields()) {
			memberReader.visitField(visitor.visitField(
					pool.getUtf(field.getNameIndex()),
					field.getAccess(),
					Descriptor.from(pool.getUtf(field.getTypeIndex()))
			), field);
		}
		visitor.visitClassEnd();
	}

	private void visitModule(ModuleVisitor visitor, ModuleAttribute module) {
		ConstPool pool = classFile.getPool();
		for (ModuleAttribute.Requires require : module.getRequires()) {
			String name = pool.getUtf(require.getIndex());
			int flags = require.getFlags();
			String version = require.getVersionIndex() == 0 ? null : pool.getUtf(require.getVersionIndex());
			visitor.visitRequires(name, flags, version);
		}
		for (ModuleAttribute.Exports export : module.getExports()) {
			String name = pool.getUtf(export.getIndex());
			int flags = export.getFlags();
			String[] targets = new String[export.getToIndices().size()];
			for (int i = 0; i < targets.length; i++) {
				targets[i] = pool.getUtf(export.getToIndices().get(i));
			}
			visitor.visitExports(name, flags, targets);
		}
		for (ModuleAttribute.Opens open : module.getOpens()) {
			String name = pool.getUtf(open.getIndex());
			int flags = open.getFlags();
			String[] targets = new String[open.getToIndices().size()];
			for (int i = 0; i < targets.length; i++) {
				targets[i] = pool.getUtf(open.getToIndices().get(i));
			}
			visitor.visitOpens(name, flags, targets);
		}
		for (int uses : module.getUses()) {
			visitor.visitUses(pool.getUtf(uses));
		}
		for (ModuleAttribute.Provides provide : module.getProvides()) {
			String name = pool.getUtf(provide.getIndex());
			String[] targets = new String[provide.getWithIndices().size()];
			for (int i = 0; i < targets.length; i++) {
				targets[i] = pool.getUtf(provide.getWithIndices().get(i));
			}
			visitor.visitProvides(name, targets);
		}
		ModuleMainClassAttribute mainClass = classFile.getAttribute(ModuleMainClassAttribute.class);
		if (mainClass != null) {
			visitor.visitMainClass(getClassName(mainClass.getMainClassIndex()));
		}
		ModulePackagesAttribute packages = classFile.getAttribute(ModulePackagesAttribute.class);
		if (packages != null) {
			for (int pkg : packages.getPackageIndexes()) {
				visitor.visitPackage(pool.getUtf(pkg));
			}
		}
	}

	private String getClassName(int classIndex) {
		CpClass cpClass = (CpClass) classFile.getCp(classIndex);
		CpUtf8 cpClassName = (CpUtf8) classFile.getCp(cpClass.getIndex());
		return cpClassName.getText();
	}
}
