package me.coley.cafedude;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.tree.visitor.reader.ClassReader;
import me.coley.cafedude.tree.visitor.writer.ClassWriter;
import org.junit.jupiter.api.Assertions;
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

import static org.junit.jupiter.api.Assertions.fail;

public class WriterTest {

	@ParameterizedTest
	@MethodSource("supply")
	public void testVisitClass(Path file) {
		try {
			byte[] code = Files.readAllBytes(file);
			ClassFile classFile = new ClassFileReader().read(code);
			ClassWriter writer = new ClassWriter(classFile.getVersionMajor(), classFile.getVersionMinor());
			ClassReader reader = new ClassReader(classFile);
			reader.accept(writer);
			Assertions.assertEquals(code.length, writer.toByteArray().length);
		} catch (IOException e) {
			fail("Failed to read class, IO error", e);
		} catch (InvalidClassException e) {
			fail("Failed to compare written output, invalid class", e);
		} catch (Exception ex) {
			fail("Unexpected error while testing class " + file, ex);
		}
	}

	public static List<Path> supply() {
		try {
			BiPredicate<Path, BasicFileAttributes> filter =
					(path, attrib) -> attrib.isRegularFile() && path.toString().endsWith(".class");
			return Files.find(Paths.get("src/test/resources/samples/normal/"), 25, filter)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
