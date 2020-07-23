package me.coley.cafedude.io;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * An implementation of {@link DataInputStream} that can seek backwards using {@link #reset(int)}
 * based off an internal {@link ByteArrayInputStream}.
 *
 * @author Matt Coley
 */
public class IndexableByteStream extends DataInputStream {
	private final IndexableByteArrayInputStream exposer;

	/**
	 * @param data
	 * 		Data to read from.
	 */
	public IndexableByteStream(byte[] data) {
		super(new IndexableByteArrayInputStream(data));
		this.exposer = ((IndexableByteArrayInputStream) in);
	}

	/**
	 * @return Current input stream index.
	 */
	public int getIndex() {
		return exposer.getIndex();
	}

	/**
	 * Seek backwards in the stream.
	 *
	 * @param distance
	 * 		Distance to move backwards.
	 */
	public void reset(int distance) {
		exposer.reset(distance);
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

		private void reset(int distance) {
			pos -= distance;
		}

		private int getIndex() {
			return pos;
		}
	}
}