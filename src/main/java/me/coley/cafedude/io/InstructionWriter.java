package me.coley.cafedude.io;

import me.coley.cafedude.instruction.*;
import me.coley.cafedude.util.GrowingByteBuffer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static me.coley.cafedude.instruction.Opcodes.*;

/**
 * Writes meaningful instructions
 * into code attribute.
 *
 * @author xDark
 */
public class InstructionWriter {

	private final FallbackInstructionWriter fallbackWriter;

	/**
	 * @param fallbackWriter
	 * 		Fallback instruction writer.
	 */
	public InstructionWriter(FallbackInstructionWriter fallbackWriter) {
		this.fallbackWriter = fallbackWriter;
	}

	/**
	 * Instruction writer that will use
	 * fail-fast fallback writer.
	 */
	public InstructionWriter() {
		this(FallbackInstructionWriter.fail());
	}

	/**
	 * Writes the list of instructions to a {@code byte[]}.
	 *
	 * @param list
	 * 		Instructions to write.
	 *
	 * @return Content written.
	 */
	public byte[] writeCode(List<Instruction> list) {
		GrowingByteBuffer buffer = new GrowingByteBuffer();
		FallbackInstructionWriter fallbackWriter = this.fallbackWriter;
		for (Instruction instruction : list) {
			int opcode = instruction.getOpcode();
			buffer.put(opcode);
			switch (opcode) {
				case NOP:
				case ACONST_NULL:
				case ICONST_M1:
				case ICONST_0:
				case ICONST_1:
				case ICONST_2:
				case ICONST_3:
				case ICONST_4:
				case ICONST_5:
				case LCONST_0:
				case LCONST_1:
				case FCONST_0:
				case FCONST_1:
				case FCONST_2:
				case DCONST_0:
				case DCONST_1:
				case ILOAD_0:
				case ILOAD_1:
				case ILOAD_2:
				case ILOAD_3:
				case LLOAD_0:
				case LLOAD_1:
				case LLOAD_2:
				case LLOAD_3:
				case FLOAD_0:
				case FLOAD_1:
				case FLOAD_2:
				case FLOAD_3:
				case DLOAD_0:
				case DLOAD_1:
				case DLOAD_2:
				case DLOAD_3:
				case ALOAD_0:
				case ALOAD_1:
				case ALOAD_2:
				case ALOAD_3:
				case IALOAD:
				case LALOAD:
				case FALOAD:
				case DALOAD:
				case AALOAD:
				case BALOAD:
				case CALOAD:
				case SALOAD:
				case ISTORE_0:
				case ISTORE_1:
				case ISTORE_2:
				case ISTORE_3:
				case LSTORE_0:
				case LSTORE_1:
				case LSTORE_2:
				case LSTORE_3:
				case FSTORE_0:
				case FSTORE_1:
				case FSTORE_2:
				case FSTORE_3:
				case DSTORE_0:
				case DSTORE_1:
				case DSTORE_2:
				case DSTORE_3:
				case ASTORE_0:
				case ASTORE_1:
				case ASTORE_2:
				case ASTORE_3:
				case IASTORE:
				case LASTORE:
				case FASTORE:
				case DASTORE:
				case AASTORE:
				case BASTORE:
				case CASTORE:
				case SASTORE:
				case POP:
				case POP2:
				case DUP:
				case DUP_X1:
				case DUP_X2:
				case DUP2:
				case DUP2_X1:
				case DUP2_X2:
				case SWAP:
				case IADD:
				case LADD:
				case FADD:
				case DADD:
				case ISUB:
				case LSUB:
				case FSUB:
				case DSUB:
				case IMUL:
				case LMUL:
				case FMUL:
				case DMUL:
				case IDIV:
				case LDIV:
				case FDIV:
				case DDIV:
				case IREM:
				case LREM:
				case FREM:
				case DREM:
				case INEG:
				case LNEG:
				case FNEG:
				case DNEG:
				case ISHL:
				case LSHL:
				case ISHR:
				case LSHR:
				case IUSHR:
				case LUSHR:
				case IAND:
				case LAND:
				case IOR:
				case LOR:
				case IXOR:
				case LXOR:
				case I2L:
				case I2F:
				case I2D:
				case L2I:
				case L2F:
				case L2D:
				case F2I:
				case F2L:
				case F2D:
				case D2I:
				case D2L:
				case D2F:
				case I2B:
				case I2C:
				case I2S:
				case LCMP:
				case FCMPL:
				case FCMPG:
				case DCMPL:
				case DCMPG:
				case IRETURN:
				case LRETURN:
				case FRETURN:
				case DRETURN:
				case ARETURN:
				case RETURN:
				case ARRAYLENGTH:
				case ATHROW:
				case MONITORENTER:
				case MONITOREXIT:
					break;
				case BIPUSH:
					buffer.put(((IntOperandInstruction) instruction).getOperand());
					break;
				case SIPUSH:
				case IFEQ:
				case IFNE:
				case IFLT:
				case IFGE:
				case IFGT:
				case IFLE:
				case IF_ICMPEQ:
				case IF_ICMPNE:
				case IF_ICMPLT:
				case IF_ICMPGE:
				case IF_ICMPGT:
				case IF_ICMPLE:
				case IF_ACMPEQ:
				case IF_ACMPNE:
				case GOTO:
				case JSR:
				case IFNULL:
				case IFNONNULL:
					buffer.putShort(((IntOperandInstruction) instruction).getOperand());
					break;
				case LDC:
				case ILOAD:
				case LLOAD:
				case FLOAD:
				case DLOAD:
				case ALOAD:
				case ISTORE:
				case LSTORE:
				case FSTORE:
				case DSTORE:
				case ASTORE:
				case RET:
				case NEWARRAY:
					buffer.put(((IntOperandInstruction) instruction).getOperand() & 0xff);
					break;
				case LDC_W:
				case LDC2_W:
				case GETSTATIC:
				case PUTSTATIC:
				case GETFIELD:
				case PUTFIELD:
				case INVOKEVIRTUAL:
				case INVOKESPECIAL:
				case INVOKESTATIC:
				case INVOKEINTERFACE:
				case NEW:
				case ANEWARRAY:
				case CHECKCAST:
				case INSTANCEOF:
					buffer.putShort(((IntOperandInstruction) instruction).getOperand() & 0xff);
					break;
				case IINC: {
					BiIntOperandInstruction iinc = (BiIntOperandInstruction) instruction;
					buffer.put(iinc.getFirstOperand() & 0xff);
					buffer.put(iinc.getSecondOperand());
					break;
				}
				case TABLESWITCH: {
					int pos = buffer.position();
					buffer.skip(pos + (4 - pos & 3));
					TableSwitchInstruction tsw = (TableSwitchInstruction) instruction;
					buffer.putInt(tsw.getDefault());
					buffer.putInt(tsw.getLow());
					buffer.putInt(tsw.getHigh());
					List<Integer> branches = tsw.getOffsets();
					for (int i = 0, j = branches.size(); i < j; i++) {
						buffer.putInt(branches.get(i));
					}
					break;
				}
				case LOOKUPSWITCH:
					int pos = buffer.position();
					buffer.skip(pos + (4 - pos & 3));
					LookupSwitchInstruction lsw = (LookupSwitchInstruction) instruction;
					buffer.putInt(lsw.getDefault());
					List<Integer> keys = lsw.getKeys();
					List<Integer> offsets = lsw.getOffsets();
					for (int i = 0, j = keys.size(); i < j; i++) {
						buffer.putInt(keys.get(i));
						buffer.putInt(offsets.get(i));
					}
					break;
				case INVOKEDYNAMIC:
					buffer.putShort(((IntOperandInstruction) instruction).getOperand() & 0xff);
					buffer.putShort(0);
					break;
				case WIDE:
					Instruction backing = ((WideInstruction) instruction).getBacking();
					int type = backing.getOpcode();
					buffer.put(type & 0xff);
					switch (type) {
						case ILOAD:
						case FLOAD:
						case ALOAD:
						case LLOAD:
						case DLOAD:
						case ISTORE:
						case FSTORE:
						case DSTORE:
						case RET:
							buffer.putShort(((IntOperandInstruction) backing).getOperand() & 0xff);
							break;
						case IINC:
							BiIntOperandInstruction iinc = (BiIntOperandInstruction) backing;
							buffer.putShort(iinc.getFirstOperand() & 0xff);
							buffer.putShort(iinc.getSecondOperand());
							break;
						default:
							throw new IllegalStateException("Illegal wide instruction type: " + type);
					}
					break;
				case MULTIANEWARRAY:
					BiIntOperandInstruction multiNewArray = (BiIntOperandInstruction) instruction;
					buffer.putShort(multiNewArray.getFirstOperand() & 0xff);
					buffer.put(multiNewArray.getSecondOperand() & 0xff);
					break;
				case GOTO_W:
				case JSR_W:
					buffer.putInt(((IntOperandInstruction) instruction).getOperand());
					break;
				default:
					fallbackWriter.write(instruction, buffer);
			}
		}
		ByteBuffer result = buffer.unwrap();
		byte[] content = result.array();
		return Arrays.copyOf(content, result.position());
	}
}
