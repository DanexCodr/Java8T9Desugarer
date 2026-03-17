package test;

import j9compat.MethodHandlesBackport;
import j9compat.VarHandle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static test.BackportTestRunner.*;

/**
 * Tests for {@link j9compat.VarHandle} and MethodHandles backports.
 */
public final class VarHandleBackportTest {

    static void run() {
        section("VarHandleBackport");

        testFieldHandle();
        testArrayHandle();
        testMethodHandlesLookup();
    }

    private static void testFieldHandle() {
        Holder holder = new Holder();
        try {
            VarHandle handle = MethodHandlesBackport.findVarHandle(MethodHandles.lookup(),
                    Holder.class, "value", int.class);
            handle.set(holder, 4);
            assertEquals(4, handle.get(holder), "VarHandle.set/get: updates value");
            assertTrue(handle.compareAndSet(holder, 4, 7),
                    "VarHandle.compareAndSet: succeeds");
            assertEquals(7, handle.get(holder), "VarHandle.compareAndSet: updates value");
            Object previous = handle.getAndAdd(holder, 3);
            assertEquals(7, previous, "VarHandle.getAndAdd: returns previous");
            assertEquals(10, handle.get(holder), "VarHandle.getAndAdd: updates value");
        } catch (Exception e) {
            fail("VarHandle field access threw exception: " + e.getMessage());
        }
    }

    private static void testArrayHandle() {
        int[] values = new int[]{1, 2, 3};
        VarHandle handle = MethodHandlesBackport.arrayElementVarHandle(int[].class);
        handle.set(values, 1, 9);
        assertEquals(9, handle.get(values, 1), "VarHandle array: set/get element");
    }

    private static void testMethodHandlesLookup() {
        try {
            MethodHandle handle = MethodHandlesBackport.findVirtual(MethodHandles.lookup(),
                    Stream.class,
                    "takeWhile",
                    MethodType.methodType(Stream.class, Predicate.class));
            Stream<Integer> input = Stream.of(1, 2, 3, 0, 4);
            @SuppressWarnings("unchecked")
            Stream<Integer> output = (Stream<Integer>) handle.invokeWithArguments(
                    input, (Predicate<Integer>) value -> value > 0);
            long count = output.count();
            assertEquals(3L, count, "MethodHandlesBackport.findVirtual: takeWhile works");
        } catch (Throwable e) {
            fail("MethodHandlesBackport.findVirtual threw exception: " + e.getMessage());
        }
    }

    private static final class Holder {
        int value;
    }
}
