package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.AttributeConstants;
import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.annotation.TargetInfo;
import me.coley.cafedude.classfile.annotation.TypeAnnotation;
import me.coley.cafedude.classfile.annotation.TypePath;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.tree.visitor.AnnotationVisitor;
import me.coley.cafedude.tree.visitor.DeclarationVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DeclarationWriter implements DeclarationVisitor {

	protected final List<Attribute> attributes = new ArrayList<>();
	private final List<Annotation> visibleAnnotations = new ArrayList<>();
	private final List<Annotation> invisibleAnnotations = new ArrayList<>();
	private final List<Annotation> visibleTypeAnnotations = new ArrayList<>();
	private final List<Annotation> invisibleTypeAnnotations = new ArrayList<>();
	protected Symbols symbols;

	public DeclarationWriter(Symbols symbols) {
		this.symbols = symbols;
	}

	@Override
	public DeclarationVisitor declarationDelegate() {
		return null;
	}

	@Override
	public @Nullable AnnotationVisitor visitAnnotation(String type, boolean visible) {
		return new AnnotationWriter(symbols, values -> {
			Annotation annotation = new Annotation(symbols.newUtf8(type), values);
			if(visible)
				visibleAnnotations.add(annotation);
			else
				invisibleAnnotations.add(annotation);
		});
	}

	@Override
	public @Nullable AnnotationVisitor visitTypeAnnotation(String type, TargetInfo target, TypePath path,
														   boolean visible) {
		return new AnnotationWriter(symbols, values -> {
			// copy values over to type annotation
			TypeAnnotation typeAnnotation = new TypeAnnotation(symbols.newUtf8(type), values, target, path);
			if(visible)
				visibleTypeAnnotations.add(typeAnnotation);
			else
				invisibleTypeAnnotations.add(typeAnnotation);
		});
	}

	@Override
	public void visitSignature(String signature) {
		attributes.add(new SignatureAttribute(
				symbols.newUtf8(AttributeConstants.SIGNATURE), symbols.newUtf8(signature)));
	}

	@Override
	public void visitSynthetic(boolean synthetic) {
		if (synthetic) {
			attributes.add(new SyntheticAttribute(
					symbols.newUtf8(AttributeConstants.SYNTHETIC)));
		}
	}

	@Override
	public void visitDeprecated(boolean deprecated) {
		if(deprecated) {
			attributes.add(new DeprecatedAttribute(
					symbols.newUtf8(AttributeConstants.DEPRECATED)));
		}
	}

	void visitDeclarationEnd() {
		if(!visibleAnnotations.isEmpty()) {
			attributes.add(new AnnotationsAttribute(
					symbols.newUtf8(AttributeConstants.RUNTIME_VISIBLE_ANNOTATIONS),
					visibleAnnotations, true));
		}
		if(!invisibleAnnotations.isEmpty()) {
			attributes.add(new AnnotationsAttribute(
					symbols.newUtf8(AttributeConstants.RUNTIME_INVISIBLE_ANNOTATIONS),
					invisibleAnnotations, false));
		}
		if(!visibleTypeAnnotations.isEmpty()) {
			attributes.add(new AnnotationsAttribute(
					symbols.newUtf8(AttributeConstants.RUNTIME_VISIBLE_TYPE_ANNOTATIONS),
					visibleTypeAnnotations, true));
		}
		if(!invisibleTypeAnnotations.isEmpty()) {
			attributes.add(new AnnotationsAttribute(
					symbols.newUtf8(AttributeConstants.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS),
					invisibleTypeAnnotations, false));
		}
	}
}