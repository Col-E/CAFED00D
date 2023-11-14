package software.coley.cafedude;

import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.io.ClassFileReader;
import software.coley.cafedude.io.ClassFileWriter;
import software.coley.cafedude.transform.IllegalStrippingTransformer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that asserts the classes that are read and written back to are identical.
 */
public class EqualityIOTest {
	@ParameterizedTest
	@MethodSource("supplyResourceClasses")
	public void testEqualityNormal(Path sub) {
		test(sub);
	}

	@ParameterizedTest
	@MethodSource("supplySelfClasses")
	public void testEqualitySelf(Path sub) {
		test(sub);
	}

	private void test(Path sub) {
		try {
			byte[] code = Files.readAllBytes(sub);
			ClassFile cf = new ClassFileReader().read(code);
			new IllegalStrippingTransformer(cf).transform();
			byte[] out = new ClassFileWriter().write(cf);
			assertEquals(code.length, out.length, "Class difference for: " + cf.getName());
			assertArrayEquals(code, out);
		} catch (IOException e) {
			fail("Failed to read class, IO error", e);
		} catch (InvalidClassException e) {
			fail("Failed to read class, invalid class", e);
		}
	}

	/**
	 * @return Test files to check.
	 */
	public static List<Path> supplyResourceClasses() {
		try {
			BiPredicate<Path, BasicFileAttributes> filter =
					(path, attrib) -> attrib.isRegularFile() && path.toString().endsWith(".class");
			return Files.find(Paths.get("src/test/resources/samples/normal"), 25, filter)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return Test files to check.
	 */
	public static List<Path> supplySelfClasses() {
		try {
			BiPredicate<Path, BasicFileAttributes> filter =
					(path, attrib) -> attrib.isRegularFile() && path.toString().endsWith(".class");
			return Files.find(Paths.get("target/classes"), 25, filter)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
