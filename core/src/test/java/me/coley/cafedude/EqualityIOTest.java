package me.coley.cafedude;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.io.ClassFileWriter;
import me.coley.cafedude.transform.IllegalStrippingTransformer;
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
	@MethodSource("supply")
	public void testEquality(Path sub) {
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
	public static List<Path> supply() {
		try {
			BiPredicate<Path, BasicFileAttributes> filter =
					(path, attrib) -> attrib.isRegularFile() && path.toString().endsWith(".class");
			return Files.find(Paths.get("src/test/resources/samples/normal"), 25, filter)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
