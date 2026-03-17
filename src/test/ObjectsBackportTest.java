package test;

import j9compat.ObjectsBackport;

import static test.BackportTestRunner.*;

/**
 * Tests for {@link j9compat.ObjectsBackport}.
 *
 * Focuses on null-safety contracts, return-value contracts, and the
 * integer-overflow edge cases in the index-range checks.
 */
public final class ObjectsBackportTest {

    static void run() {
        section("ObjectsBackport");

        testRequireNonNullElse();
        testRequireNonNullElseGet();
        testCheckIndex();
        testCheckFromToIndex();
        testCheckFromIndexSize();
    }

    // ── requireNonNullElse ───────────────────────────────────────────────────

    static void testRequireNonNullElse() {
        assertEquals("hello",
                ObjectsBackport.requireNonNullElse("hello", "default"),
                "requireNonNullElse: non-null obj returns obj");

        assertEquals("default",
                ObjectsBackport.requireNonNullElse(null, "default"),
                "requireNonNullElse: null obj returns defaultObj");

        assertThrows(NullPointerException.class,
                () -> ObjectsBackport.requireNonNullElse(null, null),
                "requireNonNullElse: both null throws NPE");

        // Numeric types
        assertEquals(42,
                ObjectsBackport.requireNonNullElse(42, 0),
                "requireNonNullElse: non-null Integer returned");

        assertEquals(0,
                ObjectsBackport.requireNonNullElse(null, 0),
                "requireNonNullElse: null Integer falls back to default");
    }

    // ── requireNonNullElseGet ────────────────────────────────────────────────

    static void testRequireNonNullElseGet() {
        assertEquals("hello",
                ObjectsBackport.requireNonNullElseGet("hello", () -> "default"),
                "requireNonNullElseGet: non-null obj returned");

        assertEquals("computed",
                ObjectsBackport.requireNonNullElseGet(null, () -> "computed"),
                "requireNonNullElseGet: null obj uses supplier");

        assertThrows(NullPointerException.class,
                () -> ObjectsBackport.requireNonNullElseGet(null, null),
                "requireNonNullElseGet: null supplier throws NPE");

        assertThrows(NullPointerException.class,
                () -> ObjectsBackport.requireNonNullElseGet(null, () -> null),
                "requireNonNullElseGet: supplier returning null throws NPE");

        // Supplier must NOT be called when obj is non-null
        boolean[] supplierCalled = {false};
        ObjectsBackport.requireNonNullElseGet("present", () -> {
            supplierCalled[0] = true;
            return "lazy";
        });
        assertTrue(!supplierCalled[0],
                "requireNonNullElseGet: supplier not called when obj non-null");
    }

    // ── checkIndex ───────────────────────────────────────────────────────────

    static void testCheckIndex() {
        assertEquals(0, ObjectsBackport.checkIndex(0, 1),
                "checkIndex: 0 in length-1 is valid");
        assertEquals(4, ObjectsBackport.checkIndex(4, 5),
                "checkIndex: last index in length-5 is valid");
        assertEquals(0, ObjectsBackport.checkIndex(0, Integer.MAX_VALUE),
                "checkIndex: 0 in MAX_VALUE length is valid");

        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkIndex(-1, 5),
                "checkIndex: negative index throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkIndex(5, 5),
                "checkIndex: index == length throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkIndex(6, 5),
                "checkIndex: index > length throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkIndex(0, 0),
                "checkIndex: any index in empty range throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkIndex(0, -1),
                "checkIndex: negative length throws");
    }

    // ── checkFromToIndex ─────────────────────────────────────────────────────

    static void testCheckFromToIndex() {
        assertEquals(0, ObjectsBackport.checkFromToIndex(0, 5, 10),
                "checkFromToIndex: valid [0,5) in 10");
        assertEquals(3, ObjectsBackport.checkFromToIndex(3, 3, 10),
                "checkFromToIndex: empty sub-range [3,3) is valid");
        assertEquals(0, ObjectsBackport.checkFromToIndex(0, 0, 0),
                "checkFromToIndex: empty sub-range [0,0) in 0 is valid");
        assertEquals(0, ObjectsBackport.checkFromToIndex(0, 10, 10),
                "checkFromToIndex: [0,10) in 10 is valid");

        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromToIndex(-1, 5, 10),
                "checkFromToIndex: negative fromIndex throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromToIndex(6, 5, 10),
                "checkFromToIndex: fromIndex > toIndex throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromToIndex(0, 11, 10),
                "checkFromToIndex: toIndex > length throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromToIndex(0, 5, -1),
                "checkFromToIndex: negative length throws");
    }

    // ── checkFromIndexSize ───────────────────────────────────────────────────

    static void testCheckFromIndexSize() {
        // Basic valid cases
        assertEquals(0, ObjectsBackport.checkFromIndexSize(0, 5, 10),
                "checkFromIndexSize: [0,5) in 10 is valid");
        assertEquals(5, ObjectsBackport.checkFromIndexSize(5, 5, 10),
                "checkFromIndexSize: [5,10) in 10 is valid");
        assertEquals(0, ObjectsBackport.checkFromIndexSize(0, 0, 0),
                "checkFromIndexSize: empty [0,0) in 0 is valid");
        assertEquals(0, ObjectsBackport.checkFromIndexSize(0, 0, 10),
                "checkFromIndexSize: zero-size at start is valid");
        assertEquals(10, ObjectsBackport.checkFromIndexSize(10, 0, 10),
                "checkFromIndexSize: zero-size at end is valid");
        assertEquals(0, ObjectsBackport.checkFromIndexSize(0, Integer.MAX_VALUE, Integer.MAX_VALUE),
                "checkFromIndexSize: [0,MAX) in MAX is valid");

        // Boundary: fromIndex + size == length
        assertEquals(3, ObjectsBackport.checkFromIndexSize(3, 7, 10),
                "checkFromIndexSize: [3,10) in 10 is valid (exact fit)");

        // Must throw
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromIndexSize(-1, 5, 10),
                "checkFromIndexSize: negative fromIndex throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromIndexSize(0, -1, 10),
                "checkFromIndexSize: negative size throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromIndexSize(0, 5, -1),
                "checkFromIndexSize: negative length throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromIndexSize(6, 5, 10),
                "checkFromIndexSize: [6,11) exceeds 10 throws");
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromIndexSize(0, 11, 10),
                "checkFromIndexSize: size > length throws");

        // Integer overflow / edge-case values that would mislead naive
        // (fromIndex < 0 || size < 0 || fromIndex > length - size) check:
        // length = Integer.MIN_VALUE is negative → must throw
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromIndexSize(0, 1, Integer.MIN_VALUE),
                "checkFromIndexSize: MIN_VALUE length throws (overflow guard)");
        // length = 0, size = MAX_VALUE → fromIndex(0) + MAX_VALUE > 0 → throws
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromIndexSize(0, Integer.MAX_VALUE, 0),
                "checkFromIndexSize: size > length throws when length=0");
        // fromIndex = MAX_VALUE, size = 1 → sum overflows, still must throw
        assertThrows(IndexOutOfBoundsException.class,
                () -> ObjectsBackport.checkFromIndexSize(Integer.MAX_VALUE, 1, Integer.MAX_VALUE),
                "checkFromIndexSize: fromIndex+size overflow throws");
    }
}
