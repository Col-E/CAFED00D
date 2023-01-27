package me.coley.cafedude.io;

import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.annotation.AnnotationElementValue;
import me.coley.cafedude.classfile.annotation.ArrayElementValue;
import me.coley.cafedude.classfile.annotation.ClassElementValue;
import me.coley.cafedude.classfile.annotation.ElementValue;
import me.coley.cafedude.classfile.annotation.EnumElementValue;
import me.coley.cafedude.classfile.annotation.PrimitiveElementValue;
import me.coley.cafedude.classfile.annotation.TargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.CatchTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.FormalParameterTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.LocalVarTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.LocalVarTargetInfo.Variable;
import me.coley.cafedude.classfile.annotation.TargetInfo.OffsetTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.SuperTypeTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.ThrowsTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.TypeArgumentTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.TypeParameterBoundTargetInfo;
import me.coley.cafedude.classfile.annotation.TargetInfo.TypeParameterTargetInfo;
import me.coley.cafedude.classfile.annotation.TypeAnnotation;
import me.coley.cafedude.classfile.annotation.TypePath;
import me.coley.cafedude.classfile.annotation.TypePathElement;
import me.coley.cafedude.classfile.annotation.Utf8ElementValue;
import me.coley.cafedude.classfile.attribute.AnnotationDefaultAttribute;
import me.coley.cafedude.classfile.attribute.AnnotationsAttribute;
import me.coley.cafedude.classfile.attribute.ParameterAnnotationsAttribute;
import me.coley.cafedude.classfile.constant.CpUtf8;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Annotation writer for all annotation attributes.
 *
 * @author Matt Coley
 */
public class AnnotationWriter {
	private final DataOutputStream out;

	/**
	 * Create an annotation writer.
	 *
	 * @param out
	 * 		Stream to write to.
	 */
	public AnnotationWriter(DataOutputStream out) {
		this.out = out;
	}

