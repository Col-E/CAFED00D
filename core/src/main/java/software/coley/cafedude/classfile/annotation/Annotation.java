package software.coley.cafedude.classfile.annotation;

import software.coley.cafedude.classfile.attribute.AnnotationsAttribute;
import software.coley.cafedude.classfile.attribute.ParameterAnnotationsAttribute;
import software.coley.cafedude.classfile.behavior.CpAccessor;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpUtf8;

import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Annotation outline. Represents an annotation item to be contained in an annotation collection attribute such as:
 * <ul>
 *     <li>{@link AnnotationsAttribute RuntimeInvisibleAnnotations}</li>
 *     <li>{@link AnnotationsAttribute RuntimeVisibleAnnotations}</li>
 *     <li>{@link ParameterAnnotationsAttribute RuntimeInvisibleParameterAnnotations}</li>
 *     <li>{@link ParameterAnnotationsAttribute RuntimeVisibleParameterAnnotations}</li>
 * </ul>
 *
 * @author Matt Coley
 * @see AnnotationsAttribute
 * @see ParameterAnnotationsAttribute
 */
public class Annotation implements CpAccessor {
	private final Map<CpUtf8, ElementValue> values;
	private final CpUtf8 type;

	/**
	 * @param type
	 * 		Annotation descriptor index.
	 * @param values
	 * 		Annotation key-value pairs. Keys point to UTF8 constants.
	 */
	public Annotation(@Nonnull CpUtf8 type, @Nonnull Map<CpUtf8, ElementValue> values) {
		this.type = type;
		this.values = values;
	}

	/**
	 * @return Annotation descriptor index.
	 */
	@Nonnull
	public CpUtf8 getType() {
		return type;
	}

	/**
	 * The annotation's key-value pairs. Keys point to UTF8 constants.
	 *
	 * @return Annotation key-value pairs.
	 *
	 * @see ElementValue Values.
	 */
	@Nonnull
	public Map<CpUtf8, ElementValue> getValues() {
		return values;
	}

	@Nonnull
	@Override
	public Set<CpEntry> cpAccesses() {
		Set<CpEntry> set = new HashSet<>();
		set.add(getType());
		for (ElementValue value : values.values())
			set.addAll(value.cpAccesses());
		return set;
	}

	/**
	 * @return Computed size for the annotation.
	 */
	public int computeLength() {
		// u2: type_index
		// u2: num_element_value_pairs
		int length = 4;
		// ??: element_values
		for (Map.Entry<CpUtf8, ElementValue> entry : values.entrySet()) {
			// u2: name_index (key)
			// ??: value
			length += 2;
			length += entry.getValue().computeLength();
		}
		return length;
	}
}
