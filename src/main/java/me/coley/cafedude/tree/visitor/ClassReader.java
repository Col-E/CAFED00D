package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.*;
import me.coley.cafedude.classfile.attribute.EnclosingMethodAttribute;
import me.coley.cafedude.classfile.attribute.InnerClassesAttribute;
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
		MemberReader memberReader = new MemberReader(classFile, transformer);
		for (Method method : classFile.getMethods()) {
			memberReader.visitMethod(visitor.visitMethod(
					pool.getUtf(method.getNameIndex()),
					method.getAccess(),
					Descriptor.from(pool.getUtf(method.getTypeIndex()))
			), method);
		}
		for (Field field : classFile.getFields()) {
			memberReader.visitField(visitor.visitField(
					pool.getUtf(field.getNameIndex()),
					field.getAccess(),
					Descriptor.from(pool.getUtf(field.getTypeIndex()))
			), field);
		}
		visitor.visitClassEnd();
	}

	private String getClassName(int classIndex) {
		CpClass cpClass = (CpClass) classFile.getCp(classIndex);
		CpUtf8 cpClassName = (CpUtf8) classFile.getCp(cpClass.getIndex());
		return cpClassName.getText();
	}
}
