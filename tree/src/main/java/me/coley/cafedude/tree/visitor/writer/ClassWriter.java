package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.InvalidClassException;
import me.coley.cafedude.classfile.AttributeConstants;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.io.ClassBuilder;
import me.coley.cafedude.io.ClassFileWriter;
import me.coley.cafedude.tree.visitor.ClassVisitor;
import me.coley.cafedude.tree.visitor.FieldVisitor;
import me.coley.cafedude.tree.visitor.MethodVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.coley.cafedude.classfile.attribute.InnerClassesAttribute.InnerClass;

public class ClassWriter extends DeclarationWriter implements ClassVisitor {

	final ClassBuilder builder;
	private final List<InnerClass> innerClasses = new ArrayList<>();

	public ClassWriter(int versionMajor, int versionMinor) {
		super(new Symbols(new ConstPool()));
		this.builder = new ClassBuilder();
		// set version minor and major
		builder.setVersionMajor(versionMajor);
		builder.setVersionMinor(versionMinor);
	}

	@Override
	public void visitClass(String name, int access, String superName, String... interfaces) {
		builder.setClassIndex(symbols.newClass(name));
		builder.setAccess(access);
		builder.setSuperIndex(symbols.newClass(superName));
		for (String anInterface : interfaces) {
			builder.addInterface(symbols.newClass(anInterface));
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
	public void visitOuterClass(String owner, @Nullable String name, @Nullable Descriptor descriptor) {
		attributes.add(new EnclosingMethodAttribute(
				symbols.newUtf8(AttributeConstants.ENCLOSING_METHOD),
				symbols.newClass(owner),
				name == null ? 0 : symbols.newNameType(name, descriptor)));
	}

	@Override
	public void visitInnerClass(String name, @Nullable String outerName, @Nullable String innerName, int access) {
		innerClasses.add(new InnerClass(
				symbols.newClass(name),
				outerName == null ? 0 : symbols.newClass(outerName),
				innerName == null ? 0 : symbols.newUtf8(innerName),
				access));
	}

	@Override
	public void visitSource(@Nullable String source, byte @Nullable [] debug) {
		if (source != null) {
			attributes.add(new SourceFileAttribute(
					symbols.newUtf8(AttributeConstants.SOURCE_FILE),
					symbols.newUtf8(source)));
		}
		if (debug != null) {
			attributes.add(new SourceDebugExtensionAttribute(
					symbols.newUtf8(AttributeConstants.SOURCE_DEBUG_EXTENSION),
					debug));
		}
	}

	@Override
	public void visitClassEnd() {
		super.visitDeclarationEnd();
		if(!innerClasses.isEmpty()) {
			attributes.add(new InnerClassesAttribute(
					symbols.newUtf8(AttributeConstants.INNER_CLASSES),
					innerClasses));
		}
		for (Attribute attribute : attributes) {
			builder.addAttribute(attribute);
		}
	}

	public byte[] toByteArray() throws InvalidClassException {
		builder.setConstPool(symbols.pool);
		ClassFile file = builder.build();
		ClassFileWriter writer = new ClassFileWriter();
		return writer.write(file);
	}
}
