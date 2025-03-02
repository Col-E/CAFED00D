package software.coley.cafedude;

import software.coley.cafedude.classfile.Descriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.annotation.Nonnull;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Descriptor parsing tests.
 */
public class DescTest {
	@Test
	public void testParseMethodParams() {
		assertParameterCount(0, "()V");
		assertParameterCount(1, "(I)V");
		assertParameterCount(1, "(Ljava/lang/String;)V");
		assertParameterCount(1, "([Ljava/lang/String;)V");
		assertParameterCount(1, "([I)V");
		assertParameterCount(1, "([[I)V");
		assertParameterCount(2, "(II)V");
		assertParameterCount(2, "(I[I)V");
		assertParameterCount(2, "([I[I)V");
		assertParameterCount(2, "(Ljava/lang/String;Ljava/lang/String;)V");
		assertParameterCount(2, "(Ljava/lang/String;[Ljava/lang/String;)V");
		assertParameterCount(2, "([Ljava/lang/String;[Ljava/lang/String;)V");
		assertParameterCount(2, "(Ljava/lang/String;I)V");
		assertParameterCount(2, "(I[Ljava/lang/String;)V");
		assertParameterCount(2, "([BLjava/lang/String;)Ljava/util/List;");
		assertParameterCount(3, "([BLjava/lang/String;[B)Ljava/util/List;");
		assertParameterCount(3, "([Ljava/lang/String;I[Ljava/lang/String;)V");
		assertParameterCount(4, "([BLjava/lang/String;[B[B)Ljava/util/List;");
		assertParameterCount(4, "([BLjava/lang/String;[BB)Ljava/util/List;");
	}

	private static void assertParameterCount(int expected, @Nonnull String desc) {
		Descriptor descriptor = Descriptor.from(desc);
		assertEquals(expected, descriptor.getParameterCount());
		List<Descriptor> parameters = descriptor.getParameters();
		assertEquals(expected, parameters.size());
		for (Descriptor parameter : parameters)
			if (parameter.getKind() == Descriptor.Kind.ILLEGAL)
				fail("Illegal parameter: " + desc + " ---> " + parameter);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"I",
			"Ljava/lang/String;",
			"[I",
			"[Ljava/lang/String;"
	})
	public void testParseFields(String desc) {
		Descriptor d = Descriptor.from(desc);
		Assertions.assertNotEquals(Descriptor.Kind.ILLEGAL, d.getKind());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"([LLL;)V",
			"([L ;)V",
			"([L\0;)V",
			"(I)V",
			"(Ljava/lang/String;)V",
			"(Ljava/lang/String;)Ljava/lang/String;",
			"(Ljava/lang/String;I)V",
			"(ILjava/lang/String;)V",
			"([I[I[I)[[[I",
	})
	public void testParseMethods(String desc) {
		Descriptor d = Descriptor.from(desc);
		Assertions.assertEquals(Descriptor.Kind.METHOD, d.getKind());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			" ",
			"\t",
			"\n",
			"K",
			"[",
			"[[K",
			"[[L;",
			"L;",
			"()",
			"(",
			"()[",
			"([)V",
			"([K)V",
			"([L)V",
			"([L;)V",
			"(L)V",
			"(L;)V",
			"(K)V",
	})
	public void testParseIllegal(String desc) {
		Descriptor d = Descriptor.from(desc);
		assertEquals(Descriptor.Kind.ILLEGAL, d.getKind());
	}
}
