package me.coley.cafedude.tree.visitor.reader;

import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.annotation.*;
import me.coley.cafedude.tree.visitor.AnnotationArrayVisitor;
import me.coley.cafedude.tree.visitor.AnnotationDefaultVisitor;
import me.coley.cafedude.tree.visitor.AnnotationVisitor;
import me.coley.cafedude.util.ConstantUtil;

import java.util.Map;

class AnnotationReader {

	static void visitAnnotation(Annotation annotation, AnnotationVisitor av, ConstPool pool) {
		for (Map.Entry<Integer, ElementValue> entry : annotation.getValues().entrySet()) {
			visitAnnotationElement(pool.getUtf(entry.getKey()), entry.getValue(), av, pool);
		}
	}

	static void visitAnnotationElement(String key, ElementValue value, AnnotationVisitor av, ConstPool pool) {
		if(value.getTag() == '[' || value.getTag() == '@' || value.getTag() == 'e') {
			if(value instanceof ArrayElementValue) {
				ArrayElementValue array = (ArrayElementValue) value;
				AnnotationArrayVisitor aav = av.visitArray(key);
				if(aav == null) return; // skip
				for (ElementValue elementValue : array.getArray()) {
					visitArrayElement(elementValue, aav, pool);
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
				visitAnnotation(annotationValue.getAnnotation(), annotationVisitor, pool);
				annotationVisitor.visitAnnotationEnd();
			}
		} else {
			av.visitValue(key, ConstantUtil.from(value, pool));
		}
	}

	static void visitArrayElement(ElementValue value, AnnotationArrayVisitor aav, ConstPool pool) {
		if(value.getTag() == '[' || value.getTag() == '@' || value.getTag() == 'e') {
			if(value instanceof ArrayElementValue) {
				ArrayElementValue array = (ArrayElementValue) value;
				AnnotationArrayVisitor aav2 = aav.visitSubArray();
				if(aav2 == null) return; // skip
				for (ElementValue elementValue : array.getArray()) {
					visitArrayElement(elementValue, aav2, pool);
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
				visitAnnotation(annotationValue.getAnnotation(), annotationVisitor, pool);
				annotationVisitor.visitAnnotationEnd();
			}
		} else {
			aav.visitArrayValue(ConstantUtil.from(value, pool));
		}
	}

	static void visitAnnotationDefaultElement(ElementValue value, AnnotationDefaultVisitor adv, ConstPool pool) {
		if(value.getTag() == '[' || value.getTag() == '@' || value.getTag() == 'e') {
			if(value instanceof ArrayElementValue) {
				ArrayElementValue array = (ArrayElementValue) value;
				AnnotationArrayVisitor aav = adv.visitDefaultArray();
				if(aav == null) return; // skip
				for (ElementValue elementValue : array.getArray()) {
					visitArrayElement(elementValue, aav, pool);
				}
				aav.visitArrayEnd();
			} else if(value instanceof EnumElementValue) {
				EnumElementValue enumValue = (EnumElementValue) value;
				adv.visitDefaultEnum(pool.getUtf(enumValue.getTypeIndex()), pool.getUtf(enumValue.getNameIndex()));
			} else if(value instanceof AnnotationElementValue) {
				AnnotationElementValue annotationValue = (AnnotationElementValue) value;
				Annotation annotation = annotationValue.getAnnotation();
				AnnotationVisitor annotationVisitor = adv.visitDefaultAnnotation(pool.getUtf(annotation.getTypeIndex()));
				if(annotationVisitor == null) return; // skip
				visitAnnotation(annotationValue.getAnnotation(), annotationVisitor, pool);
				annotationVisitor.visitAnnotationEnd();
			}
		} else {
			adv.visitDefaultValue(ConstantUtil.from(value, pool));
		}
	}

}
