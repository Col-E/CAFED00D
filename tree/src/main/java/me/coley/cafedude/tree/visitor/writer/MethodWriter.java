package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.AttributeConstants;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.tree.visitor.AnnotationDefaultVisitor;
import me.coley.cafedude.tree.visitor.AnnotationVisitor;
import me.coley.cafedude.tree.visitor.CodeVisitor;
import me.coley.cafedude.tree.visitor.MethodVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MethodWriter extends DeclarationWriter implements MethodVisitor {

	private final List<Attribute> attributes = new ArrayList<>();
	private final List<CpClass> exceptions = new ArrayList<>();
	private final List<MethodParametersAttribute.Parameter> parameters = new ArrayList<>();
	private final Map<Integer, List<Annotation>> visibleParameterAnnotations = new HashMap<>();
	private final Map<Integer, List<Annotation>> invisibleParameterAnnotations = new HashMap<>();
	private final Consumer<Method> callback;
	private final Method method;

	public MethodWriter(Symbols symbols, int access, CpUtf8 name, CpUtf8 descriptor, Consumer<Method> callback) {
		super(symbols);
		this.method = new Method(attributes, access, name, descriptor);
		this.callback = callback;
	}

	@Override
	public void visitThrows(String type) {
		exceptions.add(symbols.newClass(type));
	}

	@Override
	public void visitParameter(String name, int access) {
		parameters.add(new MethodParametersAttribute.Parameter(
				access,
				symbols.newUtf8(name)));
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String type, boolean visible) {
		return new AnnotationWriter(symbols, values -> {
			Annotation annotation = new Annotation(symbols.newUtf8(type), values);
			Map<Integer, List<Annotation>> map = visible ? visibleParameterAnnotations : invisibleParameterAnnotations;
			map.computeIfAbsent(parameter, k -> new ArrayList<>()).add(annotation);
		});
	}

	@Override
	public CodeVisitor visitCode() {
		return new CodeWriter(symbols, attributes::add);
	}

	@Override
	public AnnotationDefaultVisitor visitAnnotationDefault() {
		return new AnnotationDefaultWriter(symbols, value -> {
			attributes.add(new AnnotationDefaultAttribute(
					symbols.newUtf8(AttributeConstants.ANNOTATION_DEFAULT),
					value));
		});
	}

	@Override
	public void visitMethodEnd() {
		super.visitDeclarationEnd();
		if(!exceptions.isEmpty()) {
			attributes.add(new ExceptionsAttribute(
					symbols.newUtf8(AttributeConstants.EXCEPTIONS),
					exceptions));
		}
		if(!parameters.isEmpty()) {
			attributes.add(new MethodParametersAttribute(
					symbols.newUtf8(AttributeConstants.METHOD_PARAMETERS),
					parameters));
		}
		if(!visibleParameterAnnotations.isEmpty()) {
			attributes.add(new ParameterAnnotationsAttribute(
					symbols.newUtf8(AttributeConstants.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS),
					visibleParameterAnnotations,
					true));
		}
		if(!invisibleParameterAnnotations.isEmpty()) {
			attributes.add(new ParameterAnnotationsAttribute(
					symbols.newUtf8(AttributeConstants.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS),
					invisibleParameterAnnotations,
					false));
		}
		callback.accept(method);
	}
}
