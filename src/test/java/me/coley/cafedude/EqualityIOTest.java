package me.coley.cafedude;

import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.io.ClassFileWriter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that asserts the classes that are read and written back to are identical.
 */
public class EqualityIOTest {
	private File lastLoaded;
	private int successes;

	@Test
	public void testSamples() {
		try {
			visitRootDir("samples/annos");
			visitRootDir("samples/javac");
		} catch (IOException e) {
			System.err.println(lastLoaded.getPath());
			fail("Failed to read class, IO error", e);
		} catch (InvalidClassException e) {
			System.err.println(lastLoaded.getPath());
			fail("Failed to read class, invalid class", e);
		}
		System.out.println("Successes: " + successes);
	}

	private void assertReadWriteEquality(byte[] code) throws InvalidClassException, IOException {
		ClassFile cf = new ClassFileReader().read(code);
		byte[] out = new ClassFileWriter().write(cf);
		assertEquals(code.length, out.length, "Class difference for: " + cf.getName());
		assertArrayEquals(code, out);
	}

	private void visitRootDir(String dir) throws IOException, InvalidClassException {
		File root = new File("src/test/resources/" + dir);
		visit(root);
	}

	private void visit(File file) throws IOException, InvalidClassException {
		if (file.isDirectory()) {
			for (File sub : Objects.requireNonNull(file.listFiles()))
				visit(sub);
		} else if (file.getName().endsWith(".class")) {
			lastLoaded = file;
			byte[] code = Files.readAllBytes(file.toPath());
			assertReadWriteEquality(code);
			successes++;
		}
	}
}
