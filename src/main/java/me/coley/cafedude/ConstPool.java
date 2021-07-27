package me.coley.cafedude;

import me.coley.cafedude.constant.ConstPoolEntry;
import me.coley.cafedude.constant.CpUtf8;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Constant pool wrapper.
 *
 * @author Matt Coley
 */
public class ConstPool implements Iterable<ConstPoolEntry> {
	private CpListNode first;
	private CpListNode last;

	/**
	 * Append an entry to the end of the pool.
	 *
	 * @param entry
	 * 		Inserted pool entry value.
	 */
	public void add(ConstPoolEntry entry) {
		if (first == null) {
			first = new CpListNode(entry);
			last = first;
		} else {
			last.insertAfter(entry);
			last = last.next;
		}
	}

	/**
	 * Insert an entry after the given index in the pool.
	 *
	 * @param index
	 * 		CP index.
	 * @param entry
	 * 		Inserted pool entry value.
	 */
	public void insertAfter(int index, ConstPoolEntry entry) {
		getNode(index).insertAfter(entry);
	}

	/**
	 * Insert an entry befoire the given index in the pool.
	 *
	 * @param index
	 * 		CP index.
	 * @param entry
	 * 		Inserted pool entry value.
	 */
	public void insertBefore(int index, ConstPoolEntry entry) {
		getNode(index).insertBefore(entry);
	}

	/**
	 * @param index
	 * 		CP index.
	 * @param entry
	 * 		New pool entry value.
	 */
	public void set(int index, ConstPoolEntry entry) {
		getNode(index).set(entry);
	}

	/**
	 * @param index
	 * 		CP index.
	 *
	 * @return Constant at index.
	 */
	public ConstPoolEntry get(int index) {
		return getNode(index).entry;
	}

	/**
	 * @param index
	 * 		CP index of UTF8 constant.
	 *
	 * @return String value of constant.
	 *
	 * @throws IllegalArgumentException
	 * 		When the index is not a UTF8 constant.
	 */
	public String getUtf(int index) {
		ConstPoolEntry entry = get(index);
		if (entry instanceof CpUtf8)
			return ((CpUtf8) entry).getText();
		throw new IllegalArgumentException("Index " + index + " not UTF8");
	}

