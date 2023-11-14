package software.coley.cafedude.classfile.annotation;

import javax.annotation.Nonnull;
import java.util.List;

// TODO: Validate that my understanding of the path length matches reality
//       https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.7.20.2

/**
 * Identifies which part of the type is annotated by a type annotation.
 * <br>
 * Example with a 2D {@code String} array:
 * <pre>
 * &#064;Foo String[][]   // Annotates the class type String     - Path length = 2
 * String[] &#064;Foo []  // Annotates the array type String[]   - Path length = 1
 * String &#064;Foo [][]  // Annotates the array type String[][] - Path length = 0
 * </pre>
 * <br>
 * Example with nested types:
 * <pre>
 * &#064;Foo Outer.Middle.Inner // Path length = 0
 * Outer.&#064;Foo Middle.Inner // Path length = 1
 * Outer.Middle.&#064;Foo Inner // Path length = 2
 * </pre>
 * Example with different parts of the parameterized types:
 * <pre>
 * &#064;Foo Map&lt;String, Object&gt;
 * Map&lt;&#064;Foo String,Object&gt;
 * Map&lt;String,&#064;Foo Object&gt;
 *
 * List&lt;&#064;Foo ? extends String&gt;
 * List&lt;? extends &#064;Foo String&gt;
 * </pre>
 *
 * @author Matt Coley
 */
public class TypePath {
	private final List<TypePathElement> path;

	/**
	 * @param path
	 * 		Path elements.
	 */
	public TypePath(@Nonnull List<TypePathElement> path) {
		this.path = path;
	}

	/**
	 * @return Path elements.
	 */
	@Nonnull
	public List<TypePathElement> getPath() {
		return path;
	}

	/**
	 * @return Length of type path item.
	 */
	public int computeLength() {
		// u1: path_length + pathSize * (type_path_kind + type_argument_index)
		return 1 + (2 * getPath().size());
	}
}
