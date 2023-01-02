package me.coley.cafedude.util;

import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.constant.*;
import me.coley.cafedude.tree.Constant;

import static me.coley.cafedude.classfile.ConstantPoolConstants.*;

public class ConstantUtil {

	/**
	 * Convert a {@link ConstPoolEntry} to a {@link Constant}.
	 * @param entry
	 * 			Constant pool entry.
	 * @param pool
	 * 			Constant pool to use for resolving references.
	 * @return Constant or {@code null} if the entry is not convertible.
	 */
	public static Constant toConstant(ConstPoolEntry entry, ConstPool pool) {
		switch (entry.getTag()) {
			case UTF8: return new Constant(Constant.Type.STRING, ((CpUtf8) entry).getText());
			case STRING: return toConstant(pool.get(((CpString) entry).getIndex()), pool);
			case INTEGER: return new Constant(Constant.Type.INT, ((CpInt) entry).getValue());
			case FLOAT: return new Constant(Constant.Type.FLOAT, ((CpFloat) entry).getValue());
			case LONG: return new Constant(Constant.Type.LONG, ((CpLong) entry).getValue());
			case DOUBLE: return new Constant(Constant.Type.DOUBLE, ((CpDouble) entry).getValue());
			case CLASS: {
				CpClass cpClass = (CpClass) entry;
				String name = pool.get(cpClass.getIndex()).toString();
				return new Constant(Constant.Type.DESCRIPTOR, Descriptor.from(name));
			}
			default: return null;
		}
	}

}
