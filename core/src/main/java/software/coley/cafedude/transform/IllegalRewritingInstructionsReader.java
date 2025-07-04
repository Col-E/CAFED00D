package software.coley.cafedude.transform;

import jakarta.annotation.Nonnull;
import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.VersionConstants;
import software.coley.cafedude.classfile.instruction.BasicInstruction;
import software.coley.cafedude.classfile.instruction.Instruction;
import software.coley.cafedude.classfile.instruction.IntOperandInstruction;
import software.coley.cafedude.classfile.instruction.Opcodes;
import software.coley.cafedude.classfile.instruction.ReservedOpcodes;
import software.coley.cafedude.io.ClassBuilder;
import software.coley.cafedude.io.ClassFileReader;
import software.coley.cafedude.io.FallbackInstructionReader;
import software.coley.cafedude.io.IndexableByteStream;
import software.coley.cafedude.io.InstructionReader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static software.coley.cafedude.classfile.instruction.Opcodes.*;
import static software.coley.cafedude.classfile.instruction.ReservedOpcodes.*;

/**
 * Illegal instruction rewriter.
 * <p/>
 * Implementation details about these instructions can be found in the:
 * <ul>
 *     <li><a href="https://github.com/openjdk/jdk/blob/f2d2eef988c57cc9f6194a8fd5b2b422035ee68f/src/hotspot/share/interpreter/zero/bytecodeInterpreter.cpp">C++ {@code bytecodeInterpreter.cpp}</a></li>
 *     <li><a href="https://github.com/openjdk/jdk/blob/5e40fb6bda1d56e3eba584b49aa0b68096b34169/src/hotspot/share/interpreter/bytecodes.cpp">C++ {@code bytecodes.cpp}</a>.</li>
 *     <li><a href="https://github.com/openjdk/jdk/blob/5e40fb6bda1d56e3eba584b49aa0b68096b34169/src/jdk.hotspot.agent/share/classes/sun/jvm/hotspot/interpreter/Bytecodes.java">Java {@code Bytecodes.java}</a>.</li>
 * </ul>
 *
 * @author xDark
 * @author Matt Coley
 * @see ReservedOpcodes Opcodes of reserved instructions.
 * @see ClassFileReader#getFallbackInstructionReader(ClassBuilder)
 */
public class IllegalRewritingInstructionsReader implements FallbackInstructionReader {
	private static final Instruction NOP_INSN = new BasicInstruction(NOP);
	private static final Instruction ALOAD_0_INSN = new BasicInstruction(ALOAD_0);
	private static final Instruction CALOAD = new BasicInstruction(Opcodes.CALOAD);
	private static final Instruction RETURN_INSN = new BasicInstruction(RETURN);
	private final int classVersion;
	private final ConstPool cp;
	boolean rewritten;

	/**
	 * @param cp
	 * 		Constant pool used to pull references from.
	 * @param classVersion
	 * 		Class version.
	 */
	public IllegalRewritingInstructionsReader(@Nonnull ConstPool cp, int classVersion) {
		this.cp = cp;
		this.classVersion = classVersion;
	}

