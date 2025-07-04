package software.coley.cafedude.io;

import jakarta.annotation.Nonnull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Arrays;

/**
 * An implementation of {@link DataInputStream} that can seek to absolute positions or backwards
 * based off an internal {@link ByteArrayInputStream}.
 *
 * @author Matt Coley
 */
public class IndexableByteStream extends DataInputStream {
	private final IndexableByteArrayInputStream exposer;
	private int sliceStart;
	private int sliceEnd;

	/**
	 * New byte stream wrapping the given array.
	 *
	 * @param data
	 * 		Data to read from.
	 */
	public IndexableByteStream(byte[] data) {
		super(new IndexableByteArrayInputStream(data));
		this.exposer = ((IndexableByteArrayInputStream) in);
	}

	/**
	 * New byte stream for a slice of a given stream.
	 *
	 * @param is
	 * 		Parent indexed byte stream.
	 * @param length
	 * 		Length of content.
	 */
	public IndexableByteStream(@Nonnull IndexableByteStream is, int length) {
		this(is.exposer.getBuffer());

		// Skip to index of parent input stream
		int index = is.exposer.getIndex();
		int maxLength = is.exposer.getBuffer().length;
		moveTo(index);
		sliceStart = index;
		sliceEnd = Math.min(index + length, maxLength);
	}

	/**
	 * @return Current absolute input stream index.
	 */
	public int getAbsoluteIndex() {
		return exposer.getIndex();
	}

	/**
	 * @return Current relative input stream index.
	 */
	public int getIndex() {
		return exposer.getIndex() - sliceStart;
	}

	/**
	 * Seek to absolute position in the stream.
	 *
	 * @param index
	 * 		Absolute position to seek to.
	 */
	public void moveToAbsolute(int index) {
		exposer.moveTo(index);
	}

	/**
	 * Seek to the relative <i>(from the slice start)</i> absolute position in the stream.
	 *
	 * @param index
	 * 		Relative absolute position to seek to.
	 */
	public void moveTo(int index) {
		exposer.moveTo(index + sliceStart);
	}

	/**
	 * Seek backwards in the stream.
	 *
	 * @param distance
	 * 		Distance to move backwards.
	 */
	public void moveBack(int distance) {
		exposer.moveBack(distance);
	}

	/**
	 * @return {@code true} when this stream is a slice of another stream,
	 * and has run beyond the scope of the expected slice bounds.
	 * {@code false} if the stream is within the slice bounds, not a sliced stream.
	 */
	public boolean isBeyondSliceScope() {
		if (sliceStart == 0 && sliceEnd == 0)
			return false;
		int index = exposer.getIndex();
		return index > sliceEnd || index < sliceStart;
	}

	/**
	 * @return Backing byte stream buffer.
	 */
	public byte[] getBuffer() {
		byte[] buffer = exposer.getBuffer();
		if (sliceStart == 0 && sliceEnd == 0)
			return buffer;
		return Arrays.copyOfRange(buffer, sliceStart, sliceEnd);
	}

	/**
	 * Exposes position in {@link java.io.ByteArrayInputStream}.
	 *
	 * @author Matt Coley
	 */
	private static class IndexableByteArrayInputStream extends ByteArrayInputStream {
		private IndexableByteArrayInputStream(byte[] data) {
			super(data);
		}

		private void moveTo(int index) {
			pos = index;
		}

		private void moveBack(int distance) {
			pos -= distance;
		}

		private void moveForward(int distance) {
			pos += distance;
		}

		private int getIndex() {
			return pos;
		}

		private byte[] getBuffer() {
			return buf;
		}
	}
}