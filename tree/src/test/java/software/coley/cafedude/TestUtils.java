package software.coley.cafedude;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public class TestUtils {
	public static ClassNode node(byte[] bytes) {
		ClassNode node = new ClassNode();
		new ClassReader(bytes).accept(node, 0);
		return node;
	}

	public static byte[] node2code(ClassNode node) {
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}

	public static void verifyCode(byte[] bytes) {
		ClassNode node = node(bytes);
		Analyzer<BasicValue> a = new Analyzer<>(new BasicVerifier());
		for (MethodNode method : node.methods) {
			try {
				a.analyze(node.name, method);
			} catch (Exception e) {
				throw new RuntimeException("Failed to verify method: " + method.name + method.desc, e);
			}
		}
	}
}
