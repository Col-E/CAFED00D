package software.coley.cafedude;

import org.junit.jupiter.api.Test;
import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.Method;
import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.attribute.CodeAttribute;
import software.coley.cafedude.io.ClassFileReader;
import software.coley.cafedude.io.ClassFileWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that class files with duplicate attributes are normalized <i>(Keeping only one copy when expected)</i> when read and written.
 */
public class AttributeDedupeTest {
	@Test
	public void writesFixtureWithDedupedAttributes() throws IOException, InvalidClassException {
		ClassFile input = dupeAttrClass();

		byte[] written = new ClassFileWriter().write(input);
		ClassFile output = new ClassFileReader().read(written);
		Method outputMethod = findMethodWithCode(output);
		CodeAttribute outputCode = outputMethod.getAttribute(CodeAttribute.class);

		// Verify that the duplicate attributes were deduped when written and read back.
		assertEquals(1, countNamed(output.getAttributes(), "SourceFile"));
		assertEquals(1, countNamed(outputMethod.getAttributes(), "Code"));

		// Surprisingly you are allowed to have multiple copies of these attributes, so we don't dedupe them.
		assertEquals(2, countNamed(outputCode.getAttributes(), "LineNumberTable"));
		assertEquals(2, countNamed(outputCode.getAttributes(), "LocalVariableTable"));
	}

	private static ClassFile dupeAttrClass() throws IOException, InvalidClassException {
		byte[] code = Files.readAllBytes(new File("src/test/resources/samples/obfuscated/misc/DuplicateAttributeFixture.class").toPath());
		return new ClassFileReader().read(code);
	}

	private static Method findMethodWithCode(ClassFile classFile) {
		return classFile.getMethods().stream()
				.filter(method -> method.getAttribute(CodeAttribute.class) != null)
				.findFirst()
				.orElseThrow(() -> new AssertionError("Fixture has no Code attribute"));
	}

	private static int countNamed(Iterable<Attribute> attributes, String name) {
		int count = 0;
		for (Attribute attribute : attributes)
			if (attribute.getName().getText().equals(name))
				count++;
		return count;
	}
}
