package j9compat;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * Java 8-compatible backport of {@link java.lang.module.ModuleReference}.
 */
public abstract class ModuleReference {

    private final ModuleDescriptor descriptor;
    private final URI location;

    protected ModuleReference(ModuleDescriptor descriptor, URI location) {
        this.descriptor = descriptor;
        this.location = location;
    }

    public final ModuleDescriptor descriptor() {
        return descriptor;
    }

    public final Optional<URI> location() {
        return Optional.ofNullable(location);
    }

    public abstract ModuleReader open() throws IOException;
}
