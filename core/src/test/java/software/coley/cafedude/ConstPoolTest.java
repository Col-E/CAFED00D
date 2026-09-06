package software.coley.cafedude;

import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.constant.CpInt;
import software.coley.cafedude.classfile.constant.CpLong;
import software.coley.cafedude.classfile.constant.CpUtf8;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for general behavior of {@link ConstPool} when adding, removing, and replacing entries.
 */
public class ConstPoolTest {
	@Test
	public void testAppendKeepsWidePaddingAndIndices() {
		ConstPool pool = new ConstPool();
		CpUtf8 a = new CpUtf8("a");
		CpLong longEntry = new CpLong(1L);
		CpUtf8 b = new CpUtf8("b");

		pool.add(a);
		pool.add(longEntry);
		pool.add(b);

		// Backing is [zero, a, long, padding, b].
		assertEquals(5, pool.size());
		assertEquals(a, pool.get(1));
		assertEquals(longEntry, pool.get(2));
		assertNull(pool.get(3));
		assertEquals(b, pool.get(4));
		assertEquals(1, a.getIndex());
		assertEquals(2, longEntry.getIndex());
		assertEquals(4, b.getIndex());
	}

	@Test
	public void testSetNarrowToWideInsertsPadding() {
		ConstPool pool = new ConstPool();
		CpUtf8 a = new CpUtf8("a");
		CpUtf8 b = new CpUtf8("b");
		pool.add(a);
		pool.add(b);

		CpLong longEntry = new CpLong(9L);
		pool.set(1, longEntry);

		// Backing is [zero, long, padding, b].
		assertEquals(4, pool.size());
		assertEquals(longEntry, pool.get(1));
		assertNull(pool.get(2));
		assertEquals(b, pool.get(3));
		assertEquals(1, longEntry.getIndex());
		assertEquals(3, b.getIndex());
	}

	@Test
	public void testSetWideToNarrowRemovesPadding() {
		ConstPool pool = new ConstPool();
		pool.add(new CpLong(1L));
		CpUtf8 b = new CpUtf8("b");
		pool.add(b);

		CpUtf8 x = new CpUtf8("x");
		pool.set(1, x);

		// Backing is [zero, x, b].
		assertEquals(3, pool.size());
		assertEquals(x, pool.get(1));
		assertEquals(b, pool.get(2));
		assertEquals(1, x.getIndex());
		assertEquals(2, b.getIndex());
	}

	@Test
	public void testRemoveWideRemovesPaddingAndRepairsIndices() {
		ConstPool pool = new ConstPool();
		pool.add(new CpLong(1L));
		CpUtf8 b = new CpUtf8("b");
		pool.add(b);

		pool.remove(1);

		// Backing is [zero, b].
		assertEquals(2, pool.size());
		assertEquals(b, pool.get(1));
		assertEquals(1, b.getIndex());
	}

	@Test
	public void testRemoveLastWideEntryDoesNotOverflow() {
		ConstPool pool = new ConstPool();
		pool.add(new CpLong(1L));

		// Backing is [zero, long, padding].
		assertEquals(3, pool.size());
		pool.remove(1);

		// Only the reserved zero slot remains.
		assertEquals(1, pool.size());
	}

	@Test
	public void testRemoveRejectsZeroAndPaddingSlots() {
		ConstPool pool = new ConstPool();
		pool.add(new CpLong(1L));

		// The reserved zero slot and the wide padding slot are not removable constants.
		assertThrows(IndexOutOfBoundsException.class, () -> pool.remove(0));
		assertThrows(IndexOutOfBoundsException.class, () -> pool.remove(2));
	}

	@Test
	public void testSetRejectsZeroAndPaddingSlots() {
		ConstPool pool = new ConstPool();
		pool.add(new CpLong(1L));

		assertThrows(IndexOutOfBoundsException.class, () -> pool.set(0, new CpInt(1)));
		assertThrows(IndexOutOfBoundsException.class, () -> pool.set(2, new CpInt(1)));
	}

