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
			backing.add(ImplWidePadding.INSTANCE);
		return true;
	}

	@Override
	public void add(int index, CpEntry cp) {
		if (cp.isWide())
			backing.add(index, ImplWidePadding.INSTANCE);
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
		for (CpEntry cp : c)
			add(index, cp);
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
		CpEntry removed = backing.remove(index);
		if (removed.isWide() && backing.get(index) instanceof ImplWidePadding)
			backing.remove(index);
		fixIndices(index);
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean res = false;
		for (Object o : c)
			if (o instanceof CpEntry)
				res |= add((CpEntry) o);
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
		if (index < 1 || index >= size())
			return null;
		return backing.set(index, cp);
	}

	protected void fixIndices(int startingIndex) {
		listIterator(startingIndex).forEach((i, entry) -> entry.setIndex(i));
	}

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
			if (hasPrevious()) {
				// Move backwards, skipping over padding entries.
				CpEntry cp = pool.get(cursor - 1);
				if (cp == null)
					throw new NoSuchElementException("No CP at " + (cursor - 1));
				if (cp.getTag() <= 0)
					cp = pool.get(cursor - 2);
				if (cp == null)
					throw new NoSuchElementException("No CP at " + (cursor - 2));
				lastCursor = cursor;
				cursor -= cp.isWide() ? 2 : 1;
				return cp;
			}
			throw new NoSuchElementException();
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
			int remove = previousIndex();
			if (remove < 1)
				throw new IllegalStateException();
			pool.remove(remove);
			cursor = lastCursor;
		}

		@Override
		public void set(CpEntry cp) {
			pool.set(cursor, cp);
		}

		@Override
		public void add(CpEntry cp) {
			pool.set(cursor, cp);
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