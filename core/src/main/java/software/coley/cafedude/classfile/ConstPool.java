package software.coley.cafedude.classfile;

import software.coley.cafedude.classfile.constant.CpEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Constant pool wrapper.
 *
 * @author Matt Coley
 */
public class ConstPool implements List<CpEntry> {
	private final List<CpEntry> backing = new ArrayList<>();
	private final SortedSet<Integer> wideIndices = new TreeSet<>();
	private final Map<Integer, Integer> indexToWides = new HashMap<>();

	/**
	 * CP indices are 1-indexed, so the indices must start at 1.
	 * In addition, wide constants <i>(long/double)</i> take two indices in the CP.
	 * <br>
	 * In order to count wide indices, we use {@link SortedSet#headSet(Object)} which is a sub-set of items
	 * that are {@code < index}.
	 *
	 * @param index
	 * 		Internal index of {@link #backing}.
	 *
	 * @return Converted CP index.
	 */
	private int internalToCp(int index) {
		if (index == -1)
			return -1; // -1 is used when the index is not found in the CP.
		// 0: Double --> 1
		// 1: String --> 3 --
		// 2: String --> 4
		// 3: Double --> 5
		// 4: String --> 7 --
		// 5: String --> 8
		int wideCount = indexToWides.computeIfAbsent(index, i -> wideIndices.headSet(i).size());
		return 1 + index + wideCount;
	}

	/**
	 * CP indices are 1-indexed, so the indices must start at 1.
	 * In addition, wide constants <i>(long/double)</i> take two indices in the CP.
	 * <br>
	 *
	 * @param index
	 * 		CP index.
	 *
	 * @return Converted internal index for {@link #backing}.
	 */
	private int cpToInternal(int index) {
		// Edge case
		if (index == 0)
			return index;
		// Convert index back to 0-index
		int internal = index - 1;
		// Just subtract until a match. Will be at worst O(N) where N is the # of wide entries.
		while (internalToCp(internal - 1) >= index) {
			internal--;
		}
		return internal;
	}

	/**
	 * Clear wide entries.
	 */
	private void onClear() {
		wideIndices.clear();
	}

	/**
	 * Update wide index tracking.
	 *
	 * @param cpEntry
	 * 		Entry added.
	 * @param location
	 * 		Location added.
	 */
	private void onAdd(@Nonnull CpEntry cpEntry, int location) {
		int entrySize = cpEntry.isWide() ? 2 : 1;
		// Need to push things over since something is being inserted.
		// Shift everything >= location by +entrySize
		SortedSet<Integer> larger = wideIndices.tailSet(location);
		if (!larger.isEmpty()) {
			List<Integer> tmp = new ArrayList<>(larger);
			larger.clear();
			tmp.forEach(i -> addWideIndex(i + entrySize));
		}
		// Add wide
		if (cpEntry.isWide())
			addWideIndex(location);
		cpEntry.setIndex(internalToCp(location));
	}

	/**
	 * Update wide index tracking.
	 *
	 * @param cpEntry
	 * 		Entry removed.
	 * @param location
	 * 		Location removed from.
	 */
	private void onRemove(@Nonnull CpEntry cpEntry, int location) {
		int entrySize = cpEntry.isWide() ? 2 : 1;
		// Remove wide
		if (cpEntry.isWide())
			wideIndices.remove(location);
		// Need to move everything down to fill the gap.
		// Shift everything >= location by -entrySize
		SortedSet<Integer> larger = wideIndices.tailSet(location + 1);
		if (!larger.isEmpty()) {
			List<Integer> tmp = new ArrayList<>(larger);
			larger.clear();
			tmp.forEach(i -> addWideIndex(i - entrySize));
		}
		cpEntry.setIndex(-1);
	}

	private void addWideIndex(int i) {
		wideIndices.add(i);
		indexToWides.clear();
	}

	@Override
	public int size() {
		if (backing.isEmpty())
			return 0;
		return internalToCp(backing.size() - 1);
	}

	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	@Override
	public boolean contains(@Nonnull Object o) {
		return backing.contains(o);
	}

	@Nonnull
	@Override
	public Iterator<CpEntry> iterator() {
		return backing.iterator();
	}

	@Nonnull
	@Override
	public Object[] toArray() {
		return backing.toArray(new CpEntry[0]);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		return (T[]) backing.toArray();
	}

	@Override
	public boolean add(@Nonnull CpEntry cpEntry) {
		onAdd(cpEntry, backing.size());
		return backing.add(cpEntry);
	}

	@Override
	public void add(int index, @Nonnull CpEntry element) {
		onAdd(element, index);
		backing.add(cpToInternal(index), element);
	}

	@Nullable
	@Override
	public CpEntry remove(int index) {
		CpEntry ret = backing.remove(cpToInternal(index));
		if (ret != null)
			onRemove(ret, index);
		return ret;
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof CpEntry) {
			CpEntry cpEntry = (CpEntry) o;
			onRemove(cpEntry, indexOf(cpEntry));
			return backing.remove(cpEntry);
		}
		return false;
	}

	@Override
	public boolean containsAll(@Nonnull Collection<?> c) {
		return new HashSet<>(backing).containsAll(c);
	}

	@Override
	public boolean addAll(@Nonnull Collection<? extends CpEntry> c) {
		for (CpEntry cpEntry : c)
			add(cpEntry);
		return true;
	}

	@Override
	public boolean addAll(int index, @Nonnull Collection<? extends CpEntry> c) {
		for (CpEntry cpEntry : c)
			add(index, cpEntry);
		return true;
	}

	@Override
	public boolean removeAll(@Nonnull Collection<?> c) {
		boolean ret = false;
		for (Object o : c)
			ret |= remove(o);
		return ret;
	}

	@Override
	public boolean retainAll(@Nonnull Collection<?> c) {
		boolean ret = false;
		for (CpEntry o : this)
			if (!c.contains(o))
				ret |= remove(o);
		return ret;
	}

	@Override
	public void clear() {
		onClear();
		backing.clear();
	}

	@Nullable
	@Override
	public CpEntry get(int index) {
		try {
			if (index == 0)
				return null;
			return backing.get(cpToInternal(index));
		} catch (IndexOutOfBoundsException e) {
			throw new InvalidCpIndexException(this, index);
		}
	}

	@Nullable
	@Override
	public CpEntry set(int index, @Nonnull CpEntry element) {
		CpEntry ret = remove(index);
		add(index, element);
		return ret;
	}

	@Override
	public int indexOf(Object o) {
		return internalToCp(backing.indexOf(o));
	}

	@Override
	public int lastIndexOf(Object o) {
		return internalToCp(backing.lastIndexOf(o));
	}

	@Nonnull
	@Override
	public ListIterator<CpEntry> listIterator() {
		return backing.listIterator();
	}

	@Nonnull
	@Override
	public ListIterator<CpEntry> listIterator(int index) {
		return backing.listIterator(cpToInternal(index));
	}

	@Nonnull
	@Override
	public List<CpEntry> subList(int fromIndex, int toIndex) {
		return backing.subList(cpToInternal(fromIndex), cpToInternal(toIndex));
	}
}