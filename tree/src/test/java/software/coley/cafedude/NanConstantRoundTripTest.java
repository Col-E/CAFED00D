package software.coley.cafedude;

import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.Field;
import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.attribute.ConstantValueAttribute;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpDouble;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpFloat;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.io.ClassFileReader;
import software.coley.cafedude.io.ClassFileWriter;
import software.coley.cafedude.tree.visitor.reader.ClassReader;
import software.coley.cafedude.tree.visitor.writer.ClassWriter;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that NaN constant values keep their exact bits when a class is round-tripped through the tree API.
 */
public class NanConstantRoundTripTest {
	/** Negative quiet NaN, the value x86 hardware produces for {@code 0.0 / 0.0}. */
	private static final int FLOAT_NAN_NEGATIVE = 0xffc00000;
	/** Positive quiet NaN with a non-zero payload. */
	private static final int FLOAT_NAN_PAYLOAD = 0x7fc00001;
	/** Positive quiet NaN with a non-zero payload. */
	private static final long DOUBLE_NAN_PAYLOAD = 0x7ff8000000000001L;

	@Test
	public void testNanConstantValuesSurviveTreeRoundTrip() throws Exception {
		ConstPool pool = new ConstPool();
		CpUtf8 className = new CpUtf8("Nan");
		pool.add(className);
		CpClass thisClass = new CpClass(className);
		pool.add(thisClass);
		CpUtf8 constantValueName = new CpUtf8("ConstantValue");
		pool.add(constantValueName);

		List<Field> fields = List.of(
				constantField(pool, "negative", "F", constantValueName, CpFloat.fromRawBits(FLOAT_NAN_NEGATIVE)),
				constantField(pool, "payload", "F", constantValueName, CpFloat.fromRawBits(FLOAT_NAN_PAYLOAD)),
				constantField(pool, "wide", "D", constantValueName, CpDouble.fromRawBits(DOUBLE_NAN_PAYLOAD)));
		ClassFile input = new ClassFile(0, 65, pool, 0x21, thisClass, null,
				List.of(), fields, List.of(), List.of());
		ClassFile parsed = new ClassFileReader().read(new ClassFileWriter().write(input));

		ClassWriter writer = new ClassWriter(parsed.getVersionMajor(), parsed.getVersionMinor());
		new ClassReader(parsed).accept(writer);
		ClassFile output = new ClassFileReader().read(writer.toByteArray());

		Set<Integer> floatBits = new HashSet<>();
		Set<Long> doubleBits = new HashSet<>();
		for (CpEntry entry : output.getPool()) {
			if (entry instanceof CpFloat cpFloat) floatBits.add(cpFloat.getRawBits());
			if (entry instanceof CpDouble cpDouble) doubleBits.add(cpDouble.getRawBits());
		}
		assertEquals(Set.of(FLOAT_NAN_NEGATIVE, FLOAT_NAN_PAYLOAD), floatBits);
		assertEquals(Set.of(DOUBLE_NAN_PAYLOAD), doubleBits);
	}

	private static Field constantField(ConstPool pool, String name, String descriptor,
	                                   CpUtf8 attributeName, CpEntry value) {
		CpUtf8 nameEntry = new CpUtf8(name);
		pool.add(nameEntry);
		CpUtf8 descriptorEntry = new CpUtf8(descriptor);
		pool.add(descriptorEntry);
		pool.add(value);
		List<Attribute> attributes = List.of(new ConstantValueAttribute(attributeName, value));
		// public static final
		return new Field(attributes, 0x0019, nameEntry, descriptorEntry);
	}
}
