package me.coley.cafedude.classfile.instruction;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Name utility for instruction opcodes.
 *
 * @author Matt Coley
 */
public class OpcodeNames {
	private static final String UNKNOWN = "?";
	private static final String[] NAMES = new String[255];

	/**
	 * @param opcode
	 * 		Opcode value.
	 *
	 * @return Name of the opcode, if known. Otherwise {@code "?"}.
	 */
	public static String name(int opcode) {
		if (opcode < 0 || opcode >= NAMES.length - 1)
			return UNKNOWN;
		return NAMES[opcode];
	}

	static {
		Arrays.fill(NAMES, UNKNOWN);
		try {
			for (Field field : Opcodes.class.getDeclaredFields()) {
				if (field.getType() == int.class) {
					int opcode = field.getInt(null);
					String name = field.getName();
					NAMES[opcode] = name;
				}
			}
		} catch (Throwable t) {
			throw new IllegalStateException("Failed to populate opcode names", t);
		}
	}
}
