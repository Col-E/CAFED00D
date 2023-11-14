package software.coley.cafedude;

import software.coley.cafedude.classfile.Descriptor;
import software.coley.cafedude.classfile.VersionConstants;
import software.coley.cafedude.classfile.instruction.Opcodes;
import software.coley.cafedude.tree.Constant;
import software.coley.cafedude.tree.Label;
import software.coley.cafedude.tree.visitor.CodeVisitor;
import software.coley.cafedude.tree.visitor.writer.ClassWriter;
import software.coley.cafedude.tree.visitor.writer.MethodWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.fail;

public class CodeTests implements Opcodes {

	private CodeVisitor code;
	private MethodWriter method;
	private ClassWriter clazz;
	private Class<? extends Exception> expectedException;

	@BeforeEach
	public void setup() {
		this.clazz = new ClassWriter(VersionConstants.JAVA8, VersionConstants.JAVA8);
		this.clazz.visitClass("me/coley/cafedude/CodeTests", 0, "java/lang/Object");
		this.method = (MethodWriter) clazz.visitMethod("test", 0, Descriptor.from("()V"));
		this.code = method.visitCode();
	}

	@AfterEach
	public void verify() {
		try {
			code.visitCodeEnd();
			method.visitMethodEnd();
			clazz.visitClassEnd();
			TestUtils.verifyCode(clazz.toByteArray());
		} catch (Exception e) {
			if (expectedException == null || !expectedException.isInstance(e)) {
				fail("Failed to verify code", e);
			} else {
				expectedException = null;
			}
		}
		if(expectedException != null)
			fail("Expected exception: " + expectedException.getSimpleName());
	}

	@Test
	public void testSimple() {
		code.visitReturnInsn(RETURN);
		code.visitMaxs(0, 1);
	}

	@Test
	public void testIntOp() {
		code.visitIntInsn(BIPUSH, 1);
		code.visitIntInsn(BIPUSH, 2);
		code.visitStackInsn(IADD);
		code.visitReturnInsn(RETURN);
		code.visitMaxs(2, 1);
	}

	@Test
	public void testLocalVar() {
		code.visitIntInsn(BIPUSH, 1);
		code.visitVarInsn(ISTORE, 0);
		code.visitVarInsn(ILOAD, 0);
		code.visitStackInsn(RETURN);
		code.visitMaxs(1, 2);
	}

