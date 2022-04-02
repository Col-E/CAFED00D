package me.coley.cafedude.util;

import java.nio.ByteBuffer;

/**
 * Byte buffer that can extend its capacity.
 *
 * @author xDark
 */
public class GrowingByteBuffer {

	private ByteBuffer buffer = ByteBuffer.allocate(1024);

	public GrowingByteBuffer ensureWriteable(int size) {
		ByteBuffer buffer = this.buffer;
		int capacity = buffer.capacity();
		int newLength = buffer.position() + size;
		if (capacity < newLength) {
			ByteBuffer copy = ByteBuffer.allocate((newLength + 16) >> 1 << 1);
			copy.put(buffer);
			this.buffer = copy;
		}
		return this;
	}

	public GrowingByteBuffer putBoolean(boolean value) {
		ensureWriteable(1).buffer.put((byte) (value ? 1 : 0));
		return this;
	}

	public GrowingByteBuffer put(int value) {
		ensureWriteable(1).buffer.put((byte) value);
		return this;
	}

	public GrowingByteBuffer putShort(int value) {
		ensureWriteable(2).buffer.putShort((short) value);
		return this;
	}

	public GrowingByteBuffer putChar(char value) {
		ensureWriteable(2).buffer.putChar(value);
		return this;
	}

	public GrowingByteBuffer putInt(int value) {
		ensureWriteable(4).buffer.putInt(value);
		return this;
	}

	public GrowingByteBuffer putFloat(float value) {
		ensureWriteable(4).buffer.putFloat(value);
		return this;
	}

	public GrowingByteBuffer putLong(long value) {
		ensureWriteable(8).buffer.putLong(value);
		return this;
	}

	public GrowingByteBuffer putDouble(double value) {
		ensureWriteable(8).buffer.putDouble(value);
		return this;
	}
	
	public int position() {
		return buffer.position();
	}
	
	public GrowingByteBuffer skip(int bytes) {
		ByteBuffer buffer = ensureWriteable(bytes).buffer;
		buffer.position(buffer.position() + bytes);
		return this;
	}

	public ByteBuffer unwrap() {
		return buffer;
	}
}
