package me.coley.cafedude.tree.visitor.reader;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.ClassMember;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.annotation.TypeAnnotation;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.behavior.AttributeHolder;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.transform.LabelTransformer;
import me.coley.cafedude.tree.visitor.*;
import me.coley.cafedude.util.ConstantUtil;

import java.util.List;
import java.util.Map.Entry;

/**
 * Helper class for transforming from information {@link ClassMember} into a visitor
 */
public class MemberReader {

	private final ClassFile classFile;
	private final LabelTransformer transformer;

	/**
	 * @param file
	 * 		Class file containing the member.
	 * @param transformer
	 * 		Label transformer for accessing label and instruction positions.
	 */
	MemberReader(ClassFile file, LabelTransformer transformer) {
		this.transformer = transformer;
		this.classFile = file;
	}

	/**
	 * Visit a declaration of a {@link AttributeHolder}.
	 *
	 * @param visitor
	 * 		Visitor to visit with.
	 * @param member
	 * 		Member to visit.
	 * @param pool
	 * 		Constant pool to use for resolving.
	 */
	static void visitDeclaration(DeclarationVisitor visitor, AttributeHolder member) {
		AnnotationsAttribute annotations = member.getAttribute(AnnotationsAttribute.class);
		if (annotations != null) {
			boolean visible = annotations.isVisible();
			for (Annotation annotation : annotations.getAnnotations()) {
				String type = annotation.getType().getText();
				AnnotationVisitor av;
				if(annotation instanceof TypeAnnotation) {
					TypeAnnotation ta = (TypeAnnotation) annotation;
					av = visitor.visitTypeAnnotation(type, ta.getTargetInfo(), ta.getTypePath(), visible);
				} else {
					av = visitor.visitAnnotation(type, visible);
				}
				if(av == null) continue;
				AnnotationReader.visitAnnotation(annotation, av);
				av.visitAnnotationEnd();
			}
		}
		SignatureAttribute signature = member.getAttribute(SignatureAttribute.class);
		if (signature != null) {
			visitor.visitSignature(signature.getSignature().getText());
		}
		visitor.visitDeprecated(member.getAttribute(DeprecatedAttribute.class) != null);
		visitor.visitSynthetic(member.getAttribute(SyntheticAttribute.class) != null);
	}

	void visitMethod(MethodVisitor mv, AttributeHolder member) {
		if(mv == null) return;
		visitDeclaration(mv, member);
		visitCode(mv.visitCode(), (Method) member);
		ExceptionsAttribute exceptions = member.getAttribute(ExceptionsAttribute.class);
		if(exceptions != null) {
			for (CpClass exception : exceptions.getExceptionTable()) {
				mv.visitThrows(exception.getName().getText());
			}
		}
		ParameterAnnotationsAttribute parameterAnnotations = member.getAttribute(ParameterAnnotationsAttribute.class);
		if(parameterAnnotations != null) {
			boolean visible = parameterAnnotations.isVisible();
			for (Entry<Integer, List<Annotation>> entry : parameterAnnotations.getParameterAnnotations().entrySet()) {
				int parameter = entry.getKey();
				for (Annotation annotation : entry.getValue()) {
					String type = annotation.getType().getText();
					AnnotationVisitor av = mv.visitParameterAnnotation(parameter, type, visible);
					if(av == null) continue;
					AnnotationReader.visitAnnotation(annotation, av);
					av.visitAnnotationEnd();
				}
			}
		}
		MethodParametersAttribute methodParameters = member.getAttribute(MethodParametersAttribute.class);
		if(methodParameters != null) {
			for (MethodParametersAttribute.Parameter parameter : methodParameters.getParameters()) {
				String name = parameter.getName().getText();
				mv.visitParameter(name, parameter.getAccessFlags());
			}
		}
		AnnotationDefaultAttribute annotationDefault = member.getAttribute(AnnotationDefaultAttribute.class);
		if(annotationDefault != null) {
			AnnotationDefaultVisitor adv = mv.visitAnnotationDefault();
			if(adv == null) return;
			AnnotationReader.visitAnnotationDefaultElement(annotationDefault.getElementValue(), adv);
		}
		mv.visitMethodEnd();
	}

	void visitField(FieldVisitor fv, AttributeHolder member) {
		if(fv == null) return;
		visitDeclaration(fv, member);
		ConstantValueAttribute constant = member.getAttribute(ConstantValueAttribute.class);
		if(constant != null) {
			fv.visitConstantValue(ConstantUtil.from(constant.getConstantValue()));
		}
		fv.visitFieldEnd();
	}

	private void visitCode(CodeVisitor cv, Method method) {
		if(cv == null) return; // skip code
		CodeAttribute code = method.getAttribute(CodeAttribute.class);
		if(code == null) return; // skip code
		CodeReader cr = new CodeReader(classFile, code, cv, method,
				transformer.getLabels(method), transformer.getInstructions(method));
		cr.accept();
	}

}
