package test;

import j9compat.StreamBackport;

import java.util.*;
import java.util.stream.*;

import static test.BackportTestRunner.*;

/**
 * Tests for {@link j9compat.StreamBackport}.
 *
 * The "hard" methods are {@code takeWhile}, {@code dropWhile}, and in
 * particular the stateful spliterator in {@code iterate}, which must handle
 * the seed, the hasNext predicate, and the advancing function correctly under
 * various early-termination conditions.
 */
public final class StreamBackportTest {

    static void run() {
        section("StreamBackport – ofNullable");
        testOfNullable();

        section("StreamBackport – takeWhile");
        testTakeWhile();

        section("StreamBackport – dropWhile");
        testDropWhile();

        section("StreamBackport – iterate");
        testIterate();
    }

    // ── ofNullable ────────────────────────────────────────────────────────────

    static void testOfNullable() {
        List<String> present = StreamBackport.<String>ofNullable("hello")
                .collect(Collectors.toList());
        assertEquals(1, present.size(), "ofNullable(non-null): size 1");
        assertEquals("hello", present.get(0), "ofNullable(non-null): element");

        List<String> absent = StreamBackport.<String>ofNullable(null)
                .collect(Collectors.toList());
        assertTrue(absent.isEmpty(), "ofNullable(null): empty stream");
    }

    // ── takeWhile ─────────────────────────────────────────────────────────────

    static void testTakeWhile() {
        // Basic usage: take while positive
        List<Integer> result = StreamBackport.takeWhile(
                        Stream.of(1, 2, 3, -1, 4, 5),
                        n -> n > 0)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(1, 2, 3), result,
                "takeWhile: stops at first failing element");

