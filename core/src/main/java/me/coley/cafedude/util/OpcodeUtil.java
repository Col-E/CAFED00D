package me.coley.cafedude.util;

import me.coley.cafedude.classfile.instruction.Opcodes;
import me.coley.cafedude.classfile.instruction.ReservedOpcodes;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Utility for working with opcodes.
 */
public class OpcodeUtil {

	private static final @Nullable String[] names = new String[256];

	/**
	 * Get a string representation of the opcode.
	 * @param opcode
	 * 		Opcode to get string for.
	 * @return String representation of the opcode.
	 * 		   {@code null} if the opcode is invalid.
	 */
	public static @Nullable String getOpcodeName(int opcode) {
		if(opcode < 0 || opcode >= names.length) return null;
		return names[opcode];
	}

	static {
		try {
			// read the fields from the Opcodes and ReservedOpcodes classes
			// and put them into the names array
			int i = 0;
			Field[] fields = Opcodes.class.getFields();
			for (Field field : fields) {
				if(field.getType() != int.class) continue; // skip non-int fields
				names[i++] = field.getName().toLowerCase();
			}
			// now read the reserved opcodes
			fields = ReservedOpcodes.class.getFields();
			for (Field field : fields) {
				if(field.getType() != int.class) continue; // skip non-int fields
				names[i++] = field.getName().toLowerCase();
			}
		} catch (Exception e) {
			// silently ignore exceptions
		}
	}

}
