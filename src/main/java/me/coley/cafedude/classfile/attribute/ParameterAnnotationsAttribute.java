package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.annotation.Annotation;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Annotation collection attribute on method parameters. Represents either:
 * <ul>
 *     <li>{@code RuntimeInvisibleParameterAnnotations}</li>>
 *     <li>{@code RuntimeVisibleParameterAnnotations}</li>>
 * </ul>
 *
 * @author Matt Coley
 */
public class ParameterAnnotationsAttribute extends Attribute {
	private Map<Integer, List<Annotation>> parameterAnnotations;

	/**
	 * @param nameIndex
	 * 		Name index in constant pool.
	 * @param parameterAnnotations
	 * 		Map of parameter indices to their list of attributes.
	 */
	public ParameterAnnotationsAttribute(int nameIndex, Map<Integer, List<Annotation>> parameterAnnotations) {
		super(nameIndex);
		this.parameterAnnotations = parameterAnnotations;
	}

	/**
	 * @return Map of parameter indices to their list of attributes.
	 */
	public Map<Integer, List<Annotation>> getParameterAnnotations() {
		return parameterAnnotations;
	}

	/**
	 * @param parameterAnnotations
	 * 		Map of parameter indices to their list of attributes.
	 */
	public void setParameterAnnotations(Map<Integer, List<Annotation>> parameterAnnotations) {
		this.parameterAnnotations = parameterAnnotations;
	}

	@Override
	public Set<Integer> cpAccesses() {
		Set<Integer> set = super.cpAccesses();
		for (List<Annotation> list : getParameterAnnotations().values())
			for (Annotation annotation : list)
				set.addAll(annotation.cpAccesses());
		return set;
	}

	@Override
	public int computeInternalLength() {
		int length = 1; // u1: num_parameters
		for (List<Annotation> annotations : parameterAnnotations.values()) {
			// u2: num_annotations + annotations
			length += 2 + annotations.stream().mapToInt(Annotation::computeLength).sum();
		}
		return length;
	}
}
