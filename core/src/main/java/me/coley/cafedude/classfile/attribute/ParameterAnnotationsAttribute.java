package me.coley.cafedude.classfile.attribute;

import me.coley.cafedude.classfile.annotation.Annotation;
import me.coley.cafedude.classfile.constant.CpEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

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
	private boolean visible;

	/**
	 * @param name
	 * 		Name index in constant pool.
	 * @param parameterAnnotations
	 * 		Map of parameter indices to their list of attributes.
	 * @param visible
	 * 		Whether the annotations are visible at runtime.
	 */
	public ParameterAnnotationsAttribute(CpUtf8 name, Map<Integer, List<Annotation>> parameterAnnotations,
										 boolean visible) {
		super(name);
		this.parameterAnnotations = parameterAnnotations;
		this.visible = visible;
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

	/**
	 * @return {@code true} if the annotations are visible at runtime.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible
	 * 		{@code true} if the annotations are visible at runtime.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = super.cpAccesses();
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
