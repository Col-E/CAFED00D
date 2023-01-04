package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.Field;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.constant.CpClass;
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
		for (int i = 0; i < interfaces.size(); i++) {
			interfaceNames[i] = getClassName(interfaces.get(i));
		}
		visitor.visitClass(classFile.getName(), classFile.getAccess(), classFile.getSuperName(), interfaceNames);
		for (Method method : classFile.getMethods()) {
			new MemberReader(method, classFile, visitor, transformer).visitMethod();
		}
		for (Field field : classFile.getFields()) {
			new MemberReader(field, classFile, visitor, transformer).visitField();
		}
		visitor.visitClassEnd();
	}

	private String getClassName(int classIndex) {
		CpClass cpClass = (CpClass) classFile.getCp(classIndex);
		CpUtf8 cpClassName = (CpUtf8) classFile.getCp(cpClass.getIndex());
		return cpClassName.getText();
	}
}
