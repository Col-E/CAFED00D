package me.coley.cafedude.classfile;

/**
 * Constants for constant pool.
 *
 * @author Matt Coley
 */
public interface ConstantPoolConstants {
	/** Constant pool identifier for UTF8 values. These values are used by other constants. */
	int UTF8 = 1;
	/** Constant pool identifier for integers. */
	int INTEGER = 3;
	/** Constant pool identifier for floats. */
	int FLOAT = 4;
	/** Constant pool identifier for longs. */
	int LONG = 5;
	/** Constant pool identifier for doubles. */
	int DOUBLE = 6;
	/** Constant pool identifier for classes. */
	int CLASS = 7;
	/** Constant pool identifier for strings. */
	int STRING = 8;
	/** Constant pool identifier for field references. */
	int FIELD_REF = 9;
	/** Constant pool identifier for method references. */
	int METHOD_REF = 10;
	/** Constant pool identifier for interface method references. */
	int INTERFACE_METHOD_REF = 11;
	/** Constant pool identifier for name-type. These are simply name/descriptor pairs. */
	int NAME_TYPE = 12;
	/** Constant pool identifier for method handles. */
	int METHOD_HANDLE = 15;
	/** Constant pool identifier for method types. These are simply descriptor UTF8s. */
	int METHOD_TYPE = 16;
	/** Constant pool identifier for dynamically fetched values to be held by constants. */
	int DYNAMIC = 17;
	/** Constant pool identifier for dynamically fetched method handles. */
	int INVOKE_DYNAMIC = 18;
	/** Constant pool identifier for modules. */
	int MODULE = 19;
	/** Constant pool identifier for packages. */
	int PACKAGE = 20;
}