        // All elements match
        List<Integer> allMatch = StreamBackport.takeWhile(
                        Stream.of(1, 2, 3),
                        n -> n > 0)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(1, 2, 3), allMatch,
                "takeWhile: returns all when all match");

        // First element fails
        List<Integer> noneMatch = StreamBackport.takeWhile(
                        Stream.of(-1, 2, 3),
                        n -> n > 0)
                .collect(Collectors.toList());
        assertTrue(noneMatch.isEmpty(),
                "takeWhile: empty result when first element fails");

        // Empty stream
        List<Integer> empty = StreamBackport.takeWhile(
                        Stream.<Integer>empty(),
                        n -> true)
                .collect(Collectors.toList());
        assertTrue(empty.isEmpty(), "takeWhile: empty input yields empty output");

        // Null checks
        assertThrows(NullPointerException.class,
                () -> StreamBackport.takeWhile(null, n -> true),
                "takeWhile(null stream): throws NPE");
        assertThrows(NullPointerException.class,
                () -> StreamBackport.takeWhile(Stream.of(1), null),
                "takeWhile(null predicate): throws NPE");

        // Single element matching
        List<String> single = StreamBackport.takeWhile(
                        Stream.of("a"),
                        s -> !s.isEmpty())
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList("a"), single,
                "takeWhile: single matching element returned");

        // Single element not matching
        List<String> singleFail = StreamBackport.takeWhile(
                        Stream.of(""),
                        s -> !s.isEmpty())
                .collect(Collectors.toList());
        assertTrue(singleFail.isEmpty(),
                "takeWhile: single non-matching element gives empty result");

        // Order is preserved
        List<Integer> ordered = StreamBackport.takeWhile(
                        Stream.of(5, 4, 3, 2, 1, 0),
                        n -> n > 2)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(5, 4, 3), ordered,
                "takeWhile: order preserved");
    }

    // ── dropWhile ─────────────────────────────────────────────────────────────

    static void testDropWhile() {
        // Basic usage: drop while positive
        List<Integer> result = StreamBackport.dropWhile(
                        Stream.of(1, 2, 3, -1, 4, 5),
                        n -> n > 0)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(-1, 4, 5), result,
                "dropWhile: drops prefix matching predicate");

        // All elements match (should return empty)
        List<Integer> allMatch = StreamBackport.dropWhile(
                        Stream.of(1, 2, 3),
                        n -> n > 0)
                .collect(Collectors.toList());
        assertTrue(allMatch.isEmpty(),
                "dropWhile: all match gives empty result");

        // First element fails (drop nothing)
        List<Integer> noneDropped = StreamBackport.dropWhile(
                        Stream.of(-1, 2, 3),
                        n -> n > 0)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(-1, 2, 3), noneDropped,
                "dropWhile: nothing dropped when first fails");

        // Empty stream
        List<Integer> empty = StreamBackport.dropWhile(
                        Stream.<Integer>empty(),
                        n -> true)
                .collect(Collectors.toList());
        assertTrue(empty.isEmpty(), "dropWhile: empty input yields empty output");

        // Null checks
        assertThrows(NullPointerException.class,
                () -> StreamBackport.dropWhile(null, n -> true),
                "dropWhile(null stream): throws NPE");
        assertThrows(NullPointerException.class,
                () -> StreamBackport.dropWhile(Stream.of(1), null),
                "dropWhile(null predicate): throws NPE");

        // After the failing element, subsequent matches should still be included
        List<Integer> afterFail = StreamBackport.dropWhile(
                        Stream.of(1, 2, -1, 3, 4),
                        n -> n > 0)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(-1, 3, 4), afterFail,
                "dropWhile: elements after first failure always included");

        // Order is preserved
        List<Integer> ordered = StreamBackport.dropWhile(
                        Stream.of(1, 2, 3, 4, 5),
                        n -> n < 3)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(3, 4, 5), ordered,
                "dropWhile: order preserved after drop");
    }

    // ── iterate ───────────────────────────────────────────────────────────────

    static void testIterate() {
        // Standard counting loop: 0..4
        List<Integer> counted = StreamBackport.iterate(0, n -> n < 5, n -> n + 1)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), counted,
                "iterate: 0 to 4 inclusive");

        // Seed fails immediately → empty stream
        List<Integer> emptySeed = StreamBackport.iterate(10, n -> n < 5, n -> n + 1)
                .collect(Collectors.toList());
        assertTrue(emptySeed.isEmpty(),
                "iterate: empty stream when seed fails hasNext");

        // Single element (seed passes, f(seed) fails)
        List<Integer> singleElem = StreamBackport.iterate(0, n -> n < 1, n -> n + 1)
                .collect(Collectors.toList());
        assertEquals(Collections.singletonList(0), singleElem,
                "iterate: single element when only seed passes");

        // Limit usage (early termination)
        List<Integer> limited = StreamBackport.iterate(0, n -> true, n -> n + 1)
                .limit(5)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), limited,
                "iterate: limit terminates infinite iterate");

        // Seed is null-like but valid (string empty check)
        List<String> strings = StreamBackport.iterate("", s -> s.length() < 4, s -> s + "a")
                .collect(Collectors.toList());
        assertEquals(Arrays.asList("", "a", "aa", "aaa"), strings,
                "iterate: string growth terminates at length 4");

        // Powers of 2 up to 16
        List<Integer> powers = StreamBackport.iterate(1, n -> n <= 16, n -> n * 2)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(1, 2, 4, 8, 16), powers,
                "iterate: powers of 2 up to 16");

        // Null checks
        assertThrows(NullPointerException.class,
                () -> StreamBackport.iterate(0, null, n -> n + 1),
                "iterate(null hasNext): throws NPE");
        assertThrows(NullPointerException.class,
                () -> StreamBackport.iterate(0, n -> n < 5, null),
                "iterate(null f): throws NPE");

        // Sum using reduce – validates lazy evaluation
        int sum = StreamBackport.iterate(1, n -> n <= 100, n -> n + 1)
                .reduce(0, Integer::sum);
        assertEquals(5050, sum, "iterate: sum 1..100 = 5050");

        // Count
        long count = StreamBackport.iterate(0, n -> n < 1000, n -> n + 1).count();
        assertEquals(1000L, count, "iterate: count of 0..999 = 1000");

        // Verify seed is emitted and f(seed) stops when hasNext fails for f(seed)
        List<Integer> identityLimited = StreamBackport.iterate(
                42,
                n -> n == 42,    // passes only for 42
                n -> n + 1       // f(42) = 43, hasNext(43) = false → terminates
        ).collect(Collectors.toList());
        assertEquals(Collections.singletonList(42), identityLimited,
                "iterate: seed emitted once, f(seed) fails hasNext terminates correctly");
    }
}
