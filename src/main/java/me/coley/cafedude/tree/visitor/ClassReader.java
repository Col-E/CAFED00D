package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.*;
import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.attribute.CodeAttribute;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.transform.LabelTransformer;

import java.util.List;
import java.util.Optional;

/**
 * Class to read a {@link ClassFile} into a {@link ClassVisitor}.
 * @author Justus Garbe
 */
public class ClassReader {

	private final ClassFile classFile;
	private LabelTransformer transformer;

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
		transformer = new LabelTransformer(classFile);
		transformer.transform();
		ConstPool pool = classFile.getPool();
		List<Integer> interfaces = classFile.getInterfaceIndices();
		String[] interfaceNames = new String[interfaces.size()];
		for (int i = 0; i < interfaces.size(); i++) {
			interfaceNames[i] = getClassName(interfaces.get(i));
		}
		visitor.visitClass(classFile.getName(), classFile.getAccess(), classFile.getSuperName(), interfaceNames);
		for (Method method : classFile.getMethods()) {
			String name = pool.getUtf(method.getNameIndex());
			String descriptor = pool.getUtf(method.getTypeIndex());
			Descriptor desc = Descriptor.from(descriptor);
			MethodVisitor mv = visitor.visitMethod(name, method.getAccess(), desc);
			if(mv == null) continue; // method skipped
			accept(mv.visitCode(), method);
			mv.visitMethodEnd();
		}
		for (Field field : classFile.getFields()) {
			String name = pool.getUtf(field.getNameIndex());
			String descriptor = pool.getUtf(field.getTypeIndex());
			Descriptor desc = Descriptor.from(descriptor);
			FieldVisitor fv = visitor.visitField(name, field.getAccess(), desc);
			if(fv == null) continue; // field skipped
			fv.visitFieldEnd();
		}
		visitor.visitClassEnd();
	}

	private void accept(CodeVisitor cv, Method method) {
		if(cv == null) return; // skip code
		CodeAttribute code = method.getAttribute(CodeAttribute.class).orElse(null);
		if(code == null) return; // skip code
		InstructionVisitor ir = new InstructionVisitor(classFile, code, cv, method,
				transformer.getLabels(method), transformer.getInstructions(method));
		ir. accept();
	}

	private String getClassName(int classIndex) {
		CpClass cpClass = (CpClass) classFile.getCp(classIndex);
		CpUtf8 cpClassName = (CpUtf8) classFile.getCp(cpClass.getIndex());
		return cpClassName.getText();
	}
}