	/**
	 * @param index
	 * 		CP index to check/
	 * @param type
	 * 		Type to assert.
	 *
	 * @return {@code true} when the entry at the index is the given type.
	 */
	public boolean isIndexOfType(int index, Class<? extends ConstPoolEntry> type) {
		try {
			ConstPoolEntry entry = get(index);
			return type.isAssignableFrom(entry.getClass());
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * The number of slots in the constant pool, including empty spaces for wide entry padding
	 * <i>(A mistake even sun regrets)</i>.
	 * <br>
	 * Note that even if the pool has a size of {@code 1}
	 * you should still use {@code 1} as the argument for {@link #get(int)} since the pool is not
	 * 0-indexed. Instead indices start at 1.
	 *
	 * @return Number of constants in the constant pool.
	 * Includes empty entries after wide values <i>(long/double)</i>
	 * <br>
	 * {@code 0} when there are no items.
	 */
	public int size() {
		if (last == null)
			return 0;
		return last.getCpIndex();
	}

	/**
	 * @param entry
	 * 		Entry to fetch index of. Must not be {@code null}.
	 *
	 * @return Index in pool. {@code -1} if not in pool.
	 */
	public int indexOf(ConstPoolEntry entry) {
		CpIter it = (CpIter) iterator();
		ConstPoolEntry itEntry = null;
		while (it.hasNext() && (itEntry = it.next()) != null) {
			if (entry.equals(itEntry))
				return it.currentIndex();
		}
		return -1;
	}


	/**
	 * Removes all entries in the pool that matches the given filter.
	 *
	 * @param filter
	 * 		Filter for constant matching.
	 */
	public void removeIf(Predicate<ConstPoolEntry> filter) {
		CpIter it = (CpIter) iterator();
		ConstPoolEntry entry = null;
		while (it.hasNext() && (entry = it.next()) != null) {
			if (filter.test(entry))
				it.remove();
		}
	}

	/**
	 * Replace all entries in the pool that matches the given filter.
	 *
	 * @param filter
	 * 		Filter for constant matching.
	 * @param replacer
	 * 		Function of old constant to replacement constant.
	 */
	public void replaceIf(Predicate<ConstPoolEntry> filter, Function<ConstPoolEntry, ConstPoolEntry> replacer) {
		CpIter it = (CpIter) iterator();
		ConstPoolEntry entry = null;
		while (it.hasNext() && (entry = it.next()) != null) {
			if (filter.test(entry))
				it.replace(replacer.apply(entry));
		}
	}

	@Override
	public Iterator<ConstPoolEntry> iterator() {
		assertNotEmpty();
		return new CpIter(first, true);
	}

	/**
	 * Iterates over the constant pool from the last to the first.
	 *
	 * @return Backwards iterator.
	 */
	public Iterator<ConstPoolEntry> backwardsIterator() {
		assertNotEmpty();
		return new CpIter(last, false);
	}

	/**
	 * @param index
	 * 		CP index.
	 *
	 * @return Linked list entry wrapper for constant at index.
	 */
	protected CpListNode getNode(int index) {
		// Min bounds check
		if (index <= 0)
			throw new IndexOutOfBoundsException("CP indices must be >= 1");
		assertNotEmpty();
		CpListNode node = first;
		// It's more optimized to count externally than use the node index getter, which is dynamic
		int i = 1;
		while (i < index) {
			// Max bounds check
			if (node.next == null)
				throw new IndexOutOfBoundsException("CP index out of range for class, max: " + i);
			i += node.getCpEntrySize();
			node = node.next;
		}
		if (i != index)
			throw new IndexOutOfBoundsException("CP index requested was reserved! Index: " + index);
		return node;
	}

	/**
	 * Throws {@link IndexOutOfBoundsException} if pool is empty.
	 */
	private void assertNotEmpty() {
		if (first == null)
			throw new IndexOutOfBoundsException("CP is empty!");
	}

	/**
	 * Linked list entry wrapper.
	 *
	 * @author Matt Coley
	 */
	public static class CpListNode {
		private ConstPoolEntry entry;
		private CpListNode next;
		private CpListNode prev;

		/**
		 * @param entry
		 * 		Wrapped pool entry.
		 */
		private CpListNode(ConstPoolEntry entry) {
			this.entry = entry;
		}

		/**
		 * Insert a pool entry after the current wrapped entry.
		 *
		 * @param entry
		 * 		Inserted pool entry value.
		 */
		public void insertAfter(ConstPoolEntry entry) {
			CpListNode oldNext = next;
			CpListNode node = new CpListNode(entry);
			node.next = oldNext;
			node.prev = this;
			// update adjacent nodes
			if (oldNext != null)
				oldNext.prev = node;
			// update self
			next = node;
		}

		/**
		 * Insert a pool entry before the current wrapped entry.
		 *
		 * @param entry
		 * 		Inserted pool entry value.
		 */
		public void insertBefore(ConstPoolEntry entry) {
			CpListNode oldPrev = prev;
			CpListNode node = new CpListNode(entry);
			node.next = this;
			node.prev = oldPrev;
			// update adjacent nodes
			oldPrev.next = node;
			// update self
			prev = node;
		}

		/**
		 * Update node's wrapped value.
		 *
		 * @param entry
		 * 		New pool entry value.
		 */
		public void set(ConstPoolEntry entry) {
			this.entry = entry;
		}

		/**
		 * @return Number of slots in the constant pool the wrapped entry occupies.
		 */
		public int getCpEntrySize() {
			return entry.isWide() ? 2 : 1;
		}

		/**
		 * @return Index of this wrapped cp-entry in the constant pool.
		 */
		public int getCpIndex() {
			int sum = 1;
			CpListNode tmp = prev;
			while (tmp != null) {
				sum += tmp.getCpEntrySize();
				tmp = tmp.prev;
			}
			return sum;
		}

		/**
		 * Remove self from the constant pool.
		 */
		public void delete() {
			// update adjacent nodes
			prev.next = next;
			next.prev = prev;
			// remove refs
			prev = null;
			next = null;
		}
	}

	/**
	 * Linked list iterator.
	 *
	 * @author Matt Coley
	 */
	public static class CpIter implements Iterator<ConstPoolEntry> {
		private final boolean forward;
		private CpListNode current;

		private CpIter(CpListNode initial, boolean forward) {
			current = initial;
			this.forward = forward;
		}

		/**
		 * @return Current index.
		 */
		public int currentIndex() {
			return current.getCpIndex();
		}

		/**
		 * Replace the current constant pool entry.
		 *
		 * @param entry
		 * 		New constant pool entry.
		 */
		public void replace(ConstPoolEntry entry) {
			if (forward) {
				current.prev.set(entry);
			} else {
				current.next.set(entry);
			}
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public ConstPoolEntry next() {
			ConstPoolEntry value = current.entry;
			current = forward ? current.next : current.prev;
			return value;

		}

		@Override
		public void remove() {
			// This is valid because to get the "current" value the user will have to call "next"
			if (forward) {
				current.prev.delete();
			} else {
				current.next.delete();
			}
		}
	}
}
