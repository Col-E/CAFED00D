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

public class Symbols {

	protected final ConstPool pool;

	public Symbols(ConstPool pool) {
		this.pool = pool;
	}

	int newUtf8(String value) {
		return newSym(new CpUtf8(value));
	}

	int newClass(String type) {
		return newSym(new CpClass(newUtf8(type)));
	}

	int newNameType(String name, Descriptor type) {
		return newSym(new CpNameType(newUtf8(name), newUtf8(type.toString())));
	}

	int newField(String owner, String name, Descriptor type) {
		return newSym(new CpFieldRef(newClass(owner), newNameType(name, type)));
	}

	int newMethod(String owner, String name, Descriptor type) {
		return newSym(new CpMethodRef(newClass(owner), newNameType(name, type)));
	}

	int newInterfaceMethod(String owner, String name, Descriptor type) {
		return newSym(new CpInterfaceMethodRef(newClass(owner), newNameType(name, type)));
	}

	int newHandle(Handle handle) {
		return newSym(new CpMethodHandle((byte) handle.getTag().ordinal(),
				newMethod(handle.getOwner(), handle.getName(), handle.getDescriptor())));
	}

	int newInvokeDynamic(int bootstrapMethodIndex, int nameAndTypeIndex) {
		return newSym(new CpInvokeDynamic(bootstrapMethodIndex, nameAndTypeIndex));
	}

	int newSym(ConstPoolEntry entry) {
		int index = pool.indexOf(entry);
		if(index != -1) { // no duplicate entries
			return index;
		}
		pool.add(entry);
		return pool.indexOf(entry);
	}

	int newConstant(Constant value) {
		switch (value.getType()) {
			case STRING:
				return newUtf8((String) value.getValue());
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
						int descriptorIndex = newUtf8(descriptor.getDescriptor());
						return newSym(new CpMethodType(descriptorIndex));
					}
					case OBJECT:
					case ARRAY: {
						int descriptorIndex = newUtf8(descriptor.getDescriptor());
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

	ElementValue newElementValue(Constant value) {
		char tag = ' ';
		int index = newConstant(value);
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
				return new PrimitiveElementValue(tag, index);
			case 's': // String
				return new Utf8ElementValue(tag, index);
			case 'c': // Class
				return new ClassElementValue(tag, index);
		}
		throw new IllegalStateException("Unknown element value tag: " + tag);
	}
}