	/**
	 * Writes an {@link AnnotationDefaultAttribute} attribute.
	 *
	 * @param annoDefault
	 * 		Default value attribute to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	public void writeAnnotationDefault(AnnotationDefaultAttribute annoDefault) throws IOException {
		writeElementValue(annoDefault.getElementValue());
	}

	/**
	 * Writes an attribute containing multiple annotations. Used for:
	 * <ul>
	 *     <li>{@code RuntimeInvisibleAnnotations}</li>
	 *     <li>{@code RuntimeVisibleAnnotations}</li>
	 * </ul>
	 *
	 * @param annos
	 * 		Annotations to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	public void writeAnnotations(AnnotationsAttribute annos) throws IOException {
		out.writeShort(annos.getAnnotations().size());
		for (Annotation annotation : annos.getAnnotations()) {
			writeAnnotation(annotation);
		}
	}

	/**
	 * Writes an attribute containing multiple type annotations. Used for:
	 * <ul>
	 *     <li>{@code TypeParameterAnnotations}</li>
	 * </ul>
	 *
	 * @param annos
	 * 		Annotations to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	public void writeTypeAnnotations(AnnotationsAttribute annos) throws IOException {
		out.writeShort(annos.getAnnotations().size());
		for (Annotation annotation : annos.getAnnotations()) {
			writeTypeAnnotation((TypeAnnotation) annotation);
		}
	}

	/**
	 * Writes a {@link ParameterAnnotationsAttribute} attribute.
	 *
	 * @param annos
	 * 		Annotations to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	public void writeParameterAnnotations(ParameterAnnotationsAttribute annos) throws IOException {
		out.writeByte(annos.getParameterAnnotations().size());
		for (Map.Entry<Integer, List<Annotation>> parameterAnnotations : annos.getParameterAnnotations().entrySet()) {
			List<Annotation> annotations = parameterAnnotations.getValue();
			out.writeShort(annotations.size());
			for (Annotation annotation : annotations) {
				writeAnnotation(annotation);
			}
		}
	}

	/**
	 * Common annotation structure writing.
	 *
	 * @param annotation
	 * 		Annotation to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	private void writeAnnotation(Annotation annotation) throws IOException {
		out.writeShort(annotation.getType().getIndex());
		writeElementPairs(annotation.getValues());
	}

	/**
	 * Common type annotation structure writing.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	private void writeTypeAnnotation(TypeAnnotation annotation) throws IOException {
		// Write target info union
		TargetInfo info = annotation.getTargetInfo();
		out.writeByte(info.getTargetType());
		switch (info.getTargetTypeKind()) {
			case TYPE_PARAMETER_TARGET:
				TypeParameterTargetInfo typeParameterTargetInfo = (TypeParameterTargetInfo) info;
				out.writeByte(typeParameterTargetInfo.getTypeParameterIndex());
				break;
			case SUPERTYPE_TARGET:
				SuperTypeTargetInfo superTypeTargetInfo = (SuperTypeTargetInfo) info;
				out.writeShort(superTypeTargetInfo.getSuperTypeIndex());
				break;
			case TYPE_PARAMETER_BOUND_TARGET:
				TypeParameterBoundTargetInfo typeParameterBoundTargetInfo = (TypeParameterBoundTargetInfo) info;
				out.writeByte(typeParameterBoundTargetInfo.getTypeParameterIndex());
				out.writeByte(typeParameterBoundTargetInfo.getBoundIndex());
				break;
			case EMPTY_TARGET:
				// no-op
				break;
			case FORMAL_PARAMETER_TARGET:
				FormalParameterTargetInfo formalParameterTargetInfo = (FormalParameterTargetInfo) info;
				out.writeByte(formalParameterTargetInfo.getFormalParameterIndex());
				break;
			case THROWS_TARGET:
				ThrowsTargetInfo throwsTargetInfo = (ThrowsTargetInfo) info;
				out.writeShort(throwsTargetInfo.getThrowsTypeIndex());
				break;
			case LOCALVAR_TARGET:
				LocalVarTargetInfo localVarTargetInfo = (LocalVarTargetInfo) info;
				out.writeShort(localVarTargetInfo.getVariableTable().size());
				for (Variable variable : localVarTargetInfo.getVariableTable()) {
					out.writeShort(variable.getStartPc());
					out.writeShort(variable.getLength());
					out.writeShort(variable.getIndex());
				}
				break;
			case CATCH_TARGET:
				CatchTargetInfo catchTargetInfo = (CatchTargetInfo) info;
				out.writeShort(catchTargetInfo.getExceptionTableIndex());
				break;
			case OFFSET_TARGET:
				OffsetTargetInfo offsetTargetInfo = (OffsetTargetInfo) info;
				out.writeShort(offsetTargetInfo.getOffset());
				break;
			case TYPE_ARGUMENT_TARGET:
				TypeArgumentTargetInfo typeArgumentTargetInfo = (TypeArgumentTargetInfo) info;
				out.writeShort(typeArgumentTargetInfo.getOffset());
				out.writeByte(typeArgumentTargetInfo.getTypeArgumentIndex());
				break;
			default:
				throw new IllegalArgumentException("Invalid type argument target");
		}
		// Write type path
		writeTypePath(annotation.getTypePath());
		// Write the rest of the normal annotation
		writeAnnotation(annotation);
	}

	/**
	 * @param typePath
	 * 		Type path to write
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	private void writeTypePath(TypePath typePath) throws IOException {
		out.writeByte(typePath.getPath().size());
		for (TypePathElement element : typePath.getPath()) {
			out.writeByte(element.getKind().getValue());
			out.writeByte(element.getArgIndex());
		}
	}

	/**
	 * @param values
	 * 		The annotation field pairs <i>({@code name} --> {@code Value})</i> to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	private void writeElementPairs(Map<CpUtf8, ElementValue> values) throws IOException {
		out.writeShort(values.size());
		for (Map.Entry<CpUtf8, ElementValue> elementValueEntry : values.entrySet()) {
			CpUtf8 name = elementValueEntry.getKey();
			out.writeShort(name.getIndex());
			ElementValue value = elementValueEntry.getValue();
			writeElementValue(value);
		}
	}

	/**
	 * @param elementValue
	 * 		Annotation field <i>(Technically method)</i> value to write.
	 *
	 * @throws IOException
	 * 		When the stream cannot be written to.
	 */
	private void writeElementValue(ElementValue elementValue) throws IOException {
		out.writeByte(elementValue.getTag());
		switch (elementValue.getTag()) {
			case 'B': // byte
			case 'C': // char
			case 'D': // double
			case 'F': // float
			case 'I': // int
			case 'J': // long
			case 'S': // short
			case 'Z': // boolean
				PrimitiveElementValue primitiveElementValue = (PrimitiveElementValue) elementValue;
				out.writeShort(primitiveElementValue.getValue().getIndex());
				break;
			case 's': // String
				Utf8ElementValue utf8ElementValue = (Utf8ElementValue) elementValue;
				out.writeShort(utf8ElementValue.getValue().getIndex());
				break;
			case 'e': // Enum
				EnumElementValue enumElementValue = (EnumElementValue) elementValue;
				out.writeShort(enumElementValue.getType().getIndex());
				out.writeShort(enumElementValue.getName().getIndex());
				break;
			case 'c': // Class
				ClassElementValue classElementValue = (ClassElementValue) elementValue;
				out.writeShort(classElementValue.getClassEntry().getIndex());
				break;
			case '@': // Annotation
				AnnotationElementValue annotationElementValue = (AnnotationElementValue) elementValue;
				writeAnnotation(annotationElementValue.getAnnotation());
				break;
			case '[': // Array
				ArrayElementValue arrayElementValue = (ArrayElementValue) elementValue;
				out.writeShort(arrayElementValue.getArray().size());
				for (ElementValue arrayValue : arrayElementValue.getArray()) {
					writeElementValue(arrayValue);
				}
				break;
			default:
				break;
		}
	}
}
