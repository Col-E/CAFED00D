package software.coley.cafedude.classfile;

import jakarta.annotation.Nonnull;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpInternal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Constant pool wrapper.
 *
 * @author Matt Coley
 */
public class ConstPool implements List<CpEntry> {
	private final List<CpEntry> backing = new ArrayList<>();

	public ConstPool() {
		// Constant pool index starts at one, so we add a reserved item at the 0th index.
		backing.add(ImplZero.INSTANCE);
	}

	@Override
	public int size() {
		// Size is correct as-is since we insert dummy entries at the 0th index and for any wide reserved slot.
		return backing.size();
	}

	@Override
	public boolean isEmpty() {
		// Constant pool index starts at one, so we ignore our first item.
		return size() <= 1;
	}

	@Override
	public boolean contains(Object o) {
		return backing.contains(o);
	}

	@Override
	public int indexOf(Object o) {
		for (int i = 1; i < backing.size(); i++)
			if (Objects.equals(backing.get(i), o))
				return i;
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		for (int i = backing.size() - 1; i >= 1; i--)
			if (Objects.equals(backing.get(i), o))
				return i;
		return -1;
	}

	@Nonnull
	@Override
	public CpIterator iterator() {
		return listIterator();
	}

	@Nonnull
	@Override
	public CpIterator listIterator() {
		// Initialize index to one since the constant pool starts at one.
		return listIterator(1);
	}

	@Nonnull
	@Override
	public CpIterator listIterator(int index) {
		return new CpIterator(this, index);
	}

