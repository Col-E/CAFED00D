package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.io.ClassBuilder;
import me.coley.cafedude.io.ClassFileWriter;
import me.coley.cafedude.tree.visitor.ClassVisitor;
import me.coley.cafedude.tree.visitor.FieldVisitor;
import me.coley.cafedude.tree.visitor.MethodVisitor;

public class ClassWriter extends DeclarationWriter implements ClassVisitor {

	final ClassBuilder builder;

	public ClassWriter(int versionMajor, int versionMinor) {
		super(new Symbols(new ConstPool()));
		this.builder = new ClassBuilder();
		// set version minor and major
		builder.setVersionMajor(versionMajor);
		builder.setVersionMinor(versionMinor);
	}

	@Override
	public void visitClass(String name, int access, String superName, String... interfaces) {
		builder.setClassIndex(symbols.newUtf8(name));
		builder.setAccess(access);
		builder.setSuperIndex(symbols.newUtf8(superName));
		for (String anInterface : interfaces) {
			builder.addInterface(symbols.newUtf8(anInterface));
		}
	}

	@Override
	public MethodVisitor visitMethod(String name, int access, Descriptor descriptor) {
		return new MethodWriter(symbols, access, symbols.newUtf8(name), symbols.newUtf8(descriptor.getDescriptor()),
				builder::addMethod);
	}

	@Override
	public FieldVisitor visitField(String name, int access, Descriptor descriptor) {
		return new FieldWriter(symbols, access, symbols.newUtf8(name), symbols.newUtf8(descriptor.getDescriptor()),
				builder::addField);
	}

	@Override
	public void visitClassEnd() {
		super.visitDeclarationEnd();
	}

	public byte[] toByteArray() throws InvalidClassException {
		builder.setConstPool(symbols.pool);
		ClassFile file = builder.build();
		ClassFileWriter writer = new ClassFileWriter();
		return writer.write(file);
	}
}
