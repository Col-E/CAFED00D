package me.coley.cafedude;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.io.ClassFileWriter;
import me.coley.cafedude.transform.IllegalStrippingTransformer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
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
			assertThrows(Exception.class, () -> {
				ClassReader cr = new ClassReader(code);
				cr.accept(new ClassNode(Opcodes.ASM9), 0);
			}, "Class does not break ASM: " + sub.getName());
			// Patch obfuscated class
			ClassFile cf = new ClassFileReader().read(code);
			new IllegalStrippingTransformer(cf).transform();
			byte[] modified = new ClassFileWriter().write(cf);
			// Reading with ASM works
			assertDoesNotThrow(() -> {
				ClassReader cr = new ClassReader(modified);
				cr.accept(new ClassNode(Opcodes.ASM9), 0);
			}, "Failure to patch class: " + sub.getName());
		} catch (IOException e) {
			fail("Failed to read class, IO error", e);
		} catch (InvalidClassException e) {
			fail("Failed to read obfuscated class, invalid class", e);
		}
	}

	public static List<File> supply() {
		List<File> files = new ArrayList<>();
		File root = new File("src/test/resources/samples/crasher");
		for (File sub : Objects.requireNonNull(root.listFiles())) {
			if (sub.getName().endsWith(".class"))
				files.add(sub);
		}
		return files;
	}
}
