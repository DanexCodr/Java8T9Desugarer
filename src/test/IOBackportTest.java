package test;

import j9compat.IOBackport;

import java.io.*;
import java.util.Arrays;

import static test.BackportTestRunner.*;

/**
 * Tests for {@link j9compat.IOBackport}.
 *
 * Covers {@code transferTo}, {@code readAllBytes}, and both overloads of
 * {@code readNBytes}.
 */
public final class IOBackportTest {

    static void run() throws Exception {
        section("IOBackport – transferTo");
        testTransferTo();

        section("IOBackport – readAllBytes");
        testReadAllBytes();

        section("IOBackport – readNBytes(buf,off,len)");
        testReadNBytesBuffered();

        section("IOBackport – readNBytes(n)");
        testReadNBytesCount();
    }

    // ── transferTo ────────────────────────────────────────────────────────────

    static void testTransferTo() throws Exception {
        // Basic transfer
        byte[] data = "Hello, World!".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long transferred = IOBackport.transferTo(in, out);
        assertEquals((long) data.length, transferred, "transferTo: returns byte count");
        assertTrue(Arrays.equals(data, out.toByteArray()),
                "transferTo: bytes transferred correctly");

        // Empty stream
        in = new ByteArrayInputStream(new byte[0]);
        out = new ByteArrayOutputStream();
        transferred = IOBackport.transferTo(in, out);
        assertEquals(0L, transferred, "transferTo: empty stream transfers 0 bytes");
        assertEquals(0, out.size(), "transferTo: empty stream produces empty output");

        // Large transfer (crosses internal buffer boundary)
        byte[] large = new byte[20000];
        for (int i = 0; i < large.length; i++) large[i] = (byte) (i % 256);
        in = new ByteArrayInputStream(large);
        out = new ByteArrayOutputStream();
        transferred = IOBackport.transferTo(in, out);
        assertEquals((long) large.length, transferred, "transferTo: large data byte count");
        assertTrue(Arrays.equals(large, out.toByteArray()),
                "transferTo: large data transferred correctly");

        // Null parameter checks
        final InputStream[] inRef = {null};
        final OutputStream[] outRef = {new ByteArrayOutputStream()};
        assertThrows(NullPointerException.class,
                () -> {
                    try { IOBackport.transferTo(inRef[0], outRef[0]); }
                    catch (IOException e) { throw new RuntimeException(e); }
                },
                "transferTo(null in): throws NPE");

        final InputStream validIn = new ByteArrayInputStream(new byte[]{1});
        assertThrows(NullPointerException.class,
                () -> {
                    try { IOBackport.transferTo(validIn, null); }
                    catch (IOException e) { throw new RuntimeException(e); }
                },
                "transferTo(null out): throws NPE");
    }

    // ── readAllBytes ──────────────────────────────────────────────────────────

    static void testReadAllBytes() throws Exception {
        // Basic read
        byte[] data = "test data".getBytes("UTF-8");
        byte[] result = IOBackport.readAllBytes(new ByteArrayInputStream(data));
        assertTrue(Arrays.equals(data, result), "readAllBytes: returns all bytes");

        // Empty stream
        byte[] empty = IOBackport.readAllBytes(new ByteArrayInputStream(new byte[0]));
        assertEquals(0, empty.length, "readAllBytes: empty stream returns empty array");

        // Large data (crosses buffer boundary)
        byte[] large = new byte[20000];
        for (int i = 0; i < large.length; i++) large[i] = (byte) (i % 127);
        byte[] largeResult = IOBackport.readAllBytes(new ByteArrayInputStream(large));
        assertTrue(Arrays.equals(large, largeResult),
                "readAllBytes: large data read correctly");

        // Binary data (all byte values)
        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) allBytes[i] = (byte) i;
        byte[] allResult = IOBackport.readAllBytes(new ByteArrayInputStream(allBytes));
        assertTrue(Arrays.equals(allBytes, allResult),
                "readAllBytes: all 256 byte values preserved");

