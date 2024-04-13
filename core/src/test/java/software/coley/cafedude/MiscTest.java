package coley.software.cafedude;

import software.coley.cafedude.io.ClassFileReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Miscellaneous tests for checking that the library does not crash on specifically designed tests.
 */
public class MiscTest {
	@ParameterizedTest
	@MethodSource("supply")
	public void testNoErrors(File sample) {
		try {
			byte[] code = Files.readAllBytes(sample.toPath());
			assertDoesNotThrow(() -> {
				new ClassFileReader().read(code);
			}, "Library crashes when reading: " + sample.getName());
		} catch (IOException error) {
			fail("Failed to read class, IO error", error);
		}
	}

	/**
	 * @return Test class files.
	 */
	public static List<File> supply() {
		// Intention may be to add more files in the future, if more crashes are found.
		return Arrays.asList(new File[] { new File("src/test/resources/samples/obfuscated/misc/fakecode.class") });
	}
}