package me.coley.cafedude.classfile;

import me.coley.cafedude.classfile.constant.ConstPoolEntry;
import me.coley.cafedude.classfile.constant.CpUtf8;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Constant pool wrapper.
 *
 * @author Matt Coley
 */
public class ConstPool implements List<ConstPoolEntry> {
	private final List<ConstPoolEntry> backing = new ArrayList<>();
	private final SortedSet<Integer> wideIndices = new TreeSet<>();
	private final Map<Integer, Integer> indexToWides = new HashMap<>();

	/**
	 * Insert an entry after the given index in the pool.
	 *
	 * @param index
	 * 		CP index.
	 * @param entry
	 * 		Inserted pool entry value.
	 */
	public void insertAfter(int index, ConstPoolEntry entry) {
		add(index, entry);
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
		add(index - 1, entry);
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
		if(index == -1)
			return -1; // -1 is used when the index is not found in the CP.
		// 0: Double --> 1
		// 1: String --> 3 --
		// 2: String --> 4
		// 3: Double --> 5
		// 4: String --> 7 --
		// 5: String --> 8
		int wideCount = indexToWides.computeIfAbsent(index, i -> wideIndices.headSet(i + 1).size());
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
	 * @param constPoolEntry
	 * 		Entry added.
	 * @param location
	 * 		Location added.
	 */
	private void onAdd(ConstPoolEntry constPoolEntry, int location) {
		int entrySize = constPoolEntry.isWide() ? 2 : 1;
		// Need to push things over since something is being inserted.
		// Shift everything >= location by +entrySize
		SortedSet<Integer> larger = wideIndices.tailSet(location);
		if (!larger.isEmpty()) {
			List<Integer> tmp = new ArrayList<>(larger);
			larger.clear();
			tmp.forEach(i -> addWideIndex(i + entrySize));
		}
		// Add wide
		if (constPoolEntry.isWide())
			addWideIndex(location);
	}

	/**
	 * Update wide index tracking.
	 *
	 * @param constPoolEntry
	 * 		Entry removed.
	 * @param location
	 * 		Location removed from.
	 */
	private void onRemove(ConstPoolEntry constPoolEntry, int location) {
		int entrySize = constPoolEntry.isWide() ? 2 : 1;
		// Remove wide
		if (constPoolEntry.isWide())
			wideIndices.remove(location);
		// Need to move everything down to fill the gap.
		// Shift everything >= location by -entrySize
		SortedSet<Integer> larger = wideIndices.tailSet(location + 1);
		if (!larger.isEmpty()) {
			List<Integer> tmp = new ArrayList<>(larger);
			larger.clear();
			tmp.forEach(i -> addWideIndex(i - entrySize));
		}
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
	public boolean contains(Object o) {
		return backing.contains(o);
	}

	@Override
	public Iterator<ConstPoolEntry> iterator() {
		return backing.iterator();
	}

	@Override
	public Object[] toArray() {
		return backing.toArray(new ConstPoolEntry[0]);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		return (T[]) backing.toArray();
	}

	@Override
	public boolean add(ConstPoolEntry constPoolEntry) {
		onAdd(constPoolEntry, backing.size());
		return backing.add(constPoolEntry);
	}

	@Override
	public void add(int index, ConstPoolEntry element) {
		onAdd(element, index);
		backing.add(cpToInternal(index), element);
	}

	@Override
	public ConstPoolEntry remove(int index) {
		ConstPoolEntry ret = backing.remove(cpToInternal(index));
		if (ret != null)
			onRemove(ret, index);
		return ret;
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof ConstPoolEntry) {
			ConstPoolEntry constPoolEntry = (ConstPoolEntry) o;
			onRemove(constPoolEntry, indexOf(constPoolEntry));
			return backing.remove(constPoolEntry);
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return backing.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends ConstPoolEntry> c) {
		for (ConstPoolEntry constPoolEntry : c)
			add(constPoolEntry);
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends ConstPoolEntry> c) {
		for (ConstPoolEntry constPoolEntry : c)
			add(index, constPoolEntry);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean ret = false;
		for (Object o : c)
			ret |= remove(o);
		return ret;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean ret = false;
		for (ConstPoolEntry o : this)
			if (!c.contains(o))
				ret |= remove(o);
		return ret;
	}

	@Override
	public void clear() {
		onClear();
		backing.clear();
	}

	@Override
	public ConstPoolEntry get(int index) {
		return backing.get(cpToInternal(index));
	}

	@Override
	public ConstPoolEntry set(int index, ConstPoolEntry element) {
		ConstPoolEntry ret = remove(index);
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

	@Override
	public ListIterator<ConstPoolEntry> listIterator() {
		return backing.listIterator();
	}

	@Override
	public ListIterator<ConstPoolEntry> listIterator(int index) {
		return backing.listIterator(cpToInternal(index));
	}

	@Override
	public List<ConstPoolEntry> subList(int fromIndex, int toIndex) {
		return backing.subList(cpToInternal(fromIndex), cpToInternal(toIndex));
	}
}