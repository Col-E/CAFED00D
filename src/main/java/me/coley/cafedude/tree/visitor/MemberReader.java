package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.attribute.*;
import me.coley.cafedude.classfile.behavior.AttributeHolder;
import me.coley.cafedude.transform.LabelTransformer;
import me.coley.cafedude.util.ConstantUtil;

public class MemberReader {

	private final ClassFile classFile;
	private final ConstPool pool;
	private final LabelTransformer transformer;

	public MemberReader(ClassFile file, LabelTransformer transformer) {
		this.transformer = transformer;
		this.classFile = file;
		this.pool = file.getPool();
	}

	public static void visitDeclaration(DeclarationVisitor visitor, AttributeHolder member, ConstPool pool) {
		AnnotationsAttribute annotations = member.getAttribute(AnnotationsAttribute.class);
		if (annotations != null) {
			for (Annotation annotation : annotations.getAnnotations()) {
				String type = pool.getUtf(annotation.getTypeIndex());
				AnnotationVisitor av = visitor.visitAnnotation(type, annotations.isVisible());
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
	}

	public void visitMethod(MethodVisitor mv, AttributeHolder member) {
		if(mv == null) return;
		visitDeclaration(mv, member, pool);
		visitCode(mv.visitCode(), (Method) member);
		ExceptionsAttribute exceptions = member.getAttribute(ExceptionsAttribute.class);
		if(exceptions != null) {
			for (int exception : exceptions.getExceptionIndexTable()) {
				mv.visitThrows(ConstantUtil.getClassName(exception, pool));
			}
		}
		mv.visitMethodEnd();
	}

	public void visitField(FieldVisitor fv, AttributeHolder member) {
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
