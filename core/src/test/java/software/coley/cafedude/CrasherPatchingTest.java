package software.coley.cafedude;

import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.io.ClassFileReader;
import software.coley.cafedude.io.ClassFileWriter;
import software.coley.cafedude.transform.IllegalStrippingTransformer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that asserts ASM-crashing obfuscated classes are written back without the offending attributes.
 */
public class CrasherPatchingTest {
	@ParameterizedTest
	@MethodSource("supply")
	public void testPatch(File sub) {
		try {
			byte[] code = Files.readAllBytes(sub.toPath());

			// Reading with ASM fails or produces incorrect representation...
			assertThrows(Throwable.class, () -> {
				ClassWriter cw = new TestClassWriter(ClassWriter.COMPUTE_FRAMES);
				ClassReader cr = new ClassReader(code);
				ClassNode node = new ClassNode(Opcodes.ASM9);
				cr.accept(node, 0);
				node.accept(cw);
			}, "Class does not break ASM: " + sub.getName());

			// Patch obfuscated class
			ClassFile cf = new ClassFileReader().read(code);
			new IllegalStrippingTransformer(cf).transform();
			byte[] modified = new ClassFileWriter().write(cf);

			// Reading with ASM works
			assertDoesNotThrow(() -> {
				ClassWriter cw = new TestClassWriter(ClassWriter.COMPUTE_FRAMES);
				ClassReader cr = new ClassReader(modified);
				ClassNode node = new ClassNode(Opcodes.ASM9);
				cr.accept(node, 0);
				node.accept(cw);
			}, "Failure to patch class: " + sub.getName());
		} catch (IOException e) {
			fail("Failed to read class, IO error", e);
		} catch (InvalidClassException e) {
			fail("Failed to read obfuscated class, invalid class", e);
		} catch (Exception ex) {
			fail("Unexpected error while testing class " + sub.getName(), ex);
		}
	}

	/**
	 * @return Test files to check.
	 */
	public static List<File> supply() {
		List<File> files = new ArrayList<>();
		File root = new File("src/test/resources/samples/obfuscated/crasher");
		for (File sub : Objects.requireNonNull(root.listFiles())) {
			if (sub.getName().endsWith(".class"))
				files.add(sub);
		}
		return files;
	}

	/**
	 * Dummy writer that doesn't do runtime look-ups for common type analysis.
	 */
	private static class TestClassWriter extends ClassWriter {
		public TestClassWriter(int flags) {
			super(flags);
		}

		@Override
		protected String getCommonSuperClass(String type1, String type2) {
			// We don't care about the correctness of types in the frames.
			// For these tests we just want to know if the frame computation logic can be run successfully.
			return "java/lang/Object";
		}
	}
}
