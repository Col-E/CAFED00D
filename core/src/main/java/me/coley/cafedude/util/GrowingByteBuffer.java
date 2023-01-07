package me.coley.cafedude.util;

import java.nio.ByteBuffer;

/**
 * Byte buffer that can extend its capacity.
 *
 * @author xDark
 */
public class GrowingByteBuffer {
	private ByteBuffer buffer = ByteBuffer.allocate(1024);

	/**
	 * Ensures that {@code size} of bytes can be
	 * written.
	 *
	 * @param size
	 * 		Size to check.
	 *
	 * @return This buffer, possibly expanded.
	 */
	public GrowingByteBuffer ensureWriteable(int size) {
		ByteBuffer buffer = this.buffer;
		int capacity = buffer.capacity();
		int newLength = buffer.position() + size;
		if (capacity < newLength) {
			ByteBuffer copy = ByteBuffer.allocate((newLength + 16) >> 1 << 1);
			buffer.flip();
			copy.put(buffer);
			this.buffer = copy;
		}
		return this;
	}

	/**
	 * Writes boolean to a buffer.
	 *
	 * @param value
	 * 		Boolean to write.
	 *
	 * @return This buffer.
	 */
	public GrowingByteBuffer putBoolean(boolean value) {
		ensureWriteable(1).buffer.put((byte) (value ? 1 : 0));
		return this;
	}

	/**
	 * Writes byte to a buffer.
	 *
	 * @param value
	 * 		Byte to write.
	 *
	 * @return This buffer.
	 */
	public GrowingByteBuffer put(int value) {
		ensureWriteable(1).buffer.put((byte) value);
		return this;
	}

	/**
	 * Writes short to a buffer.
	 *
	 * @param value
	 * 		Short to write.
	 *
	 * @return This buffer.
	 */
	public GrowingByteBuffer putShort(int value) {
		ensureWriteable(2).buffer.putShort((short) value);
		return this;
	}

	/**
	 * Writes char to a buffer.
	 *
	 * @param value
	 * 		Char to write.
	 *
	 * @return This buffer.
	 */
	public GrowingByteBuffer putChar(char value) {
		ensureWriteable(2).buffer.putChar(value);
		return this;
	}

	/**
	 * Writes int to a buffer.
	 *
	 * @param value
	 * 		Int to write.
	 *
	 * @return This buffer.
	 */
	public GrowingByteBuffer putInt(int value) {
		ensureWriteable(4).buffer.putInt(value);
		return this;
	}

	/**
	 * Writes float to a buffer.
	 *
	 * @param value
	 * 		Float to write.
	 *
	 * @return This buffer.
	 */
	public GrowingByteBuffer putFloat(float value) {
		ensureWriteable(4).buffer.putFloat(value);
		return this;
	}

	/**
	 * Writes long to a buffer.
	 *
	 * @param value
	 * 		Long to write.
	 *
	 * @return This buffer.
	 */
	public GrowingByteBuffer putLong(long value) {
		ensureWriteable(8).buffer.putLong(value);
		return this;
	}

	/**
	 * Writes double to a buffer.
	 *
	 * @param value
	 * 		Double to write.
	 *
	 * @return This buffer.
	 */
	public GrowingByteBuffer putDouble(double value) {
		ensureWriteable(8).buffer.putDouble(value);
		return this;
	}

	/**
	 * @return Current position.
	 */
	public int position() {
		return buffer.position();
	}

	/**
	 * Skips some amount of bytes.
	 *
	 * @param bytes
	 * 		Amount of bytes to skip.
	 *
	 * @return This buffer.
	 */
	public GrowingByteBuffer skip(int bytes) {
		ByteBuffer buffer = ensureWriteable(bytes).buffer;
		buffer.position(buffer.position() + bytes);
		return this;
	}

	/**
	 * @return Underlying buffer.
	 */
	public ByteBuffer unwrap() {
		return buffer;
	}
}
