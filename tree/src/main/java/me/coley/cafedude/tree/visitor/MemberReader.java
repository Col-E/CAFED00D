package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.ClassMember;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.annotation.TypeAnnotation;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.behavior.AttributeHolder;
import me.coley.cafedude.transform.LabelTransformer;
import me.coley.cafedude.util.ConstantUtil;

import java.util.List;
import java.util.Map.Entry;

/**
 * Helper class for transforming from information {@link ClassMember} into a visitor
 */
public class MemberReader {

	private final ClassFile classFile;
	private final ConstPool pool;
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
		this.pool = file.getPool();
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
	static void visitDeclaration(DeclarationVisitor visitor, AttributeHolder member, ConstPool pool) {
		AnnotationsAttribute annotations = member.getAttribute(AnnotationsAttribute.class);
		if (annotations != null) {
			boolean visible = annotations.isVisible();
			for (Annotation annotation : annotations.getAnnotations()) {
				String type = pool.getUtf(annotation.getTypeIndex());
				AnnotationVisitor av;
				if(annotation instanceof TypeAnnotation) {
					TypeAnnotation ta = (TypeAnnotation) annotation;
					av = visitor.visitTypeAnnotation(type, ta.getTargetInfo(), ta.getTypePath(), visible);
				} else {
					av = visitor.visitAnnotation(type, visible);
				}
				if(av == null) continue;
				AnnotationReader.visitAnnotation(annotation, av, pool);
				av.visitAnnotationEnd();
			}
		}
		SignatureAttribute signature = member.getAttribute(SignatureAttribute.class);
		if (signature != null) {
			visitor.visitSignature(pool.getUtf(signature.getSignatureIndex()));
		}
		visitor.visitDeprecated(member.getAttribute(DeprecatedAttribute.class) != null);
		visitor.visitSynthetic(member.getAttribute(SyntheticAttribute.class) != null);
	}

	void visitMethod(MethodVisitor mv, AttributeHolder member) {
		if(mv == null) return;
		visitDeclaration(mv, member, pool);
		visitCode(mv.visitCode(), (Method) member);
		ExceptionsAttribute exceptions = member.getAttribute(ExceptionsAttribute.class);
		if(exceptions != null) {
			for (int exception : exceptions.getExceptionIndexTable()) {
				mv.visitThrows(ConstantUtil.getClassName(exception, pool));
			}
		}
		ParameterAnnotationsAttribute parameterAnnotations = member.getAttribute(ParameterAnnotationsAttribute.class);
		if(parameterAnnotations != null) {
			boolean visible = parameterAnnotations.isVisible();
			for (Entry<Integer, List<Annotation>> entry : parameterAnnotations.getParameterAnnotations().entrySet()) {
				int parameter = entry.getKey();
				for (Annotation annotation : entry.getValue()) {
					String type = pool.getUtf(annotation.getTypeIndex());
					AnnotationVisitor av = mv.visitParameterAnnotation(parameter, type, visible);
					if(av == null) continue;
					AnnotationReader.visitAnnotation(annotation, av, pool);
					av.visitAnnotationEnd();
				}
			}
		}
		MethodParametersAttribute methodParameters = member.getAttribute(MethodParametersAttribute.class);
		if(methodParameters != null) {
			for (MethodParametersAttribute.Parameter parameter : methodParameters.getParameters()) {
				String name = pool.getUtf(parameter.getNameIndex());
				mv.visitParameter(name, parameter.getAccessFlags());
			}
		}
		mv.visitMethodEnd();
	}

	void visitField(FieldVisitor fv, AttributeHolder member) {
		if(fv == null) return;
		visitDeclaration(fv, member, pool);
		ConstantValueAttribute constant = member.getAttribute(ConstantValueAttribute.class);
		if(constant != null) {
			fv.visitConstantValue(ConstantUtil.from(pool.get(constant.getConstantValueIndex()), pool));
		}
		fv.visitFieldEnd();
	}

	private void visitCode(CodeVisitor cv, Method method) {
		if(cv == null) return; // skip code
		CodeAttribute code = method.getAttribute(CodeAttribute.class);
		if(code == null) return; // skip code
		InstructionVisitor ir = new InstructionVisitor(classFile, code, cv, method,
				transformer.getLabels(method), transformer.getInstructions(method));
		ir. accept();
	}

}
