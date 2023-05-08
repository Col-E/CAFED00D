package me.coley.cafedude;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.transform.IllegalStrippingTransformer;
import me.coley.cafedude.tree.visitor.*;
import me.coley.cafedude.tree.visitor.reader.ClassReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

public class VisitorTest {
	@ParameterizedTest
	@MethodSource("supply")
	public void testVisitClass(Path file) {
		try {
			byte[] code = Files.readAllBytes(file);
			VerificationClassVisitor visitor = new VerificationClassVisitor();
			ClassFile classFile = new ClassFileReader().read(code);
			new IllegalStrippingTransformer(classFile).transform();
			ClassReader reader = new ClassReader(classFile);
			reader.accept(visitor);
		} catch (IOException e) {
			fail("Failed to read class, IO error", e);
		} catch (InvalidClassException e) {
			fail("Failed to read obfuscated class, invalid class", e);
		} catch (Exception ex) {
			fail("Unexpected error while testing class " + file, ex);
		}
	}

	public static List<Path> supply() {
		try {
			BiPredicate<Path, BasicFileAttributes> filter =
					(path, attrib) -> attrib.isRegularFile() && path.toString().endsWith(".class");
			return Files.find(Paths.get("src/test/resources/samples/"), 25, filter)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static class VerificationClassVisitor implements ClassVisitor {
		@Override
		public void visitClass(@Nonnull String name, int access, @Nullable String superName, String... interfaces) {
			System.out.println("Class: " + name);
			System.out.println("Access: " + access);
			System.out.println("Super: " + superName);
			System.out.println("Interfaces: " + Arrays.toString(interfaces));
		}

		@Override
		public MethodVisitor visitMethod(@Nonnull String name, int access, @Nonnull Descriptor descriptor) {
			System.out.println("Method: " + name);
			System.out.println("Access: " + access);
			System.out.printf("Descriptor: [%s] kind: %s\n", descriptor.getDescriptor(), descriptor.getKind());
			return new MethodVisitor() {};
		}

		@Override
		public FieldVisitor visitField(@Nonnull String name, int access, @Nonnull Descriptor descriptor) {
			System.out.println("Field: " + name);
			System.out.println("Access: " + access);
			System.out.printf("Descriptor: [%s] kind: %s\n", descriptor.getDescriptor(), descriptor.getKind());
			return new FieldVisitor() {
			};
		}
	}
}