	@Nonnull
	@Override
	public List<Instruction> read(int opcode, @Nonnull IndexableByteStream is) throws IOException {
		// Handle 'should-not-reach-here' which changes depending on the class version.
		if ((classVersion <= VersionConstants.JAVA8 && opcode == shouldnotreachhere_v8)
				|| (classVersion == VersionConstants.JAVA9 && opcode == shouldnotreachhere_v9)
				|| classVersion >= VersionConstants.JAVA11 && opcode == shouldnotreachhere_v11) {
			rewritten = true;
			return Collections.singletonList(NOP_INSN);
		}

		// Format strings interpretation:
		//
		// b: bytecode
		// c: signed constant, Java byte-ordering
		// i: unsigned index , Java byte-ordering
		// j: unsigned index , native byte-ordering
		// o: branch offset  , Java byte-ordering
		// _: unused/ignored
		// w: wide bytecode
		//
		// JJ: unsigned short (cp entry)
		switch (opcode) {
			case breakpoint:
				rewritten = true;
				// Breakpoint occupies two bytes (op + arg).
				// Two nops will replace a breakpoint.
				is.readByte();
				return List.of(NOP_INSN, NOP_INSN);
			case fast_agetfield: // Format = bJJ
			case fast_bgetfield:
			case fast_cgetfield:
			case fast_dgetfield:
			case fast_fgetfield:
			case fast_igetfield:
			case fast_lgetfield:
			case fast_sgetfield:
			case nofast_getfield:
				// Simple opcode swap
				rewritten = true;
				return Collections.singletonList(InstructionReader.readMemberReferenceInstruction(is, cp, GETFIELD));
			case fast_aputfield: // Format = bJJ
			case fast_bputfield:
			case fast_cputfield:
			case fast_dputfield:
			case fast_fputfield:
			case fast_iputfield:
			case fast_lputfield:
			case fast_sputfield:
			case fast_zputfield:
			case nofast_putfield:
				// Simple opcode swap
				rewritten = true;
				return Collections.singletonList(InstructionReader.readMemberReferenceInstruction(is, cp, PUTFIELD));
			case fast_aload_0: // Format = b
			case nofast_aload_0:
				// Simple opcode swap
				rewritten = true;
				return Collections.singletonList(ALOAD_0_INSN);
			case fast_iload: // Format = bi
			case nofast_iload:
				// Simple opcode swap
				rewritten = true;
				return Collections.singletonList(InstructionReader.readXLoad(is, ILOAD));
			case fast_aldc: // Format = bj
				// Simple opcode swap
				rewritten = true;
				return Collections.singletonList(InstructionReader.readLdc(is, cp));
			case fast_aldc_w:  // Format = bJJ
				// Simple opcode swap
				rewritten = true;
				return Collections.singletonList(InstructionReader.readLdcW(is, cp, LDC_W));
			case return_register_finalizer: // Format = b
				// Simple opcode swap
				rewritten = true;
				return Collections.singletonList(RETURN_INSN);
			case fast_invokevfinal: // Format = bJJ
				// Interpreter rewrites 'invokevirtual' to 'invokevfinal' if the method is final.
				rewritten = true;
				return Collections.singletonList(InstructionReader.readMemberReferenceInstruction(is, cp, INVOKEVIRTUAL));
			case fast_linearswitch: // Format is blank
			case fast_binaryswitch:
				// In 'bytecodes.cpp' these are both implemented as plain lookup-switches.
				rewritten = true;
				return Collections.singletonList(InstructionReader.readLookupSwitchInstruction(is));
			case fast_iaccess_0: // Format = b_JJ
			case fast_aaccess_0:
			case fast_faccess_0:
				rewritten = true;
				// Interpreter is rewriting the following patterns:
				//  aload_0, fast_agetfield --> fast_aaccess_0
				//  aload_0, fast_igetfield --> fast_iaccess_0
				//  aload_0, fast_fgetfield --> fast_faccess_0
				//
				// Only the opcode for aload_0 changes.
				is.readUnsignedByte();
				return List.of(ALOAD_0_INSN, InstructionReader.readMemberReferenceInstruction(is, cp, GETFIELD));
			case fast_iload2: // Format = bi_i
				// Interpreter is rewriting the following patterns:
				//  iload_x, iload_y --> fast_iload2
				//
				// Only the opcode for iload_x changes.
				rewritten = true;
				IntOperandInstruction iload1 = InstructionReader.readXLoad(is, ILOAD);
				is.readUnsignedByte(); // Padding byte for '_' in format
				IntOperandInstruction iload2 = InstructionReader.readXLoad(is, ILOAD);
				return List.of(iload1, iload2);
			case fast_icaload: // Format = bi_
				// Interpreter is rewriting the following patterns:
				//  iload_x, caload --> fast_icaload
				//
				// Only the opcode for iload_x changes.
				rewritten = true;
				IntOperandInstruction iload = InstructionReader.readXLoad(is, ILOAD);
				is.readUnsignedByte(); // Padding byte for '_' in format
				return List.of(iload, CALOAD);
			case invokehandle: // Format = bJJ
				// This one is confusing...
				// - 'sharedRuntime.cpp' implies there is no receiver for 'invokehandle' similar to 'invokestatic/dynamic'
				// - But 'rewriter.cpp' seems to imply it is any of the invoke instructions
				// - And 'bytecodes.cpp' says its basline is 'invokevirtual'
				rewritten = true;
				return Collections.singletonList(InstructionReader.readMemberReferenceInstruction(is, cp, INVOKEVIRTUAL));
			default:
				throw new IllegalStateException("Don't know how to rewrite " + opcode);
		}
	}
}
