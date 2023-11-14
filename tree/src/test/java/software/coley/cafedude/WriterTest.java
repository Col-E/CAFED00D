package software.coley.cafedude;

import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.io.ClassFileReader;
import software.coley.cafedude.tree.visitor.reader.ClassReader;
import software.coley.cafedude.tree.visitor.writer.ClassWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

public class WriterTest extends TestUtils {
	private static boolean debug = true;

	@ParameterizedTest
	@MethodSource("supply")
	public void testVisitClass(Path file) {
		try {
			byte[] code = Files.readAllBytes(file);
			ClassFile classFile = new ClassFileReader().read(code);

			// Tree API
			ClassWriter writer = new ClassWriter(classFile.getVersionMajor(), classFile.getVersionMinor());
			ClassReader reader = new ClassReader(classFile);
			reader.accept(writer);
			byte[] writtenCode = writer.toByteArray();

			ClassNode nodeIn = node(code);
			ClassNode nodeOut = node(writtenCode);

			// TODO: Our rewrite has less labels, and missing LineNumberTable entries in the code

			// Unlike 'core' we are not trying to be faithful to the original model, minus junk elements.
			// There are cases where 'javac' will create duplicate CpEntry items.
			// Thus, checking inLength == outLength will not work here.
			if (debug && code.length != writtenCode.length) {
				AbstractInsnNode[] insnsIn = nodeIn.methods.get(1).instructions.toArray();
				AbstractInsnNode[] insnsOut = nodeOut.methods.get(1).instructions.toArray();
				for (int i = 0; i < Math.min(insnsIn.length, insnsOut.length); i++) {
					AbstractInsnNode in = insnsIn[i];
					AbstractInsnNode out = insnsOut[i];
					if (in.getOpcode() != out.getOpcode()) {
						fail("Mismatch opcode at index " + i);
					}
				}

				for (int i = 0; i < Math.min(code.length, writtenCode.length); i++) {
					if (code[i] != writtenCode[i]) {
						fail("Mismatch in " + file.getFileName() + " at index " + i);
					}
				}
			}
		} catch (IOException e) {
			fail("Failed to read class, IO error", e);
		} catch (InvalidClassException e) {
			fail("Failed to compare written output, invalid class", e);
		} catch (Exception ex) {
			fail("Unexpected error while testing class ", ex);
		}
	}

	public static List<Path> supply() {
		try {
			BiPredicate<Path, BasicFileAttributes> filter =
					(path, attrib) -> attrib.isRegularFile() && path.toString().endsWith(".class");
			return Files.find(Paths.get("src/test/resources/samples/normal/javac"), 25, filter)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