	@Test
	public void testRemoveIfRemovesWideEntryWithPadding() {
		ConstPool pool = new ConstPool();
		CpUtf8 a = new CpUtf8("a");
		CpLong longEntry = new CpLong(1L);
		CpUtf8 b = new CpUtf8("b");
		pool.add(a);
		pool.add(longEntry);
		pool.add(b);

		pool.removeIf(entry -> entry instanceof CpLong);

		// Backing is [zero, a, b].
		assertEquals(3, pool.size());
		assertEquals(a, pool.get(1));
		assertEquals(b, pool.get(2));
		assertEquals(1, a.getIndex());
		assertEquals(2, b.getIndex());
	}

	@Test
	public void testIteratorSetNarrowToWide() {
		ConstPool pool = new ConstPool();
		CpUtf8 a = new CpUtf8("a");
		CpUtf8 b = new CpUtf8("b");
		pool.add(a);
		pool.add(b);

		var it = pool.listIterator();
		assertEquals(a, it.next());
		CpLong longEntry = new CpLong(5L);
		it.set(longEntry);

		// Backing is [zero, long, padding, b]; iteration continues at b.
		assertEquals(4, pool.size());
		assertEquals(longEntry, pool.get(1));
		assertNull(pool.get(2));
		assertEquals(b, pool.get(3));
		assertEquals(b, it.next());
	}

	@Test
	public void testIteratorSetWideToNarrow() {
		ConstPool pool = new ConstPool();
		pool.add(new CpLong(1L));
		CpUtf8 b = new CpUtf8("b");
		pool.add(b);

		var it = pool.listIterator();
		assertEquals(pool.get(1), it.next());
		CpUtf8 x = new CpUtf8("x");
		it.set(x);

		// Backing is [zero, x, b]; iteration continues at b.
		assertEquals(3, pool.size());
		assertEquals(x, pool.get(1));
		assertEquals(b, pool.get(2));
		assertEquals(b, it.next());
	}

	@Test
	public void testIteratorAddInsertsBeforeNext() {
		ConstPool pool = new ConstPool();
		CpUtf8 a = new CpUtf8("a");
		CpUtf8 b = new CpUtf8("b");
		pool.add(a);
		pool.add(b);

		var it = pool.listIterator();
		assertEquals(a, it.next());
		CpLong longEntry = new CpLong(7L);
		it.add(longEntry);

		// Backing is [zero, a, long, padding, b]; iteration continues at b.
		assertEquals(5, pool.size());
		assertEquals(a, pool.get(1));
		assertEquals(longEntry, pool.get(2));
		assertNull(pool.get(3));
		assertEquals(b, pool.get(4));
		assertEquals(b, it.next());
	}

	@Test
	public void testBackwardIterationSkipsPadding() {
		ConstPool pool = new ConstPool();
		CpUtf8 a = new CpUtf8("a");
		CpLong longEntry = new CpLong(1L);
		CpUtf8 b = new CpUtf8("b");
		pool.add(a);
		pool.add(longEntry);
		pool.add(b);

		var it = pool.listIterator(pool.size());
		assertEquals(b, it.previous());
		assertEquals(longEntry, it.previous());
		assertEquals(a, it.previous());
	}

	@Test
	public void testIndexedAddAllPreservesOrder() {
		ConstPool pool = new ConstPool();
		CpUtf8 seed = new CpUtf8("seed");
		pool.add(seed);

		CpUtf8 a = new CpUtf8("a");
		CpLong longEntry = new CpLong(1L);
		CpUtf8 b = new CpUtf8("b");

		pool.addAll(2, List.of(a, longEntry, b));

		// Backing is [zero, seed, a, long, padding, b]; collection order is preserved.
		assertEquals(6, pool.size());
		assertEquals(seed, pool.get(1));
		assertEquals(a, pool.get(2));
		assertEquals(longEntry, pool.get(3));
		assertNull(pool.get(4));
		assertEquals(b, pool.get(5));
		assertEquals(2, a.getIndex());
		assertEquals(3, longEntry.getIndex());
		assertEquals(5, b.getIndex());
	}

	@Test
	public void testIndexedAddAllEmptyReturnsFalse() {
		ConstPool pool = new ConstPool();
		CpUtf8 seed = new CpUtf8("seed");
		pool.add(seed);

		assertFalse(pool.addAll(1, List.of()));
		assertEquals(2, pool.size());
	}

	@Test
	public void testIndexedAddAllRejectsZeroSlot() {
		ConstPool pool = new ConstPool();
		assertThrows(IndexOutOfBoundsException.class, () -> pool.addAll(0, List.of(new CpInt(1))));
	}
}
