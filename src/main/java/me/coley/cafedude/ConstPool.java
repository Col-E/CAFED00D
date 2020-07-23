package me.coley.cafedude;

import me.coley.cafedude.constant.ConstPoolEntry;

import java.util.ListIterator;

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
	 * @return Number of constants in the constant pool. Includes empty entries after wide values <i>(long/double)</i>
	 */
	public int size() {
		if (last == null)
			return 0;
		return last.getCpIndex();
	}

	@Override
	public ListIterator<ConstPoolEntry> iterator() {
		assertNotEmpty();
		return new CpIter(first);
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
		int i = node.getCpEntrySize();
		while (i < index) {
			// Max bounds check
			if (node.next == null)
				throw new IndexOutOfBoundsException("CP index out of range for class, max: " + i);
			node = node.next;
			i += node.getCpEntrySize();
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
	 */
	protected static class CpListNode {
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
			int sum = 0;
			CpListNode tmp = this;
			do {
				sum += tmp.getCpEntrySize();
				tmp = tmp.prev;
			} while (tmp != null);
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
	 */
	private static class CpIter implements ListIterator<ConstPoolEntry> {
		private CpListNode current;

		private CpIter(CpListNode initial) {
			current = initial;
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public boolean hasPrevious() {
			return current.prev != null;
		}

		@Override
		public int nextIndex() {
			return current.getCpIndex() + current.getCpEntrySize();
		}

		@Override
		public int previousIndex() {
			return current.getCpIndex() - current.prev.getCpEntrySize();
		}

		@Override
		public ConstPoolEntry next() {
			ConstPoolEntry value = current.entry;
			current = current.next;
			return value;
		}

		@Override
		public ConstPoolEntry previous() {
			current = current.prev;
			return current.entry;
		}

		@Override
		public void remove() {
			current.delete();
		}

		@Override
		public void set(ConstPoolEntry entry) {
			current.set(entry);
		}

		@Override
		public void add(ConstPoolEntry entry) {
			current.insertAfter(entry);
		}
	}
}
