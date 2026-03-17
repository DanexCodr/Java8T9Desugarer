package j9compat;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Java 8-compatible backport of {@link java.lang.module.Configuration}.
 */
public final class Configuration {

    private final List<Configuration> parents;
    private final Set<ResolvedModule> modules;

    Configuration(List<Configuration> parents, Set<ResolvedModule> modules) {
        this.parents = parents == null ? Collections.<Configuration>emptyList()
                : Collections.unmodifiableList(new ArrayList<Configuration>(parents));
        this.modules = modules == null ? Collections.<ResolvedModule>emptySet()
                : Collections.unmodifiableSet(new HashSet<ResolvedModule>(modules));
    }

    public Configuration resolve(ModuleFinder before,
                                 ModuleFinder after,
                                 java.util.Collection<String> roots) {
        return this;
    }

    public Configuration resolveAndBind(ModuleFinder before,
                                        ModuleFinder after,
                                        java.util.Collection<String> roots) {
        return this;
    }

    static Configuration resolveAndBind(ModuleFinder finder,
                                        java.util.Collection<String> roots,
                                        PrintStream out) {
        return empty();
    }

    public static Configuration resolve(ModuleFinder before,
                                        List<Configuration> parents,
                                        ModuleFinder after,
                                        java.util.Collection<String> roots) {
        return new Configuration(parents, Collections.<ResolvedModule>emptySet());
    }

    public static Configuration resolveAndBind(ModuleFinder before,
                                               List<Configuration> parents,
                                               ModuleFinder after,
                                               java.util.Collection<String> roots) {
        return new Configuration(parents, Collections.<ResolvedModule>emptySet());
    }

    public static Configuration empty() {
        return new Configuration(Collections.<Configuration>emptyList(),
                Collections.<ResolvedModule>emptySet());
    }

    public List<Configuration> parents() {
        return parents;
    }

    public Set<ResolvedModule> modules() {
        return modules;
    }

    public Optional<ResolvedModule> findModule(String name) {
        if (name == null) {
            return Optional.<ResolvedModule>empty();
        }
        for (ResolvedModule module : modules) {
            if (name.equals(module.name())) {
                return Optional.of(module);
            }
        }
        return Optional.<ResolvedModule>empty();
    }

    Set<ModuleDescriptor> descriptors() {
        Set<ModuleDescriptor> descriptors = new HashSet<ModuleDescriptor>();
        for (ResolvedModule module : modules) {
            ModuleDescriptor descriptor = module.descriptor();
            if (descriptor != null) {
                descriptors.add(descriptor);
            }
        }
        return descriptors;
    }

    Set<ResolvedModule> reads(ResolvedModule module) {
        return Collections.emptySet();
    }

    Stream<Configuration> configurations() {
        return Stream.concat(Stream.of(this),
                parents.stream().flatMap(Configuration::configurations));
    }

    @Override
    public String toString() {
        return "Configuration[" + modules + "]";
    }
}
