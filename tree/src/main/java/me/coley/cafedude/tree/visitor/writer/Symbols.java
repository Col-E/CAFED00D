package me.coley.cafedude.tree.visitor.writer;

import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Descriptor;
import me.coley.cafedude.classfile.annotation.ClassElementValue;
import me.coley.cafedude.classfile.annotation.ElementValue;
import me.coley.cafedude.classfile.annotation.PrimitiveElementValue;
import me.coley.cafedude.classfile.annotation.Utf8ElementValue;
import me.coley.cafedude.classfile.constant.*;
import me.coley.cafedude.tree.Constant;
import me.coley.cafedude.tree.Handle;

import javax.annotation.Nonnull;

/**
 * Helper for symbol creation.
 *
 * @author Justus Garbe
 */
public class Symbols {
	protected final ConstPool pool;

	public Symbols(@Nonnull ConstPool pool) {
		this.pool = pool;
	}

	CpUtf8 newUtf8(@Nonnull String value) {
		return newSym(new CpUtf8(value));
	}

	CpClass newClass(@Nonnull String type) {
		return newSym(new CpClass(newUtf8(type)));
	}

	CpNameType newNameType(@Nonnull String name, @Nonnull Descriptor type) {
		return newSym(new CpNameType(newUtf8(name), newUtf8(type.getDescriptor())));
	}

	CpFieldRef newField(@Nonnull String owner, @Nonnull String name, @Nonnull Descriptor type) {
		return newSym(new CpFieldRef(newClass(owner), newNameType(name, type)));
	}

	CpMethodRef newMethod(@Nonnull String owner, @Nonnull String name, @Nonnull Descriptor type) {
		return newSym(new CpMethodRef(newClass(owner), newNameType(name, type)));
	}

	CpInterfaceMethodRef newInterfaceMethod(@Nonnull String owner, @Nonnull String name, @Nonnull Descriptor type) {
		return newSym(new CpInterfaceMethodRef(newClass(owner), newNameType(name, type)));
	}

	CpMethodHandle newHandle(@Nonnull Handle handle) {
		return newSym(new CpMethodHandle((byte) handle.getTag().ordinal(),
				newMethod(handle.getOwner(), handle.getName(), handle.getDescriptor())));
	}

	CpInvokeDynamic newInvokeDynamic(int bootstrapMethodIndex, @Nonnull CpNameType nameAndTypeIndex) {
		return newSym(new CpInvokeDynamic(bootstrapMethodIndex, nameAndTypeIndex));
	}

	CpPackage newPackage(@Nonnull String exportPackage) {
		return newSym(new CpPackage(newUtf8(exportPackage)));
	}

	CpModule newModule(@Nonnull String module) {
		return newSym(new CpModule(newUtf8(module)));
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	<T extends CpEntry> T newSym(T entry) {
		int index = pool.indexOf(entry);
		if (index != -1) { // no duplicate entries
			T value = (T) pool.get(index);
			if (value == null)
				throw new IllegalStateException("Symbol at index " + index + " was null");
			return value;
		}
		pool.add(entry);
		return entry;
	}

	@Nonnull
	CpEntry newConstant(@Nonnull Constant value) {
		switch (value.getType()) {
			case STRING:
				return newSym(new CpString(newUtf8((String) value.getValue())));
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case SHORT:
			case INT:
				return newSym(new CpInt((int) value.getValue()));
			case LONG:
				return newSym(new CpLong((long) value.getValue()));
			case FLOAT:
				return newSym(new CpFloat((float) value.getValue()));
			case DOUBLE:
				return newSym(new CpDouble((double) value.getValue()));
			case DESCRIPTOR: {
				Descriptor descriptor = (Descriptor) value.getValue();
				switch (descriptor.getKind()) {
					case METHOD: {
						CpUtf8 descriptorIndex = newUtf8(descriptor.getDescriptor());
						return newSym(new CpMethodType(descriptorIndex));
					}
					case OBJECT:
					case ARRAY: {
						CpUtf8 descriptorIndex = newUtf8(descriptor.getDescriptor());
						return newSym(new CpClass(descriptorIndex));
					}
					case PRIMITIVE:
						throw new IllegalStateException("Cannot create constant for primitive descriptor");
					case ILLEGAL:
						throw new IllegalStateException("Cannot create constant for illegal descriptor");
				}
			}
			case HANDLE: {
				Handle handle = (Handle) value.getValue();
				return newHandle(handle);
			}
		}
		throw new IllegalStateException("Unknown constant type: " + value.getType());
	}

	@Nonnull
	ElementValue newElementValue(@Nonnull Constant value) {
		char tag = ' ';
		CpEntry entry;
		if (value.getType().equals(Constant.Type.STRING))
			// ElementValue requires UTF8 instead of String
			entry = newUtf8((String) value.getValue());
		else entry = newConstant(value);
		switch (value.getType()) {
			case BOOLEAN:
				tag = 'Z';
				break;
			case BYTE:
				tag = 'B';
				break;
			case CHAR:
				tag = 'C';
				break;
			case SHORT:
				tag = 'S';
				break;
			case INT:
				tag = 'I';
				break;
			case LONG:
				tag = 'J';
				break;
			case FLOAT:
				tag = 'F';
				break;
			case DOUBLE:
				tag = 'D';
				break;
			case STRING:
				tag = 's';
				break;
			case DESCRIPTOR: {
				Descriptor descriptor = (Descriptor) value.getValue();
				switch (descriptor.getKind()) {
					case OBJECT:
					case ARRAY:
						tag = 'c';
						break;
					case METHOD:
						throw new IllegalStateException("Cannot create element value for method descriptor");
					case PRIMITIVE:
						throw new IllegalStateException("Cannot create element value for primitive descriptor");
					case ILLEGAL:
						throw new IllegalStateException("Cannot create element value for illegal descriptor");
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Invalid constant type: " + value.getType());
		}
		switch (tag) {
			case 'B': // byte
			case 'C': // char
			case 'D': // double
			case 'F': // float
			case 'I': // int
			case 'J': // long
			case 'S': // short
			case 'Z': // boolean
				return new PrimitiveElementValue(tag, entry);
			case 's': // String
				return new Utf8ElementValue(tag, (CpUtf8) entry);
			case 'c': // Class
				return new ClassElementValue(tag, (CpUtf8) entry);
		}
		throw new IllegalStateException("Unknown element value tag: " + tag);
	}
}
