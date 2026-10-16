package software.coley.cafedude;

import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpDouble;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpFloat;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.io.ClassFileReader;
import software.coley.cafedude.io.ClassFileWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests that float and double pool entries keep their exact bit patterns, including NaN sign and payload.
 */
public class FloatingPointConstantTest {
	/** Negative quiet NaN, the value x86 hardware produces for {@code 0.0 / 0.0}. */
	private static final int FLOAT_NAN_NEGATIVE = 0xffc00000;
	/** Positive quiet NaN with a non-zero payload. */
	private static final int FLOAT_NAN_PAYLOAD = 0x7fc00001;
	/** Positive quiet NaN with a non-zero payload. */
	private static final long DOUBLE_NAN_PAYLOAD = 0x7ff8000000000001L;

	@Test
	public void testNanBitsSurviveWriteThenRead() throws Exception {
		ConstPool pool = new ConstPool();
		CpUtf8 name = new CpUtf8("Nan");
		pool.add(name);
		CpClass thisClass = new CpClass(name);
		pool.add(thisClass);
		CpFloat negativeNan = new CpFloat(Float.intBitsToFloat(FLOAT_NAN_NEGATIVE));
		pool.add(negativeNan);
		CpFloat payloadNan = new CpFloat(Float.intBitsToFloat(FLOAT_NAN_PAYLOAD));
		pool.add(payloadNan);
		CpDouble doubleNan = new CpDouble(Double.longBitsToDouble(DOUBLE_NAN_PAYLOAD));
		pool.add(doubleNan);
		ClassFile clazz = new ClassFile(0, 65, pool, 0x21, thisClass, null,
				List.of(), List.of(), List.of(), List.of());

		byte[] written = new ClassFileWriter().write(clazz);
		ClassFile read = new ClassFileReader().read(written);

		List<Float> floats = new ArrayList<>();
		List<Double> doubles = new ArrayList<>();
		for (CpEntry entry : read.getPool()) {
			if (entry instanceof CpFloat cpFloat) floats.add(cpFloat.getValue());
			if (entry instanceof CpDouble cpDouble) doubles.add(cpDouble.getValue());
		}
		assertEquals(List.of(FLOAT_NAN_NEGATIVE, FLOAT_NAN_PAYLOAD),
				floats.stream().map(Float::floatToRawIntBits).toList());
		assertEquals(List.of(DOUBLE_NAN_PAYLOAD),
				doubles.stream().map(Double::doubleToRawLongBits).toList());
	}

	@Test
	public void testNanPayloadsAreDistinctPoolEntries() {
		assertNotEquals(CpFloat.fromRawBits(FLOAT_NAN_NEGATIVE), CpFloat.fromRawBits(FLOAT_NAN_PAYLOAD));
		assertNotEquals(CpDouble.fromRawBits(DOUBLE_NAN_PAYLOAD), CpDouble.fromRawBits(0x7ff8000000000000L));

		ConstPool pool = new ConstPool();
		pool.add(CpFloat.fromRawBits(FLOAT_NAN_NEGATIVE));
		assertEquals(-1, pool.indexOf(CpFloat.fromRawBits(FLOAT_NAN_PAYLOAD)));
		assertEquals(1, pool.indexOf(CpFloat.fromRawBits(FLOAT_NAN_NEGATIVE)));
	}

	@Test
	public void testSetValueKeepsEqualityAndHashConsistent() {
		CpFloat entry = CpFloat.fromRawBits(FLOAT_NAN_PAYLOAD);
		entry.setValue(Float.intBitsToFloat(FLOAT_NAN_NEGATIVE));
		CpFloat expected = CpFloat.fromRawBits(FLOAT_NAN_NEGATIVE);
		assertEquals(expected, entry);
		assertEquals(expected.hashCode(), entry.hashCode());
		assertEquals(FLOAT_NAN_NEGATIVE, entry.getRawBits());
	}
}
