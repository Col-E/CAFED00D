package me.coley.cafedude.io;

import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.constant.ConstRef;
import me.coley.cafedude.classfile.constant.CpClass;
import me.coley.cafedude.classfile.constant.CpInvokeDynamic;
import me.coley.cafedude.classfile.instruction.*;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static me.coley.cafedude.classfile.instruction.Opcodes.*;

/**
 * Reads code attribute into meaningful instructions.
 *
 * @author xDark
 */
public class InstructionReader {
	private final FallbackInstructionReader fallbackReader;

	/**
	 * @param fallbackReader
	 * 		Fallback instruction reader.
	 */
	public InstructionReader(@Nonnull FallbackInstructionReader fallbackReader) {
		this.fallbackReader = fallbackReader;
	}

	/**
	 * Instruction reader that will use fail-fast fallback reader.
	 */
	public InstructionReader() {
		this(FallbackInstructionReader.fail());
	}

	/**
	 * @param code
	 * 		Code data.
	 * @param pool
	 * 		Constant pool to pull data from.
	 *
	 * @return List of instructions.
	 */
	@Nonnull
	@SuppressWarnings("DuplicateBranchesInSwitch")
	public List<Instruction> read(@Nonnull byte[] code, @Nonnull ConstPool pool) {
		List<Instruction> instructions = new ArrayList<>();
		ByteBuffer buffer = ByteBuffer.wrap(code);
		FallbackInstructionReader fallbackReader = this.fallbackReader;
		while (buffer.hasRemaining()) {
			int opcode = buffer.get() & 0xFF;
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
					instructions.add(new BasicInstruction(opcode));
					break;
				case BIPUSH:
					instructions.add(new IntOperandInstruction(opcode, buffer.get()));
					break;
				case SIPUSH:
					instructions.add(new IntOperandInstruction(opcode, buffer.getShort()));
					break;
				case LDC:
					instructions.add(new CpRefInstruction(opcode, pool.get(buffer.get() & 0xFF)));
					break;
				case LDC_W:
				case LDC2_W:
					instructions.add(new CpRefInstruction(opcode, pool.get(buffer.getShort() & 0xFFFF)));
					break;
				case ILOAD:
				case LLOAD:
				case FLOAD:
				case DLOAD:
				case ALOAD:
					instructions.add(new IntOperandInstruction(opcode, buffer.get() & 0xFF));
					break;
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
					instructions.add(new BasicInstruction(opcode));
					break;
				case IALOAD:
				case LALOAD:
				case FALOAD:
				case DALOAD:
				case AALOAD:
				case BALOAD:
				case CALOAD:
				case SALOAD:
					instructions.add(new BasicInstruction(opcode));
					break;
				case ISTORE:
				case LSTORE:
				case FSTORE:
				case DSTORE:
				case ASTORE:
					instructions.add(new IntOperandInstruction(opcode, buffer.get() & 0xFF));
					break;
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
					instructions.add(new BasicInstruction(opcode));
					break;
				case IASTORE:
				case LASTORE:
				case FASTORE:
				case DASTORE:
				case AASTORE:
				case BASTORE:
				case CASTORE:
				case SASTORE:
					instructions.add(new BasicInstruction(opcode));
					break;
				case POP:
				case POP2:
				case DUP:
				case DUP_X1:
				case DUP_X2:
				case DUP2:
				case DUP2_X1:
				case DUP2_X2:
				case SWAP:
					instructions.add(new BasicInstruction(opcode));
					break;
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
					instructions.add(new BasicInstruction(opcode));
					break;
				case IINC:
					instructions.add(new IincInstruction(buffer.get() & 0xFF, buffer.get()));
					break;
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
					instructions.add(new BasicInstruction(opcode));
					break;
				case LCMP:
				case FCMPL:
				case FCMPG:
				case DCMPL:
				case DCMPG:
					instructions.add(new BasicInstruction(opcode));
					break;
				case IFEQ:
				case IFNE:
				case IFLT:
				case IFGE:
				case IFGT:
				case IFLE:
					instructions.add(new IntOperandInstruction(opcode, buffer.getShort()));
					break;
				case IF_ICMPEQ:
				case IF_ICMPNE:
				case IF_ICMPLT:
				case IF_ICMPGE:
				case IF_ICMPGT:
				case IF_ICMPLE:
				case IF_ACMPEQ:
				case IF_ACMPNE:
					instructions.add(new IntOperandInstruction(opcode, buffer.getShort()));
					break;
				case GOTO:
					instructions.add(new IntOperandInstruction(GOTO, buffer.getShort()));
					break;
				case JSR:
					instructions.add(new IntOperandInstruction(JSR, buffer.getShort()));
					break;
				case RET:
					instructions.add(new IntOperandInstruction(RET, buffer.get() & 0xFF));
					break;
				case TABLESWITCH: {
					int pos = buffer.position();
					// Skip padding.
					buffer.position(pos + (4 - pos & 3));
					int padding = buffer.position() - pos;
					int dflt = buffer.getInt();
					int low = buffer.getInt();
					int high = buffer.getInt();
					int count = high - low + 1;
					List<Integer> offsets = new ArrayList<>(count);
					for (int i = 0; i < count; i++) {
						offsets.add(buffer.getInt());
					}
					TableSwitchInstruction tswitch = new TableSwitchInstruction(dflt, low, high, offsets);
					tswitch.notifyStartPosition(pos - 1); // Offset by 1 to accommodate for opcode
					instructions.add(tswitch);
					break;
				}
				case LOOKUPSWITCH: {
					int pos = buffer.position();
					// Skip padding.
					buffer.position(pos + (4 - pos & 3));
					int padding = buffer.position() - pos;
					int dflt = buffer.getInt();
					int keyCount = buffer.getInt();
					List<Integer> keys = new ArrayList<>(keyCount);
					List<Integer> offsets = new ArrayList<>(keyCount);
					for (int i = 0; i < keyCount; i++) {
						keys.add(buffer.getInt());
						offsets.add(buffer.getInt());
					}
					LookupSwitchInstruction lswitch = new LookupSwitchInstruction(dflt, keys, offsets);
					lswitch.notifyStartPosition(pos - 1); // Offset by 1 to accommodate for opcode
					instructions.add(lswitch);
					break;
				}
				case IRETURN:
				case LRETURN:
				case FRETURN:
				case DRETURN:
				case ARETURN:
				case RETURN:
					instructions.add(new BasicInstruction(opcode));
					break;
				case GETSTATIC:
				case PUTSTATIC:
				case GETFIELD:
				case PUTFIELD:
				case INVOKEVIRTUAL:
				case INVOKESPECIAL:
				case INVOKESTATIC:
				case INVOKEINTERFACE:
					ConstRef ref = (ConstRef) pool.get(buffer.getShort() & 0xFFFF);
					instructions.add(new CpRefInstruction(opcode, ref));
					break;
				case INVOKEDYNAMIC: {
					int index = buffer.getShort() & 0xFFFF;
					if ((buffer.get() | buffer.get()) != 0) {
						// TODO: should we silently ignore, or throw?
						throw new IllegalStateException("InvokeDynamic padding bytes are non-zero");
					}
					CpInvokeDynamic entry = (CpInvokeDynamic) pool.get(index);
					instructions.add(new CpRefInstruction(INVOKEDYNAMIC, entry));
					break;
				}
				case NEW:
				case ANEWARRAY:
				case CHECKCAST:
				case INSTANCEOF:
					CpClass clazz = (CpClass) pool.get(buffer.getShort() & 0xFFFF);
					instructions.add(new CpRefInstruction(opcode, clazz));
					break;
				case NEWARRAY:
					instructions.add(new IntOperandInstruction(NEWARRAY, buffer.get() & 0xFF));
					break;
				case ARRAYLENGTH:
					instructions.add(new BasicInstruction(ARRAYLENGTH));
					break;
				case ATHROW:
					instructions.add(new BasicInstruction(ATHROW));
					break;
				case MONITORENTER:
				case MONITOREXIT:
					instructions.add(new BasicInstruction(opcode));
					break;
				case WIDE:
					int type = buffer.get() & 0xFF;
					switch (type) {
						case ILOAD:
						case FLOAD:
						case ALOAD:
						case LLOAD:
						case DLOAD:
						case ISTORE:
						case FSTORE:
						case ASTORE:
						case LSTORE:
						case DSTORE:
						case RET:
							instructions.add(new WideInstruction(new IntOperandInstruction(type,
									buffer.getShort() & 0xFFFF)));
							break;
						case IINC:
							instructions.add(new WideInstruction(new IincInstruction(buffer.getShort() & 0xFFFF,
									buffer.getShort())));
							break;
						default:
							throw new IllegalStateException("Illegal wide instruction type: " + type);
					}
					break;
				case MULTIANEWARRAY:
					int index = buffer.getShort() & 0xFFFF;
					int dimensions = buffer.get() & 0xFF;
					instructions.add(new MultiANewArrayInstruction((CpClass) pool.get(index),
							dimensions));
					break;
				case IFNULL:
				case IFNONNULL:
					instructions.add(new IntOperandInstruction(opcode, buffer.getShort()));
					break;
				case GOTO_W:
				case JSR_W:
					instructions.add(new IntOperandInstruction(opcode, buffer.getInt()));
					break;
				default:
					instructions.addAll(fallbackReader.read(opcode, buffer));
			}
		}
		return instructions;
	}
}