	@Nonnull
	@Override
	public Object[] toArray() {
		Object[] array = backing.toArray();
		for (int i = 0; i < array.length; i++) {
			Object o = array[i];
			if (o instanceof CpEntry && ((CpEntry) o).getTag() <= 0)
				array[i] = null;
		}
		return array;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(@Nonnull T[] array) {
		return (T[]) toArray();
	}

	@Nonnull
	@Override
	public List<CpEntry> subList(int fromIndex, int toIndex) {
		// Create a new list that contains only valid entries.
		List<CpEntry> list = new ArrayList<>();
		for (int i = fromIndex; i < toIndex; i++) {
			CpEntry cp = get(i);

			// Skip adding any of our padding entries.
			if (cp.getTag() > 0)
				list.add(cp);
		}
		return list;
	}

	@Override
	public boolean add(CpEntry cp) {
		int index = size();
		backing.add(cp);
		cp.setIndex(index);
		if (cp.isWide())
			backing.add(getWideFiller());
		return true;
	}

	@Override
	public void add(int index, CpEntry cp) {
		if (cp == null)
			throw new IllegalArgumentException("Cannot add null");
		requireValidInsertion(index);

		// Insert the padding slot first so a wide constant lands as [entry, padding].
		if (cp.isWide())
			backing.add(index, getWideFiller());

		// Insert the entry and reindex all entries after it.
		backing.add(index, cp);
		fixIndices(index);
	}

	@Override
	public boolean addAll(Collection<? extends CpEntry> c) {
		boolean res = false;
		for (CpEntry cp : c)
			res |= add(cp);
		return res;
	}

	@Override
	public boolean addAll(int index, Collection<? extends CpEntry> c) {
		requireValidInsertion(index);
		if (c.isEmpty())
			return false;

		int offset = 0;
		for (CpEntry cp : c) {
			add(index + offset, cp);
			offset += cp.isWide() ? 2 : 1;
		}
		return true;
	}

	@Override
	public boolean remove(Object o) {
		int i = indexOf(o);
		if (i >= 1)
			return remove(i) != null;
		return false;
	}

	@Override
	public CpEntry remove(int index) {
		requireValidIndex(index);

		// Remove the entry.
		CpEntry removed = backing.remove(index);

		// Remove the following padding slot if the removed entry was wide.
		if (removed.isWide())
			backing.remove(index);

		// Reindex all entries after the removed entry.
		fixIndices(index);
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean res = false;
		for (Object o : c)
			if (o instanceof CpEntry)
				res |= remove(o);
		return res;
	}

	@Override
	public void clear() {
		backing.clear();
		backing.add(ImplZero.INSTANCE);
	}

	@Override
	public boolean containsAll(@Nonnull Collection<?> c) {
		return c.stream().allMatch(this::contains);
	}

	@Override
	public boolean retainAll(@Nonnull Collection<?> c) {
		boolean ret = false;
		for (CpEntry cp : this)
			if (!c.contains(cp))
				ret |= remove(cp);
		return ret;
	}

	@Override
	public CpEntry get(int index) {
		if (index < 1 || index >= size())
			return null;
		CpEntry cp = backing.get(index);
		if (cp.getTag() <= 0)
			return null;
		return cp;
	}

	@Override
	public CpEntry set(int index, CpEntry cp) {
		if (cp == null)
			throw new IllegalArgumentException("Cannot set null");
		requireValidIndex(index);

		// Check width of the old and new entries. If they differ, we need to add/remove a padding slot.
		CpEntry old = backing.get(index);
		boolean oldWide = old.isWide();
		boolean newWide = cp.isWide();

		// Replace the entry and reindex all entries after it if the width changed.
		backing.set(index, cp);
		cp.setIndex(index);
		if (oldWide != newWide) {
			if (newWide)
				backing.add(index + 1, getWideFiller());
			else
				backing.remove(index + 1);
		}
		fixIndices(index + 1);
		return old;
	}

	/**
	 * Recomputes the index of each entry in the backing list starting at the given index.
	 *
	 * @param startingIndex
	 * 		Index to start reindexing at.
	 */
	protected void fixIndices(int startingIndex) {
		// Recompute each real entry's index from its backing position, skipping
		// the reserved zero slot and wide padding slots. Walking the backing list
		// directly avoids the iterator's cursor/padding edge cases.
		for (int i = Math.max(startingIndex, 1); i < backing.size(); i++) {
			CpEntry entry = backing.get(i);
			if (!(entry instanceof CpInternal))
				entry.setIndex(i);
		}
	}

	/**
	 * @param index
	 * 		Index to check.
	 *
	 * @throws IndexOutOfBoundsException
	 * 		When the index is the reserved zero slot, a wide padding slot, or out of range.
	 */
	protected void requireValidIndex(int index) {
		if (index < 1 || index >= size())
			throw new IndexOutOfBoundsException("Constant index out of range: " + index);
		if (isFiller(index))
			throw new IndexOutOfBoundsException("Cannot access a reserved wide padding slot: " + index);
	}

	/**
	 * @param index
	 * 		Insertion index to check.
	 *
	 * @throws IndexOutOfBoundsException
	 * 		When the index precedes the reserved zero slot or sits inside a wide padding slot.
	 */
	protected void requireValidInsertion(int index) {
		if (index < 1 || index > size())
			throw new IndexOutOfBoundsException("Constant insertion index out of range: " + index);
		if (index < size() && isFiller(index))
			throw new IndexOutOfBoundsException("Cannot insert into a wide padding slot: " + index);
	}

	/**
	 * @param index
	 * 		Backing index to check.
	 *
	 * @return {@code true} when the slot is the reserved zero entry or a wide padding entry.
	 */
	protected boolean isFiller(int index) {
		return backing.get(index) instanceof CpInternal;
	}

	/**
	 * @return Singleton for wide padding entries.
	 */
	@Nonnull
	public static CpEntry getWideFiller() {
		return ImplWidePadding.INSTANCE;
	}

	public static class CpIterator implements ListIterator<CpEntry> {
		private final ConstPool pool;
		private int lastCursor;
		private int cursor;

		public CpIterator(@Nonnull ConstPool pool, int cursor) {
			this.pool = pool;
			this.cursor = cursor;
		}

		public void forEach(@Nonnull CpConsumer consumer) {
			while (hasNext()) {
				int i = cursor;
				CpEntry next = next();
				consumer.accept(i, next);
			}
		}

		@Override
		public boolean hasNext() {
			return cursor < pool.size();
		}

		@Override
		public CpEntry next() {
			if (hasNext()) {
				// Move forwards, skipping over padding entries.
				CpEntry cp = pool.get(cursor);
				if (cp == null)
					throw new NoSuchElementException("No CP at " + cursor);
				lastCursor = cursor;
				cursor += cp.isWide() ? 2 : 1;
				return cp;
			}
			throw new NoSuchElementException("End of CP");
		}

		@Override
		public boolean hasPrevious() {
			return cursor > 1;
		}

		@Override
		public CpEntry previous() {
			if (!hasPrevious())
				throw new NoSuchElementException();
			// The slot before the cursor may be a wide entry's padding, so step
			// over it to the entry that owns that slot.
			int prevIndex = cursor - 1;
			CpEntry cp = pool.get(prevIndex);
			if (cp == null) {
				prevIndex--;
				cp = pool.get(prevIndex);
			}
			if (cp == null)
				throw new NoSuchElementException("No CP before " + cursor);
			lastCursor = prevIndex;
			cursor = prevIndex;
			return cp;
		}

		@Override
		public int nextIndex() {
			return Math.min(cursor + 1, pool.size());
		}

		@Override
		public int previousIndex() {
			if (hasPrevious()) {
				// Check the previous entry. If it is a valid entry the prev index is just -1.
				// If the entry is not a valid tag, the prev index is -2.
				CpEntry cp = pool.get(cursor - 1);
				if (cp == null)
					return -1;
				return cp.getTag() <= 0 ? cursor - 2 : cursor - 1;
			}
			return -1;
		}

		@Override
		public void remove() {
			if (lastCursor < 1)
				throw new IllegalStateException("No last returned entry to remove");

			// Removing the last returned entry shifts everything after it down by
			// its width, so move the cursor back by the same amount.
			int width = pool.get(lastCursor).isWide() ? 2 : 1;
			pool.remove(lastCursor);
			if (lastCursor < cursor)
				cursor -= width;

			lastCursor = -1;
		}

		@Override
		public void set(CpEntry cp) {
			if (lastCursor < 1)
				throw new IllegalStateException("No last returned entry to set");

			// Replace the last returned entry. If its width changes, the padding
			// slot count changes and entries after it shift, so adjust the cursor.
			int oldWidth = pool.get(lastCursor).isWide() ? 2 : 1;
			int newWidth = cp.isWide() ? 2 : 1;
			pool.set(lastCursor, cp);
			if (lastCursor < cursor)
				cursor += newWidth - oldWidth;
		}

		@Override
		public void add(CpEntry cp) {
			// Insert at the cursor position and advance past it. Clearing
			// lastCursor makes a subsequent remove() target this new entry.
			pool.add(cursor, cp);
			cursor += cp.isWide() ? 2 : 1;
			lastCursor = -1;
		}

		public interface CpConsumer {
			void accept(int index, @Nonnull CpEntry entry);
		}
	}

	private static class ImplZero extends CpInternal {
		private static final ImplZero INSTANCE = new ImplZero();

		public ImplZero() {
			super(-1);
		}

		@Override
		public String toString() {
			return "Zero";
		}
	}

	private static class ImplWidePadding extends CpInternal {
		private static final ImplWidePadding INSTANCE = new ImplWidePadding();

		public ImplWidePadding() {
			super(-1);
		}

		@Override
		public String toString() {
			return "WidePadding";
		}
	}
}