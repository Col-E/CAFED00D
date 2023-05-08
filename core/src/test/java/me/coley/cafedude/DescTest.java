package me.coley.cafedude;

import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.Descriptor.Kind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Descriptor parsing tests.
 */
public class DescTest {
	@Test
	public void testParseMethodParams() {
		assertEquals(0, Descriptor.from("()V").getParameterCount());
		assertEquals(1, Descriptor.from("(I)V").getParameterCount());
		assertEquals(1, Descriptor.from("(Ljava/lang/String;)V").getParameterCount());
		assertEquals(1, Descriptor.from("([I)V").getParameterCount());
		assertEquals(1, Descriptor.from("([[I)V").getParameterCount());
		assertEquals(2, Descriptor.from("(II)V").getParameterCount());
		assertEquals(2, Descriptor.from("(I[I)V").getParameterCount());
		assertEquals(2, Descriptor.from("([I[I)V").getParameterCount());
		assertEquals(2, Descriptor.from("(Ljava/lang/String;Ljava/lang/String;)V").getParameterCount());
		assertEquals(2, Descriptor.from("(Ljava/lang/String;[Ljava/lang/String;)V").getParameterCount());
		assertEquals(2, Descriptor.from("([Ljava/lang/String;[Ljava/lang/String;)V").getParameterCount());
		assertEquals(2, Descriptor.from("(Ljava/lang/String;I)V").getParameterCount());
		assertEquals(2, Descriptor.from("(I[Ljava/lang/String;)V").getParameterCount());
		assertEquals(2, Descriptor.from("([BLjava/lang/String;)Ljava/util/List;").getParameterCount());
		assertEquals(3, Descriptor.from("([BLjava/lang/String;[B)Ljava/util/List;").getParameterCount());
		assertEquals(3, Descriptor.from("([Ljava/lang/String;I[Ljava/lang/String;)V").getParameterCount());
		assertEquals(4, Descriptor.from("([BLjava/lang/String;[B[B)Ljava/util/List;").getParameterCount());
		assertEquals(4, Descriptor.from("([BLjava/lang/String;[BB)Ljava/util/List;").getParameterCount());
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
		assertNotEquals(Kind.ILLEGAL, d.getKind());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"()V",
			"(I)V",
			"(Ljava/lang/String;)V",
			"(Ljava/lang/String;)Ljava/lang/String;",
			"(Ljava/lang/String;I)V",
			"(ILjava/lang/String;)V",
			"([I[I[I)[[[I",
	})
	public void testParseMethods(String desc) {
		Descriptor d = Descriptor.from(desc);
		assertEquals(Kind.METHOD, d.getKind());
	}

	@ParameterizedTest
	@ValueSource(strings = {
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
		assertEquals(Kind.ILLEGAL, d.getKind());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			" ",
			"\t",
			"\n"
	})
	public void testParseEmpty(String desc) {
		assertNull(Descriptor.from(desc));
	}
}