        // Null check
        assertThrows(NullPointerException.class,
                () -> {
                    try { IOBackport.readAllBytes(null); }
                    catch (IOException e) { throw new RuntimeException(e); }
                },
                "readAllBytes(null): throws NPE");
    }

    // ── readNBytes(buf, off, len) ─────────────────────────────────────────────

    static void testReadNBytesBuffered() throws Exception {
        byte[] src = "ABCDEFGHIJ".getBytes("UTF-8");

        // Read all into exact-size buffer
        byte[] buf = new byte[src.length];
        int read = IOBackport.readNBytes(new ByteArrayInputStream(src), buf, 0, buf.length);
        assertEquals(src.length, read, "readNBytes(buf): returns full length");
        assertTrue(Arrays.equals(src, buf), "readNBytes(buf): buffer filled correctly");

        // Read partial (offset + smaller len)
        byte[] buf2 = new byte[10];
        int read2 = IOBackport.readNBytes(new ByteArrayInputStream(src), buf2, 2, 5);
        assertEquals(5, read2, "readNBytes(buf,off,len): returns len read");
        assertEquals((byte) 'A', buf2[2], "readNBytes(buf,off,len): offset 2 is 'A'");
        assertEquals((byte) 'E', buf2[6], "readNBytes(buf,off,len): offset 6 is 'E'");

        // Request more bytes than available
        byte[] buf3 = new byte[20];
        int read3 = IOBackport.readNBytes(new ByteArrayInputStream(src), buf3, 0, 20);
        assertEquals(src.length, read3,
                "readNBytes(buf): returns actual bytes when fewer than requested");

        // Zero-length request
        byte[] buf4 = new byte[5];
        int read4 = IOBackport.readNBytes(new ByteArrayInputStream(src), buf4, 0, 0);
        assertEquals(0, read4, "readNBytes(buf,off,0): returns 0");

        // Empty stream
        byte[] buf5 = new byte[5];
        int read5 = IOBackport.readNBytes(new ByteArrayInputStream(new byte[0]), buf5, 0, 5);
        assertEquals(0, read5, "readNBytes on empty stream: returns 0");

        // Invalid off/len parameters
        assertThrows(IndexOutOfBoundsException.class,
                () -> {
                    try { IOBackport.readNBytes(new ByteArrayInputStream(src), new byte[5], -1, 3); }
                    catch (IOException e) { throw new RuntimeException(e); }
                },
                "readNBytes(buf,neg-off,len): throws IOOBE");
        assertThrows(IndexOutOfBoundsException.class,
                () -> {
                    try { IOBackport.readNBytes(new ByteArrayInputStream(src), new byte[5], 0, -1); }
                    catch (IOException e) { throw new RuntimeException(e); }
                },
                "readNBytes(buf,off,neg-len): throws IOOBE");
        assertThrows(IndexOutOfBoundsException.class,
                () -> {
                    try { IOBackport.readNBytes(new ByteArrayInputStream(src), new byte[5], 4, 3); }
                    catch (IOException e) { throw new RuntimeException(e); }
                },
                "readNBytes(buf,off,len): off+len > buf.length throws IOOBE");

        // Null checks
        assertThrows(NullPointerException.class,
                () -> {
                    try { IOBackport.readNBytes(null, new byte[5], 0, 5); }
                    catch (IOException e) { throw new RuntimeException(e); }
                },
                "readNBytes(null in): throws NPE");
        assertThrows(NullPointerException.class,
                () -> {
                    try { IOBackport.readNBytes(new ByteArrayInputStream(src), null, 0, 5); }
                    catch (IOException e) { throw new RuntimeException(e); }
                },
                "readNBytes(null buf): throws NPE");
    }

    // ── readNBytes(n) ─────────────────────────────────────────────────────────

    static void testReadNBytesCount() throws Exception {
        byte[] src = "ABCDEFGHIJ".getBytes("UTF-8");

        // Read exactly n bytes
        byte[] result = IOBackport.readNBytes(new ByteArrayInputStream(src), 5);
        assertEquals(5, result.length, "readNBytes(n): length 5");
        assertEquals((byte) 'A', result[0], "readNBytes(n): first byte is A");
        assertEquals((byte) 'E', result[4], "readNBytes(n): fifth byte is E");

        // Request more than available → returns what's available
        byte[] result2 = IOBackport.readNBytes(new ByteArrayInputStream(src), 100);
        assertEquals(src.length, result2.length,
                "readNBytes(n>available): returns actual available bytes");
        assertTrue(Arrays.equals(src, result2),
                "readNBytes(n>available): content matches source");

        // Zero bytes
        byte[] zero = IOBackport.readNBytes(new ByteArrayInputStream(src), 0);
        assertEquals(0, zero.length, "readNBytes(0): returns empty array");

        // Empty stream
        byte[] fromEmpty = IOBackport.readNBytes(new ByteArrayInputStream(new byte[0]), 10);
        assertEquals(0, fromEmpty.length, "readNBytes on empty stream: returns empty array");

        // Negative n is illegal
        assertThrows(IllegalArgumentException.class,
                () -> {
                    try { IOBackport.readNBytes(new ByteArrayInputStream(src), -1); }
                    catch (IOException e) { throw new RuntimeException(e); }
                },
                "readNBytes(negative n): throws IAE");

        // Null check
        assertThrows(NullPointerException.class,
                () -> {
                    try { IOBackport.readNBytes((InputStream) null, 5); }
                    catch (IOException e) { throw new RuntimeException(e); }
                },
                "readNBytes(null, n): throws NPE");

        // Large read (crosses internal buffer)
        byte[] large = new byte[20000];
        for (int i = 0; i < large.length; i++) large[i] = (byte) (i % 200);
        byte[] largeResult = IOBackport.readNBytes(new ByteArrayInputStream(large), large.length);
        assertTrue(Arrays.equals(large, largeResult),
                "readNBytes(large n): reads all bytes correctly");
    }
}
