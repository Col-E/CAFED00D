package software.coley.cafedude.classfile.annotation;

import software.coley.cafedude.classfile.constant.CpUtf8;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Type annotation outline.
 *
 * @author Matt Coley
 */
public class TypeAnnotation extends Annotation {
	private final TargetInfo targetInfo;
	private final TypePath typePath;

	/**
	 * @param typeIndex
	 * 		Annotation descriptor index.
	 * @param values
	 * 		Annotation key-value pairs. Keys point to UTF8 constants.
	 * @param targetInfo
	 * 		Information about where the annotation is applied.
	 * @param typePath
	 * 		Information about which part of the type is annotated.
	 */
	public TypeAnnotation(@Nonnull CpUtf8 typeIndex, @Nonnull Map<CpUtf8, ElementValue> values,
	                      @Nonnull TargetInfo targetInfo, @Nonnull TypePath typePath) {
		super(typeIndex, values);
		this.targetInfo = targetInfo;
		this.typePath = typePath;
	}

	/**
	 * @return Information about where the annotation is applied.
	 */
	@Nonnull
	public TargetInfo getTargetInfo() {
		return targetInfo;
	}

	/**
	 * @return Information about which part of the type is annotated.
	 */
	@Nonnull
	public TypePath getTypePath() {
		return typePath;
	}

	@Override
	public int computeLength() {
		int length = 1; // u1: target_type
		length += targetInfo.computeLength(); // ??: target_info
		length += typePath.computeLength(); // type_path: target_path
		// Now add the rest of the normal annotation length
		return length + super.computeLength();
	}
}
