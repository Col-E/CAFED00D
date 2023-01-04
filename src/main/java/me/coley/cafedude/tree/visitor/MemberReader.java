package me.coley.cafedude.tree.visitor;

import me.coley.cafedude.classfile.*;
import me.coley.cafedude.classfile.annotation.*;
import me.coley.cafedude.classfile.attribute.AnnotationsAttribute;
import me.coley.cafedude.classfile.attribute.CodeAttribute;
import me.coley.cafedude.classfile.constant.ConstPoolEntry;
import me.coley.cafedude.transform.LabelTransformer;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.util.ConstantUtil;

import java.util.Map;

public class MemberReader {

	private final String name;
	private final int access;
	private final Descriptor descriptor;
	private final AnnotationsAttribute annotations;
	private final ClassVisitor visitor;
	private final ClassMember member;
	private final ClassFile classFile;
	private final ConstPool pool;
	private final LabelTransformer transformer;

	public MemberReader(ClassMember member, ClassFile file, ClassVisitor cv, LabelTransformer transformer) {
		this.name = file.getPool().getUtf(member.getNameIndex());
		this.access = member.getAccess();
		this.descriptor = Descriptor.from(file.getPool().getUtf(member.getTypeIndex()));
		this.annotations = member.getAttribute(AnnotationsAttribute.class).orElse(null);
		this.visitor = cv;
		this.transformer = transformer;
		this.classFile = file;
		this.pool = file.getPool();
		this.member = member;
	}

	public void visitMethod() {
		MethodVisitor mv = visitor.visitMethod(name, access, descriptor);
		if (mv == null) return;
		if(annotations != null) {
			boolean visible = annotations.isVisible();
			for (Annotation annotation : annotations.getAnnotations()) {
				AnnotationVisitor av = mv.visitAnnotation(pool.getUtf(annotation.getTypeIndex()), visible);
				if(av == null) continue;
				visitAnnotation(annotation, av);
				av.visitAnnotationEnd();
			}
		}
		visitCode(mv.visitCode(), (Method) member);
		mv.visitMethodEnd();
	}

	public void visitField() {
		FieldVisitor fv = visitor.visitField(name, access, descriptor);
		if (fv == null) return;
		if(annotations != null) {
			boolean visible = annotations.isVisible();
			for (Annotation annotation : annotations.getAnnotations()) {
				AnnotationVisitor av = fv.visitAnnotation(pool.getUtf(annotation.getTypeIndex()), visible);
				if(av == null) continue;
				visitAnnotation(annotation, av);
				av.visitAnnotationEnd();
			}
		}
		fv.visitFieldEnd();
	}

	private void visitAnnotation(Annotation annotation, AnnotationVisitor av) {
		for (Map.Entry<Integer, ElementValue> entry : annotation.getValues().entrySet()) {
			visitAnnotationElement(pool.getUtf(entry.getKey()), entry.getValue(), av);
		}
	}

	private void visitAnnotationElement(String key, ElementValue value, AnnotationVisitor av) {
		if(value.getTag() == '[' || value.getTag() == '@' || value.getTag() == 'e') {
			if(value instanceof ArrayElementValue) {
				ArrayElementValue array = (ArrayElementValue) value;
				AnnotationArrayVisitor aav = av.visitArray(key);
				if(aav == null) return; // skip
				for (ElementValue elementValue : array.getArray()) {
					visitArrayElement(elementValue, aav);
				}
				aav.visitArrayEnd();
			} else if(value instanceof EnumElementValue) {
				EnumElementValue enumValue = (EnumElementValue) value;
				av.visitEnum(key, pool.getUtf(enumValue.getTypeIndex()), pool.getUtf(enumValue.getNameIndex()));
			} else if(value instanceof AnnotationElementValue) {
				AnnotationElementValue annotationValue = (AnnotationElementValue) value;
				Annotation annotation = annotationValue.getAnnotation();
				AnnotationVisitor annotationVisitor = av.visitAnnotation(key, pool.getUtf(annotation.getTypeIndex()));
				if(annotationVisitor == null) return; // skip
				visitAnnotation(annotationValue.getAnnotation(), annotationVisitor);
				annotationVisitor.visitAnnotationEnd();
			}
		} else {
			av.visitValue(key, ConstantUtil.from(value, pool));
		}
	}

	private void visitArrayElement(ElementValue value, AnnotationArrayVisitor aav) {
		if(value.getTag() == '[' || value.getTag() == '@' || value.getTag() == 'e') {
			if(value instanceof ArrayElementValue) {
				ArrayElementValue array = (ArrayElementValue) value;
				AnnotationArrayVisitor aav2 = aav.visitSubArray();
				if(aav2 == null) return; // skip
				for (ElementValue elementValue : array.getArray()) {
					visitArrayElement(elementValue, aav2);
				}
				aav2.visitArrayEnd();
			} else if(value instanceof EnumElementValue) {
				EnumElementValue enumValue = (EnumElementValue) value;
				aav.visitArrayEnum(pool.getUtf(enumValue.getTypeIndex()), pool.getUtf(enumValue.getNameIndex()));
			} else if(value instanceof AnnotationElementValue) {
				AnnotationElementValue annotationValue = (AnnotationElementValue) value;
				Annotation annotation = annotationValue.getAnnotation();
				AnnotationVisitor annotationVisitor = aav.visitArrayAnnotation(pool.getUtf(annotation.getTypeIndex()));
				if(annotationVisitor == null) return; // skip
				visitAnnotation(annotationValue.getAnnotation(), annotationVisitor);
				annotationVisitor.visitAnnotationEnd();
			}
		} else {
			aav.visitArrayValue(ConstantUtil.from(value, pool));
		}
	}

	private void visitCode(CodeVisitor cv, Method method) {
		if(cv == null) return; // skip code
		CodeAttribute code = method.getAttribute(CodeAttribute.class).orElse(null);
		if(code == null) return; // skip code
		InstructionVisitor ir = new InstructionVisitor(classFile, code, cv, method,
				transformer.getLabels(method), transformer.getInstructions(method));
		ir. accept();
	}

}
