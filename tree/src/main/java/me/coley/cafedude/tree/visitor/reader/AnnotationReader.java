package me.coley.cafedude.tree.visitor.reader;

import me.coley.cafedude.classfile.annotation.*;
import me.coley.cafedude.classfile.constant.CpUtf8;
import me.coley.cafedude.tree.visitor.AnnotationArrayVisitor;
import me.coley.cafedude.tree.visitor.AnnotationDefaultVisitor;
import me.coley.cafedude.tree.visitor.AnnotationVisitor;
import me.coley.cafedude.util.ConstantUtil;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Reader for an {@link Annotation} to pass it along to a {@link AnnotationVisitor}.
 *
 * @author Justus Garbe
 */
class AnnotationReader {
	static void visitAnnotation(@Nonnull Annotation annotation, @Nonnull AnnotationVisitor av) {
		for (Map.Entry<CpUtf8, ElementValue> entry : annotation.getValues().entrySet()) {
			visitAnnotationElement(entry.getKey().getText(), entry.getValue(), av);
		}
	}

	static void visitAnnotationElement(@Nonnull String key, @Nonnull ElementValue value,
									   @Nonnull AnnotationVisitor av) {
		if (value.getTag() == '[' || value.getTag() == '@' || value.getTag() == 'e') {
			if (value instanceof ArrayElementValue) {
				ArrayElementValue array = (ArrayElementValue) value;
				AnnotationArrayVisitor aav = av.visitArray(key);
				if (aav == null) return; // skip
				for (ElementValue elementValue : array.getArray()) {
					visitArrayElement(elementValue, aav);
				}
				aav.visitArrayEnd();
			} else if (value instanceof EnumElementValue) {
				EnumElementValue enumValue = (EnumElementValue) value;
				av.visitEnum(key, enumValue.getType().getText(), enumValue.getName().getText());
			} else if (value instanceof AnnotationElementValue) {
				AnnotationElementValue annotationValue = (AnnotationElementValue) value;
				Annotation annotation = annotationValue.getAnnotation();
				AnnotationVisitor annotationVisitor = av.visitAnnotation(key, annotation.getType().getText());
				if (annotationVisitor == null) return; // skip
				visitAnnotation(annotationValue.getAnnotation(), annotationVisitor);
				annotationVisitor.visitAnnotationEnd();
			}
		} else {
			av.visitValue(key, ConstantUtil.from(value));
		}
	}

	static void visitArrayElement(@Nonnull ElementValue value, @Nonnull AnnotationArrayVisitor aav) {
		if (value.getTag() == '[' || value.getTag() == '@' || value.getTag() == 'e') {
			if (value instanceof ArrayElementValue) {
				ArrayElementValue array = (ArrayElementValue) value;
				AnnotationArrayVisitor aav2 = aav.visitSubArray();
				if (aav2 == null) return; // skip
				for (ElementValue elementValue : array.getArray()) {
					visitArrayElement(elementValue, aav2);
				}
				aav2.visitArrayEnd();
			} else if (value instanceof EnumElementValue) {
				EnumElementValue enumValue = (EnumElementValue) value;
				aav.visitArrayEnum(enumValue.getType().getText(), enumValue.getName().getText());
			} else if (value instanceof AnnotationElementValue) {
				AnnotationElementValue annotationValue = (AnnotationElementValue) value;
				Annotation annotation = annotationValue.getAnnotation();
				AnnotationVisitor annotationVisitor = aav.visitArrayAnnotation(annotation.getType().getText());
				if (annotationVisitor == null) return; // skip
				visitAnnotation(annotationValue.getAnnotation(), annotationVisitor);
				annotationVisitor.visitAnnotationEnd();
			}
		} else {
			aav.visitArrayValue(ConstantUtil.from(value));
		}
	}

	static void visitAnnotationDefaultElement(@Nonnull ElementValue value, @Nonnull AnnotationDefaultVisitor adv) {
		if (value.getTag() == '[' || value.getTag() == '@' || value.getTag() == 'e') {
			if (value instanceof ArrayElementValue) {
				ArrayElementValue array = (ArrayElementValue) value;
				AnnotationArrayVisitor aav = adv.visitDefaultArray();
				if (aav == null) return; // skip
				for (ElementValue elementValue : array.getArray()) {
					visitArrayElement(elementValue, aav);
				}
				aav.visitArrayEnd();
			} else if (value instanceof EnumElementValue) {
				EnumElementValue enumValue = (EnumElementValue) value;
				adv.visitDefaultEnum(enumValue.getType().getText(), enumValue.getName().getText());
			} else if (value instanceof AnnotationElementValue) {
				AnnotationElementValue annotationValue = (AnnotationElementValue) value;
				Annotation annotation = annotationValue.getAnnotation();
				AnnotationVisitor annotationVisitor = adv.visitDefaultAnnotation(annotation.getType().getText());
				if (annotationVisitor == null) return; // skip
				visitAnnotation(annotationValue.getAnnotation(), annotationVisitor);
				annotationVisitor.visitAnnotationEnd();
			}
		} else {
			adv.visitDefaultValue(ConstantUtil.from(value));
		}
	}
}
