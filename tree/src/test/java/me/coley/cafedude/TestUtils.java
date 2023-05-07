package me.coley.cafedude;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {

	public static ClassNode node(byte[] bytes) {
		ClassNode node = new ClassNode();
		new ClassReader(bytes).accept(node, 0);
		return node;
	}

	public static void verifyCode(byte[] bytes) {
		try {
			Files.write(Paths.get("test-results/debug.class"), bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
