package software.coley.cafedude;

import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.Method;
import software.coley.cafedude.classfile.attribute.BootstrapMethodsAttribute;
import software.coley.cafedude.classfile.attribute.CodeAttribute;
import software.coley.cafedude.classfile.constant.ConstDynamic;
import software.coley.cafedude.classfile.constant.CpMethodRef;
import software.coley.cafedude.classfile.instruction.CpRefInstruction;
import software.coley.cafedude.classfile.instruction.Instruction;
import software.coley.cafedude.io.ClassBuilder;
import software.coley.cafedude.io.ClassFileReader;
import software.coley.cafedude.io.ClassFileWriter;
import software.coley.cafedude.io.FallbackInstructionReader;
import software.coley.cafedude.transform.IllegalRewritingInstructionsReader;
import software.coley.cafedude.transform.IllegalStrippingTransformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that asserts ASM-crashing obfuscated classes are written back without the offending attributes.
 */
public class CrasherPatchingTest {
	private static final Set<String> noverify_code = Set.of("sample35.class", "sample36.class", "sample48.class");

	@ParameterizedTest
	@MethodSource("supply")
	public void testPatch(File sub) {
		String name = sub.getName();
		try {
			byte[] code = Files.readAllBytes(sub.toPath());
			// Reading with ASM fails or produces incorrect representation...
			assertThrows(Throwable.class, () -> {
				ClassWriter cw = new TestClassWriter(ClassWriter.COMPUTE_FRAMES);
				ClassReader cr = new ClassReader(code);
				ClassNode node = new ClassNode(Opcodes.ASM9);
				cr.accept(node, 0);
				node.accept(cw);
			}, "Class does not break ASM: " + name);

			// Patch obfuscated class
			ClassFile cf = new ReservedOpcodeRewritingReader().read(code);
			new IllegalStrippingTransformer(cf).transform();
			byte[] modified = new ClassFileWriter().write(cf);

			// Reading with ASM works
			assertDoesNotThrow(() -> {
				// Don't do compute-frames if the sample contains unverifiable bytecode (stack height mismatching)
				int writerFlags = noverify_code.contains(name) ? 0 : ClassWriter.COMPUTE_FRAMES;
				ClassWriter cw = new TestClassWriter(writerFlags);
				ClassReader cr = new ClassReader(modified);
				ClassNode node = new ClassNode(Opcodes.ASM9);
				cr.accept(node, 0);
				node.accept(cw);
			}, "Failure to patch class: " + name);
		} catch (IOException e) {
			fail("Failed to read class, IO error", e);
		} catch (InvalidClassException e) {
			fail("Failed to read obfuscated class, invalid class", e);
		} catch (Exception ex) {
			fail("Unexpected error while testing class " + name, ex);
		}
	}

