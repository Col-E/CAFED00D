package me.coley.cafedude.annotation;

import me.coley.cafedude.attribute.AnnotationsAttribute;
import me.coley.cafedude.attribute.ParameterAnnotationsAttribute;

import java.util.Map;

/**
 * Annotation outline. Represents an annotation item to be contained in an annotation collection attribute such as:
 * <ul>
 *     <li>{@link AnnotationsAttribute RuntimeInvisibleAnnotations}</li>>
 *     <li>{@link AnnotationsAttribute RuntimeVisibleAnnotations}</li>>
 *     <li>{@link ParameterAnnotationsAttribute RuntimeInvisibleParameterAnnotations}</li>>
 *     <li>{@link ParameterAnnotationsAttribute RuntimeVisibleParameterAnnotations}</li>>
 * </ul>
 *
 * @author Matt Coley
 * @see AnnotationsAttribute
 * @see ParameterAnnotationsAttribute
 */
public class Annotation {
	private final Map<Integer, ElementValue> values;
	private final int typeIndex;

	/**
	 * @param typeIndex
	 * 		Annotation descriptor index.
	 * @param values
	 * 		Annotation key-value pairs. Keys point to UTF8 constants.
	 */
	public Annotation(int typeIndex, Map<Integer, ElementValue> values) {
		this.typeIndex = typeIndex;
		this.values = values;
	}

	/**
	 * @return Annotation descriptor index.
	 */
	public int getTypeIndex() {
		return typeIndex;
	}

	/**
	 * The annotation's key-value pairs. Keys point to UTF8 constants.
	 *
	 * @return Annotation key-value pairs.
	 *
	 * @see ElementValue Values.
	 */
	public Map<Integer, ElementValue> getValues() {
		return values;
	}

	/**
	 * @return Computed size for the annotation.
	 */
	public int computeLength() {
		// u2: type_index
		// u2: num_element_value_pairs
		int length = 4;
		// ??: element_values
		for (Map.Entry<Integer, ElementValue> entry : values.entrySet()) {
			// u2: name_index (key)
			// ??: value
			length += 2;
			length += entry.getValue().computeLength();
		}
		return length;
	}
}
