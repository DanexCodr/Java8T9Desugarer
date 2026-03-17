package j9compat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Java 8-compatible backport of {@link java.lang.Module}.
 */
public final class Module implements AnnotatedElement {

    private final String name;
    private final ClassLoader classLoader;
    private final ModuleDescriptor descriptor;
    private final ModuleLayer layer;
    private final Set<String> packages;

    Module(String name, ClassLoader classLoader, ModuleDescriptor descriptor,
           ModuleLayer layer, Set<String> packages) {
        this.name = name;
        this.classLoader = classLoader;
        this.descriptor = descriptor;
        this.layer = layer;
        this.packages = packages == null ? Collections.<String>emptySet()
                : Collections.unmodifiableSet(new HashSet<String>(packages));
    }

    public boolean isNamed() {
        return name != null && name.length() > 0;
    }

    public String getName() {
        return name;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ModuleDescriptor getDescriptor() {
        return descriptor;
    }

    public ModuleLayer getLayer() {
        return layer != null ? layer : ModuleLayer.boot();
    }

    public boolean canRead(Module other) {
        return true;
    }

    public Module addReads(Module other) {
        return this;
    }

    public boolean isExported(String pkg, Module other) {
        return isExported(pkg);
    }

    public boolean isOpen(String pkg, Module other) {
        return isOpen(pkg);
    }

    public boolean isExported(String pkg) {
        return pkg != null && (packages.isEmpty() || packages.contains(pkg));
    }

    public boolean isOpen(String pkg) {
        return isExported(pkg);
    }

    public Module addExports(String pkg, Module other) {
        return this;
    }

    public Module addOpens(String pkg, Module other) {
        return this;
    }

    public Module addUses(Class<?> service) {
        return this;
    }

    public boolean canUse(Class<?> service) {
        return true;
    }

    public Set<String> getPackages() {
        return packages;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
    }

    public InputStream getResourceAsStream(String name) throws IOException {
        if (name == null) {
            return null;
        }
        if (classLoader != null) {
            return classLoader.getResourceAsStream(name);
        }
        return ClassLoader.getSystemResourceAsStream(name);
    }

    @Override
    public String toString() {
        return isNamed() ? "module " + name : "unnamed module";
    }

    static Module unnamed(ClassLoader loader, ModuleLayer layer, Set<String> packages) {
        return new Module(null, loader, ModuleDescriptor.unnamed(), layer, packages);
    }

    static Module named(ModuleDescriptor descriptor, ClassLoader loader, ModuleLayer layer) {
        String name = descriptor != null ? descriptor.name() : null;
        Set<String> packages = descriptor != null ? descriptor.packages() : Collections.<String>emptySet();
        return new Module(name, loader, descriptor, layer, packages);
    }
}