	@Test
	public void testSelectiveBootstrapMethodStripping() throws IOException, InvalidClassException {
		// Sample has a partially malformed bootstrap table, where one entry is a method reference instead of a method handle.
		// - CpMethodRef is not a loadable type and is thus an invalid bootstrap method argument.
		// - CpMethodHandle which is used in the other entries is valid and should be retained.
		File sample = new File("src/test/resources/samples/obfuscated/crasher-asm/sample59.class");
		ClassFile classFile = new ReservedOpcodeRewritingReader().read(Files.readAllBytes(sample.toPath()));
		BootstrapMethodsAttribute originalAttribute = classFile.getAttribute(BootstrapMethodsAttribute.class);
		assertNotNull(originalAttribute);
		List<BootstrapMethodsAttribute.BootstrapMethod> originalMethods = new ArrayList<>(originalAttribute.getBootstrapMethods());

		// Locate the malformed entry without depending on a hard-coded table position.
		int invalidIndex = -1;
		for (int i = 0; i < originalMethods.size(); i++) {
			if (originalMethods.get(i).getArgs().stream().anyMatch(CpMethodRef.class::isInstance)) {
				invalidIndex = i;
				break;
			}
		}
		assertTrue(invalidIndex >= 0, "Sample does not contain an invalid bootstrap entry");

		// Remember original dynamic indices by identity because filtering mutates their table indices.
		Map<ConstDynamic, Integer> originalDynamicIndices = new IdentityHashMap<>();
		int originalDynamicUses = 0;
		int invalidDynamicUses = 0;
		for (Method method : classFile.getMethods()) {
			CodeAttribute code = method.getAttribute(CodeAttribute.class);
			if (code == null)
				continue;
			for (Instruction instruction : code.getInstructions()) {
				if (instruction instanceof CpRefInstruction cpRef && cpRef.getEntry() instanceof ConstDynamic dynamic) {
					originalDynamicIndices.put(dynamic, dynamic.getBsmIndex());
					originalDynamicUses++;
					if (dynamic.getBsmIndex() == invalidIndex)
						invalidDynamicUses++;
				}
			}
		}

		// Run the stripping transformer. Should remove the illegal table entry while retaining the valid ones.
		new IllegalStrippingTransformer(classFile).transform();

		// Get the filtered table and confirm that the invalid entry was removed.
		BootstrapMethodsAttribute filteredAttribute = classFile.getAttribute(BootstrapMethodsAttribute.class);
		assertNotNull(filteredAttribute);
		List<BootstrapMethodsAttribute.BootstrapMethod> filteredMethods = filteredAttribute.getBootstrapMethods();
		assertEquals(originalMethods.size() - 1, filteredMethods.size());
		for (int originalIndex = 0, filteredIndex = 0; originalIndex < originalMethods.size(); originalIndex++) {
			if (originalIndex == invalidIndex)
				continue;
			assertSame(originalMethods.get(originalIndex), filteredMethods.get(filteredIndex++));
		}

		// Valid invokedynamic references retain their targets, while the malformed target is removed.
		int retainedDynamicUses = 0;
		for (Method method : classFile.getMethods()) {
			CodeAttribute code = method.getAttribute(CodeAttribute.class);
			if (code == null)
				continue;
			for (Instruction instruction : code.getInstructions()) {
				if (instruction instanceof CpRefInstruction cpRef && cpRef.getEntry() instanceof ConstDynamic dynamic) {
					Integer originalIndex = originalDynamicIndices.get(dynamic);
					assertNotNull(originalIndex, "Unknown dynamic constant survived transformation");
					assertNotEquals(invalidIndex, originalIndex);
					int expectedIndex = originalIndex > invalidIndex ? originalIndex - 1 : originalIndex;
					assertEquals(expectedIndex, dynamic.getBsmIndex());
					assertTrue(dynamic.getBsmIndex() < filteredMethods.size());
					retainedDynamicUses++;
				}
			}
		}
		assertEquals(originalDynamicUses - invalidDynamicUses, retainedDynamicUses);
	}

	@Test
	public void testMalformedBootstrapMethodRefRetained() throws IOException, InvalidClassException {
		// Sample60 has a BootstrapMethodsAttribute with a valid entry and a malformed entry
		// that references a CpUtf8 instead of a CpMethodHandle. This is separate from the previous test
		// since this check happens in the attribute reader, before the stripping transformer is run.
		File sample = new File("src/test/resources/samples/obfuscated/crasher-asm/sample60.class");
		ClassFile classFile = new ClassFileReader().read(Files.readAllBytes(sample.toPath()));

		// We should just drop the malformed entry. The valid one should stay.
		BootstrapMethodsAttribute attribute = classFile.getAttribute(BootstrapMethodsAttribute.class);
		assertNotNull(attribute, "BootstrapMethods attribute was dropped entirely");
		assertEquals(1, attribute.getBootstrapMethods().size(), "Only the valid entry should survive");
		assertEquals(9, attribute.getBootstrapMethods().get(0).getBsmMethodRef().getIndex());
	}

	/**
	 * @return Test files to check.
	 */
	public static List<File> supply() {
		List<File> files = new ArrayList<>();
		File root = new File("src/test/resources/samples/obfuscated/crasher-asm");
		for (File sub : Objects.requireNonNull(root.listFiles())) {
			if (sub.getName().endsWith(".class"))
				files.add(sub);
		}
		return files;
	}

	/**
	 * Class reader that supports rewriting reserved Hotspot VM instructions.
	 */
	private static class ReservedOpcodeRewritingReader extends ClassFileReader {
		IllegalRewritingInstructionsReader reader;

		@Nonnull
		@Override
		public FallbackInstructionReader getFallbackInstructionReader(@Nonnull ClassBuilder builder) {
			if (reader == null)
				reader = new IllegalRewritingInstructionsReader(builder.getPool(), builder.getVersionMajor());
			return reader;
		}
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
