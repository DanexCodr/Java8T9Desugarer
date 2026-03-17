package j9compat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Java 8-compatible backport of {@link java.lang.module.ModuleFinder}.
 */
public interface ModuleFinder {

    Optional<ModuleReference> find(String name);

    Set<ModuleReference> findAll();

    static ModuleFinder ofSystem() {
        return empty();
    }

    static ModuleFinder of(java.nio.file.Path... paths) {
        return empty();
    }

    static ModuleFinder compose(ModuleFinder... finders) {
        if (finders == null || finders.length == 0) {
            return empty();
        }
        return new CompositeModuleFinder(finders);
    }

    static ModuleFinder empty() {
        return new EmptyModuleFinder();
    }
}

final class EmptyModuleFinder implements ModuleFinder {
    @Override
    public Optional<ModuleReference> find(String name) {
        return Optional.empty();
    }

    @Override
    public Set<ModuleReference> findAll() {
        return Collections.emptySet();
    }
}

final class CompositeModuleFinder implements ModuleFinder {
    private final ModuleFinder[] finders;

    CompositeModuleFinder(ModuleFinder[] finders) {
        this.finders = finders;
    }

    @Override
    public Optional<ModuleReference> find(String name) {
        if (finders != null) {
            for (ModuleFinder finder : finders) {
                Optional<ModuleReference> found = finder.find(name);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Set<ModuleReference> findAll() {
        Set<ModuleReference> all = new HashSet<ModuleReference>();
        if (finders != null) {
            for (ModuleFinder finder : finders) {
                all.addAll(finder.findAll());
            }
        }
        return all;
    }
}
