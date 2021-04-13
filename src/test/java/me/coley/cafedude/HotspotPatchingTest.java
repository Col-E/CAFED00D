package me.coley.cafedude;

import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.io.ClassFileWriter;
import org.junit.jupiter.api.Disabled;
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
 * Test that asserts hotspot-specific attribute obfuscated classes are written back without the offending attributes.
 */
public class HotspotPatchingTest {
	@Test
	//@Disabled
	public void testPatchIllegalLengthAttributes() {
		try {
			File root = new File("src/test/resources/samples/hotspot-obf");
			for (File sub : Objects.requireNonNull(root.listFiles())) {
				//if (!sub.getName().endsWith("16.class"))
				//	continue;
				byte[] code = Files.readAllBytes(sub.toPath());
				// Reading with ASM fails or produces incorrect representation...
				assertThrows(Exception.class, () -> {
					ClassReader cr = new ClassReader(code);
					cr.accept(new ClassNode(Opcodes.ASM9), 0);
				}, "Class does not break ASM: " + sub.getName());
				// Patch obfuscated class
				ClassFile cf = new ClassFileReader().read(code);
				byte[] modified = new ClassFileWriter().write(cf);
				// Reading with ASM works
				assertDoesNotThrow(() -> {
					ClassReader cr = new ClassReader(modified);
					cr.accept(new ClassNode(Opcodes.ASM9), 0);
				}, "Failure to patch class: " + sub.getName());
			}
		} catch (IOException e) {
			fail("Failed to read class, IO error", e);
		} catch (InvalidClassException e) {
			fail("Failed to read obfuscated class, invalid class", e);
		}
	}
}
