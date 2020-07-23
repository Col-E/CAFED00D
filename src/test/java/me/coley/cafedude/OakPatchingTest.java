package me.coley.cafedude;

import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.io.ClassFileWriter;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that asserts oak classes are written back with current jvm-spec compliant formatting.
 */
public class OakPatchingTest {
	@Test
	public void testPatchOakInconsistencies() {
		try {
			File root = new File("src/test/resources/samples/oak");
			for (File sub : Objects.requireNonNull(root.listFiles())) {
				byte[] code = Files.readAllBytes(sub.toPath());
				// Reading with ASM fails or produces incorrect representation...
				// TODO: Refactor test to be individually.
				//       "sample1" crashes ASM
				//       "sample2" just yields wrong code
				//      assertThrows(Exception.class, () -> {
				//      	ClassReader cr = new ClassReader(modified);
				//      	cr.accept(new ClassNode(Opcodes.ASM8), 0);
				//      });
				// Patch oak class
				ClassFile cf = new ClassFileReader().read(code);
				byte[] modified = new ClassFileWriter().write(cf);
				// Reading with ASM works
				assertDoesNotThrow(() -> {
					ClassReader cr = new ClassReader(modified);
					cr.accept(new ClassNode(Opcodes.ASM8), 0);
				});
			}
		} catch (IOException e) {
			fail("Failed to read class, IO error", e);
		} catch (InvalidClassException e) {
			fail("Failed to read oak class, invalid class", e);
		}
	}
}
