package software.coley.cafedude;

import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.io.ClassFileReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.coley.cafedude.io.ClassFileWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Miscellaneous tests for checking that the library does not crash on specifically crafted classes.
 * These also serve as regression tests for ensuring we support obfuscated input that doesn't strictly
 * aim to crash ASM as covered by {@link CrasherPatchingTest}
 */
public class RegressionTests {
	@ParameterizedTest
	@MethodSource("supply")
	public void testNoErrors(File sample) {
		try {
			byte[] code = Files.readAllBytes(sample.toPath());
			assertDoesNotThrow(() -> {
				ClassFile classFile = new ClassFileReader().read(code);
				new ClassFileWriter().write(classFile);
			}, "Library crashes when reading: " + sample.getName());
		} catch (IOException error) {
			fail("Failed to read class, IO error", error);
		}
	}

	/**
	 * @return Test class files.
	 */
	public static List<File> supply() {
		List<File> files = new ArrayList<>();
		File root = new File("src/test/resources/samples/obfuscated/crasher-dude");
		for (File sub : Objects.requireNonNull(root.listFiles())) {
			if (sub.getName().endsWith(".class"))
				files.add(sub);
		}
		return files;	}
}