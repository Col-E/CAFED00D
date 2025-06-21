package software.coley.cafedude.tree.visitor.reader;

import software.coley.cafedude.InvalidClassException;
import software.coley.cafedude.InvalidCodeException;
import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.ClassMember;
import software.coley.cafedude.classfile.Method;
import software.coley.cafedude.classfile.annotation.Annotation;
import software.coley.cafedude.classfile.annotation.TypeAnnotation;
import software.coley.cafedude.classfile.attribute.*;
import software.coley.cafedude.classfile.behavior.AttributeHolder;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.transform.LabelTransformer;
import software.coley.cafedude.tree.visitor.*;
import software.coley.cafedude.util.ConstantUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map.Entry;

/**
 * Helper class for transforming from information {@link ClassMember} into a visitor.
 *
 * @author Justus Garbe
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
	MemberReader(@Nonnull ClassFile file,@Nonnull LabelTransformer transformer) {
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
	 */
	static void visitDeclaration(@Nonnull DeclarationVisitor visitor,@Nonnull  AttributeHolder member) {
		AnnotationsAttribute annotations = member.getAttribute(AnnotationsAttribute.class);
		if (annotations != null) {
			boolean visible = annotations.isVisible();
			for (Annotation annotation : annotations.getAnnotations()) {
				String type = annotation.getType().getText();
				AnnotationVisitor av;
				if (annotation instanceof TypeAnnotation) {
					TypeAnnotation ta = (TypeAnnotation) annotation;
					av = visitor.visitTypeAnnotation(type, ta.getTargetInfo(), ta.getTypePath(), visible);
				} else {
					av = visitor.visitAnnotation(type, visible);
				}
				if (av == null) continue;
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

	void visitMethod(@Nullable MethodVisitor mv, @Nonnull  AttributeHolder member) throws InvalidClassException {
		if (mv == null) return;
		visitDeclaration(mv, member);
		visitCode(mv.visitCode(), (Method) member);
		ExceptionsAttribute exceptions = member.getAttribute(ExceptionsAttribute.class);
		if (exceptions != null) {
			for (CpClass exception : exceptions.getExceptionTable()) {
				mv.visitThrows(exception.getName().getText());
			}
		}
		ParameterAnnotationsAttribute parameterAnnotations = member.getAttribute(ParameterAnnotationsAttribute.class);
		if (parameterAnnotations != null) {
			boolean visible = parameterAnnotations.isVisible();
			for (Entry<Integer, List<Annotation>> entry : parameterAnnotations.getParameterAnnotations().entrySet()) {
				int parameter = entry.getKey();
				for (Annotation annotation : entry.getValue()) {
					String type = annotation.getType().getText();
					AnnotationVisitor av = mv.visitParameterAnnotation(parameter, type, visible);
					if (av == null) continue;
					AnnotationReader.visitAnnotation(annotation, av);
					av.visitAnnotationEnd();
				}
			}
		}
		MethodParametersAttribute methodParameters = member.getAttribute(MethodParametersAttribute.class);
		if (methodParameters != null) {
			for (MethodParametersAttribute.Parameter parameter : methodParameters.getParameters()) {
				String name = parameter.getName().getText();
				mv.visitParameter(name, parameter.getAccessFlags());
			}
		}
		AnnotationDefaultAttribute annotationDefault = member.getAttribute(AnnotationDefaultAttribute.class);
		if (annotationDefault != null) {
			AnnotationDefaultVisitor adv = mv.visitAnnotationDefault();
			if (adv == null) return;
			AnnotationReader.visitAnnotationDefaultElement(annotationDefault.getElementValue(), adv);
		}
		mv.visitMethodEnd();
	}

	void visitField(FieldVisitor fv, AttributeHolder member) {
		if (fv == null) return;
		visitDeclaration(fv, member);
		ConstantValueAttribute constant = member.getAttribute(ConstantValueAttribute.class);
		if (constant != null) {
			fv.visitConstantValue(ConstantUtil.from(constant.getConstantValue()));
		}
		fv.visitFieldEnd();
	}

	private void visitCode(CodeVisitor cv, Method method) throws InvalidCodeException {
		if (cv == null) return; // skip code
		CodeAttribute code = method.getAttribute(CodeAttribute.class);
		if (code == null) return; // skip code
		CodeReader cr = new CodeReader(classFile, code, cv, method,
				transformer.getLabels(method), transformer.getInstructions(method));
		cr.accept();
	}
}
