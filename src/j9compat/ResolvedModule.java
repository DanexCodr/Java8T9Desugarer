package j9compat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Java 8-compatible backport of {@link java.lang.module.ResolvedModule}.
 */
public final class ResolvedModule {

    private final Configuration configuration;
    private final ModuleReference reference;

    ResolvedModule(Configuration configuration, ModuleReference reference) {
        this.configuration = configuration;
        this.reference = reference;
    }

    public Configuration configuration() {
        return configuration;
    }

    public ModuleReference reference() {
        return reference;
    }

    ModuleDescriptor descriptor() {
        return reference != null ? reference.descriptor() : null;
    }

    public String name() {
        ModuleDescriptor descriptor = descriptor();
        return descriptor != null ? descriptor.name() : null;
    }

    public Set<ResolvedModule> reads() {
        return Collections.unmodifiableSet(new HashSet<ResolvedModule>());
    }

    @Override
    public int hashCode() {
        return reference == null ? 0 : reference.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ResolvedModule)) return false;
        ResolvedModule other = (ResolvedModule) obj;
        if (reference == null) {
            return other.reference == null;
        }
        return reference.equals(other.reference);
    }

    @Override
    public String toString() {
        return "ResolvedModule[" + name() + "]";
    }
}