	@Test
	public void testSmall() {
		code.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
				Descriptor.from(PrintStream.class));
		code.visitLdcInsn(Constant.of("Hello, World!"));
		code.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
				Descriptor.from("(Ljava/lang/String;)V"));
		code.visitReturnInsn(RETURN);
		code.visitMaxs(2, 1);
	}

	@Test
	public void testFlow() {
		Label start = new Label();
		Label end = new Label();
		code.visitLabel(start);
		code.visitConstantInsn(ICONST_1);
		code.visitConstantInsn(ICONST_1);
		code.visitFlowInsn(IF_ICMPEQ, end);
		code.visitConstantInsn(ICONST_2);
		code.visitStackInsn(POP);
		code.visitLabel(end);
		code.visitStackInsn(RETURN);
		code.visitMaxs(2, 1);
	}

	@Test
	public void testUnresolvedLabel() {
		Label start = new Label();
		Label end = new Label();
		code.visitLabel(start);
		code.visitConstantInsn(ICONST_1);
		code.visitConstantInsn(ICONST_1);
		code.visitFlowInsn(IF_ICMPEQ, end);
		code.visitConstantInsn(ICONST_2);
		//code.visitLabel(end); (unresolved)
		code.visitStackInsn(RETURN);
		code.visitMaxs(2, 1);
		expectedException = UnresolvedLabelException.class;
	}

	@Test
	public void testSwitch() {
		Label case1 = new Label();
		Label case2 = new Label();
		Label defaultCase = new Label();
		Label end = new Label();
		code.visitConstantInsn(ICONST_1);
		code.visitLookupSwitchInsn(defaultCase, new int[]{1, 2}, case1, case2);
		code.visitLabel(case1);
		code.visitConstantInsn(ICONST_1);
		code.visitFlowInsn(GOTO, end);
		code.visitLabel(case2);
		code.visitConstantInsn(ICONST_2);
		code.visitFlowInsn(GOTO, end);
		code.visitLabel(defaultCase);
		code.visitConstantInsn(ICONST_3);
		code.visitLabel(end);
		code.visitStackInsn(POP);
		code.visitStackInsn(RETURN);
		code.visitMaxs(1, 1);
	}

	@Test
	public void testTableSwitch() {
		Label case1 = new Label();
		Label case2 = new Label();
		Label defaultCase = new Label();
		Label end = new Label();
		code.visitConstantInsn(ICONST_1);
		code.visitTableSwitchInsn(1, 2, defaultCase, case1, case2);
		code.visitLabel(case1);
		code.visitConstantInsn(ICONST_1);
		code.visitFlowInsn(GOTO, end);
		code.visitLabel(case2);
		code.visitConstantInsn(ICONST_2);
		code.visitFlowInsn(GOTO, end);
		code.visitLabel(defaultCase);
		code.visitConstantInsn(ICONST_3);
		code.visitLabel(end);
		code.visitStackInsn(POP);
		code.visitStackInsn(RETURN);
		code.visitMaxs(1, 1);
	}

	@Test
	public void testLocals() {
		Label start = new Label();
		Label start2 = new Label();
		Label end = new Label();
		code.visitLabel(start);
		code.visitConstantInsn(ICONST_1);
		code.visitConstantInsn(ICONST_2);
		code.visitStackInsn(IADD);
		code.visitVarInsn(ISTORE, 0);
		code.visitVarInsn(ILOAD, 0);
		code.visitLabel(start2);
		code.visitArithmeticInsn(I2D);
		code.visitConstantInsn(DCONST_0);
		code.visitArithmeticInsn(DADD);
		code.visitVarInsn(DSTORE, 1);
		code.visitVarInsn(DLOAD, 1);
		code.visitStackInsn(POP2);
		code.visitLabel(end);
		code.visitStackInsn(RETURN);
		code.visitLocalVariable(0, "i", Descriptor.INT, null, start, end);
		code.visitLocalVariable(1, "d", Descriptor.DOUBLE, null, start2, end);
		code.visitMaxs(2, 3);
	}

	@Test
	public void testInvalidLocal() {
		Label start = new Label();
		Label end = new Label();
		code.visitLabel(start);
		code.visitConstantInsn(ICONST_1);
		code.visitVarInsn(ISTORE, 0);
		code.visitVarInsn(ILOAD, 0);
		code.visitStackInsn(POP);
		code.visitStackInsn(RETURN);
		code.visitLocalVariable(0, "i", Descriptor.INT, null, start, end);
		code.visitMaxs(1, 1);
		expectedException = UnresolvedLabelException.class;
	}

	@Test
	public void testLineNumbers() {
		Label l1 = new Label();
		Label l2 = new Label();
		code.visitLabel(l1);
		code.visitLineNumber(1, l1);
		code.visitConstantInsn(ICONST_1);
		code.visitStackInsn(POP);
		code.visitLabel(l2);
		code.visitLineNumber(2, l2);
		code.visitStackInsn(RETURN);
		code.visitMaxs(1, 1);
	}

	@Test
	public void testTryCatch() {
		Label start = new Label();
		Label end = new Label();
		Label handler = new Label();
		code.visitLabel(start);
		code.visitConstantInsn(ICONST_1);
		code.visitStackInsn(POP);
		code.visitTypeInsn(NEW, "java/lang/IllegalStateException");
		code.visitStackInsn(DUP);
		code.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>",
				Descriptor.from("()V"));
		code.visitStackInsn(ATHROW);
		code.visitLabel(end);
		code.visitStackInsn(RETURN);
		code.visitExceptionHandler(null, start, end, handler);
		code.visitLabel(handler);
		code.visitStackInsn(POP);
		code.visitStackInsn(RETURN);
		code.visitMaxs(2, 1);
	}

	@Test
	public void testTryCatchWithException() {
		Label start = new Label();
		Label end = new Label();
		Label handler = new Label();
		code.visitLabel(start);
		code.visitConstantInsn(ICONST_1);
		code.visitStackInsn(POP);
		code.visitTypeInsn(NEW, "java/lang/IllegalStateException");
		code.visitStackInsn(DUP);
		code.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>",
				Descriptor.from("()V"));
		code.visitStackInsn(ATHROW);
		code.visitLabel(end);
		code.visitStackInsn(RETURN);
		code.visitExceptionHandler("java/lang/IllegalStateException", start, end, handler);
		code.visitLabel(handler);
		code.visitStackInsn(POP);
		code.visitStackInsn(RETURN);
		code.visitMaxs(2, 1);
	}


}
