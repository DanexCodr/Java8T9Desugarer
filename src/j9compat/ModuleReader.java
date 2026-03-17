package j9compat;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Java 8-compatible backport of {@link java.lang.module.ModuleReader}.
 */
public interface ModuleReader extends Closeable {

    Optional<URI> find(String name) throws IOException;

    default Optional<InputStream> open(String name) throws IOException {
        Optional<URI> found = find(name);
        if (!found.isPresent()) {
            return Optional.empty();
        }
        return Optional.ofNullable(found.get().toURL().openStream());
    }

    default Optional<ByteBuffer> read(String name) throws IOException {
        Optional<InputStream> stream = open(name);
        if (!stream.isPresent()) {
            return Optional.empty();
        }
        try (InputStream in = stream.get()) {
            byte[] bytes = readAllBytes(in);
            return Optional.of(ByteBuffer.wrap(bytes));
        }
    }

    default void release(ByteBuffer buffer) {
        // no-op
    }

    Stream<String> list() throws IOException;

    @Override
    void close() throws IOException;

    static byte[] readAllBytes(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        int read;
        while ((read = in.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }
}
