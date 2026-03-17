package j9compat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Java 8-compatible backport of {@link java.lang.module.ModuleDescriptor}.
 */
public final class ModuleDescriptor implements Comparable<ModuleDescriptor> {

    private final String name;
    private final Version version;
    private final Set<Modifier> modifiers;
    private final Set<Requires> requires;
    private final Set<Exports> exports;
    private final Set<Opens> opens;
    private final Set<String> uses;
    private final Set<Provides> provides;
    private final Set<String> packages;
    private final String mainClass;
    private final boolean open;
    private final boolean automatic;

    ModuleDescriptor(String name,
                     Version version,
                     Set<Modifier> modifiers,
                     Set<Requires> requires,
                     Set<Exports> exports,
                     Set<Opens> opens,
                     Set<String> uses,
                     Set<Provides> provides,
                     Set<String> packages,
                     String mainClass,
                     boolean open,
                     boolean automatic) {
        this.name = name;
        this.version = version;
        this.modifiers = unmodifiable(modifiers);
        this.requires = unmodifiable(requires);
        this.exports = unmodifiable(exports);
        this.opens = unmodifiable(opens);
        this.uses = unmodifiable(uses);
        this.provides = unmodifiable(provides);
        this.packages = unmodifiable(packages);
        this.mainClass = mainClass;
        this.open = open;
        this.automatic = automatic;
    }

    public String name() {
        return name;
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }

    public boolean isOpen() {
        return open || modifiers.contains(Modifier.OPEN);
    }

    public boolean isAutomatic() {
        return automatic || modifiers.contains(Modifier.AUTOMATIC);
    }

    public Set<Requires> requires() {
        return requires;
    }

    public Set<Exports> exports() {
        return exports;
    }

    public Set<Opens> opens() {
        return opens;
    }

    public Set<String> uses() {
        return uses;
    }

    public Set<Provides> provides() {
        return provides;
    }

    public Optional<Version> version() {
        return Optional.ofNullable(version);
    }

    public Optional<String> rawVersion() {
        return version == null ? Optional.<String>empty() : Optional.of(version.toString());
    }

    public String toNameAndVersion() {
        if (name == null) {
            return "unnamed";
        }
        return version == null ? name : name + "@" + version;
    }

    public Optional<String> mainClass() {
        return Optional.ofNullable(mainClass);
    }

    public Set<String> packages() {
        return packages;
    }

    @Override
    public int compareTo(ModuleDescriptor other) {
        if (other == null) {
            return 1;
        }
        if (name == null) {
            return other.name == null ? 0 : -1;
        }
        if (other.name == null) {
            return 1;
        }
        return name.compareTo(other.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ModuleDescriptor)) return false;
        ModuleDescriptor other = (ModuleDescriptor) obj;
        return safeEquals(name, other.name)
                && safeEquals(version, other.version)
                && modifiers.equals(other.modifiers)
                && requires.equals(other.requires)
                && exports.equals(other.exports)
                && opens.equals(other.opens)
                && uses.equals(other.uses)
                && provides.equals(other.provides)
                && packages.equals(other.packages)
                && safeEquals(mainClass, other.mainClass)
                && open == other.open
                && automatic == other.automatic;
    }

    @Override
    public int hashCode() {
        int result = name == null ? 0 : name.hashCode();
        result = 31 * result + (version == null ? 0 : version.hashCode());
        result = 31 * result + modifiers.hashCode();
        result = 31 * result + requires.hashCode();
        result = 31 * result + exports.hashCode();
        result = 31 * result + opens.hashCode();
        result = 31 * result + uses.hashCode();
        result = 31 * result + provides.hashCode();
        result = 31 * result + packages.hashCode();
        result = 31 * result + (mainClass == null ? 0 : mainClass.hashCode());
        result = 31 * result + (open ? 1 : 0);
        result = 31 * result + (automatic ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return toNameAndVersion();
    }

    public static Builder newModule(String name, Set<Modifier> modifiers) {
        return new Builder(name, modifiers == null ? EnumSet.noneOf(Modifier.class) : modifiers);
    }

    public static Builder newModule(String name) {
        return new Builder(name, EnumSet.noneOf(Modifier.class));
    }

    public static Builder newOpenModule(String name) {
        return new Builder(name, EnumSet.of(Modifier.OPEN));
    }

    public static Builder newAutomaticModule(String name) {
        return new Builder(name, EnumSet.of(Modifier.AUTOMATIC));
    }

    public static ModuleDescriptor read(InputStream in, Supplier<Set<String>> packagesSupplier)
            throws IOException {
        if (in == null) {
            throw new NullPointerException("in");
        }
        Set<String> packages = packagesSupplier != null ? packagesSupplier.get() : Collections.<String>emptySet();
        ModuleInfo info = ModuleInfoParser.parse(in);
        if (info == null) {
            return unnamed(packages);
        }
        Builder builder = newModule(info.name);
        builder.packages(packages);
        return builder.build();
    }

    public static ModuleDescriptor read(InputStream in) throws IOException {
        return read(in, new Supplier<Set<String>>() {
            @Override
            public Set<String> get() {
                return Collections.<String>emptySet();
            }
        });
    }

    public static ModuleDescriptor read(ByteBuffer buffer, Supplier<Set<String>> packagesSupplier) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        InputStream in = new ByteBufferInputStream(buffer);
        try {
            return read(in, packagesSupplier);
        } catch (IOException e) {
            return unnamed(packagesSupplier != null ? packagesSupplier.get() : Collections.<String>emptySet());
        }
    }

    public static ModuleDescriptor read(ByteBuffer buffer) {
        return read(buffer, new Supplier<Set<String>>() {
            @Override
            public Set<String> get() {
                return Collections.<String>emptySet();
            }
        });
    }

    static ModuleDescriptor unnamed() {
        return unnamed(Collections.<String>emptySet());
    }

    static ModuleDescriptor unnamed(Set<String> packages) {
        return new ModuleDescriptor(null, null, EnumSet.noneOf(Modifier.class),
                Collections.<Requires>emptySet(),
                Collections.<Exports>emptySet(),
                Collections.<Opens>emptySet(),
                Collections.<String>emptySet(),
                Collections.<Provides>emptySet(),
                packages == null ? Collections.<String>emptySet() : packages,
                null, false, false);
    }

    private static <T> Set<T> unmodifiable(Set<T> set) {
        if (set == null || set.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<T>(set));
    }

    private static boolean safeEquals(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }

    public enum Modifier {
        OPEN,
        AUTOMATIC,
        SYNTHETIC,
        MANDATED
    }

    public static final class Version implements Comparable<Version> {
        private final String value;

        private Version(String value) {
            this.value = value;
        }

        public static Version parse(String value) {
            if (value == null) {
                throw new NullPointerException("value");
            }
            return new Version(value);
        }

        @Override
        public int compareTo(Version other) {
            if (other == null) {
                return 1;
            }
            return value.compareTo(other.value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Version)) return false;
            Version other = (Version) obj;
            return value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static final class Requires implements Comparable<Requires> {
        private final Set<Requires.Modifier> modifiers;
        private final String name;
        private final Version version;

        Requires(Set<Requires.Modifier> modifiers, String name, Version version) {
            this.modifiers = modifiers == null ? EnumSet.noneOf(Requires.Modifier.class)
                    : EnumSet.copyOf(modifiers);
            this.name = name;
            this.version = version;
        }

        public Set<Requires.Modifier> modifiers() {
            return Collections.unmodifiableSet(modifiers);
        }

        public String name() {
            return name;
        }

        public Optional<Version> compiledVersion() {
            return Optional.ofNullable(version);
        }

        public Optional<String> rawCompiledVersion() {
            return version == null ? Optional.<String>empty() : Optional.of(version.toString());
        }

        @Override
        public int compareTo(Requires other) {
            if (other == null) {
                return 1;
            }
            if (name == null) {
                return other.name == null ? 0 : -1;
            }
            if (other.name == null) {
                return 1;
            }
            return name.compareTo(other.name);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Requires)) return false;
            Requires other = (Requires) obj;
            return safeEquals(name, other.name)
                    && modifiers.equals(other.modifiers)
                    && safeEquals(version, other.version);
        }

        @Override
        public int hashCode() {
            int result = name == null ? 0 : name.hashCode();
            result = 31 * result + modifiers.hashCode();
            result = 31 * result + (version == null ? 0 : version.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "requires " + name;
        }

        public enum Modifier {
            TRANSITIVE,
            STATIC,
            SYNTHETIC,
            MANDATED
        }
    }

    public static final class Exports implements Comparable<Exports> {
        private final Set<Exports.Modifier> modifiers;
        private final String source;
        private final Set<String> targets;

        Exports(Set<Exports.Modifier> modifiers, String source, Set<String> targets) {
            this.modifiers = modifiers == null ? EnumSet.noneOf(Exports.Modifier.class)
                    : EnumSet.copyOf(modifiers);
            this.source = source;
            this.targets = targets == null ? Collections.<String>emptySet()
                    : Collections.unmodifiableSet(new HashSet<String>(targets));
        }

        public Set<Exports.Modifier> modifiers() {
            return Collections.unmodifiableSet(modifiers);
        }

        public boolean isQualified() {
            return !targets.isEmpty();
        }

        public String source() {
            return source;
        }

        public Set<String> targets() {
            return targets;
        }

        @Override
        public int compareTo(Exports other) {
            if (other == null) {
                return 1;
            }
            if (source == null) {
                return other.source == null ? 0 : -1;
            }
            if (other.source == null) {
                return 1;
            }
            return source.compareTo(other.source);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Exports)) return false;
            Exports other = (Exports) obj;
            return safeEquals(source, other.source)
                    && modifiers.equals(other.modifiers)
                    && targets.equals(other.targets);
        }

        @Override
        public int hashCode() {
            int result = source == null ? 0 : source.hashCode();
            result = 31 * result + modifiers.hashCode();
            result = 31 * result + targets.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "exports " + source;
        }

        public enum Modifier {
            SYNTHETIC,
            MANDATED
        }
    }

    public static final class Opens implements Comparable<Opens> {
        private final Set<Opens.Modifier> modifiers;
        private final String source;
        private final Set<String> targets;

        Opens(Set<Opens.Modifier> modifiers, String source, Set<String> targets) {
            this.modifiers = modifiers == null ? EnumSet.noneOf(Opens.Modifier.class)
                    : EnumSet.copyOf(modifiers);
            this.source = source;
            this.targets = targets == null ? Collections.<String>emptySet()
                    : Collections.unmodifiableSet(new HashSet<String>(targets));
        }

        public Set<Opens.Modifier> modifiers() {
            return Collections.unmodifiableSet(modifiers);
        }

        public boolean isQualified() {
            return !targets.isEmpty();
        }

        public String source() {
            return source;
        }

        public Set<String> targets() {
            return targets;
        }

        @Override
        public int compareTo(Opens other) {
            if (other == null) {
                return 1;
            }
            if (source == null) {
                return other.source == null ? 0 : -1;
            }
            if (other.source == null) {
                return 1;
            }
            return source.compareTo(other.source);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Opens)) return false;
            Opens other = (Opens) obj;
            return safeEquals(source, other.source)
                    && modifiers.equals(other.modifiers)
                    && targets.equals(other.targets);
        }

        @Override
        public int hashCode() {
            int result = source == null ? 0 : source.hashCode();
            result = 31 * result + modifiers.hashCode();
            result = 31 * result + targets.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "opens " + source;
        }

        public enum Modifier {
            SYNTHETIC,
            MANDATED
        }
    }

    public static final class Provides implements Comparable<Provides> {
        private final String service;
        private final List<String> providers;

        Provides(String service, List<String> providers) {
            this.service = service;
            this.providers = providers == null ? Collections.<String>emptyList()
                    : Collections.unmodifiableList(new ArrayList<String>(providers));
        }

        public String service() {
            return service;
        }

        public List<String> providers() {
            return providers;
        }

        @Override
        public int compareTo(Provides other) {
            if (other == null) {
                return 1;
            }
            if (service == null) {
                return other.service == null ? 0 : -1;
            }
            if (other.service == null) {
                return 1;
            }
            return service.compareTo(other.service);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Provides)) return false;
            Provides other = (Provides) obj;
            return safeEquals(service, other.service)
                    && providers.equals(other.providers);
        }

        @Override
        public int hashCode() {
            int result = service == null ? 0 : service.hashCode();
            result = 31 * result + providers.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "provides " + service;
        }
    }

    public static final class Builder {
        private final String name;
        private final Set<Modifier> modifiers;
        private final Set<Requires> requires = new HashSet<Requires>();
        private final Set<Exports> exports = new HashSet<Exports>();
        private final Set<Opens> opens = new HashSet<Opens>();
        private final Set<String> uses = new HashSet<String>();
        private final Set<Provides> provides = new HashSet<Provides>();
        private Set<String> packages = new HashSet<String>();
        private Version version;
        private String mainClass;

        Builder(String name, Set<Modifier> modifiers) {
            this.name = name;
            this.modifiers = modifiers == null ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(modifiers);
        }

        public Builder requires(Requires requires) {
            if (requires != null) {
                this.requires.add(requires);
            }
            return this;
        }

        public Builder requires(Set<Requires.Modifier> mods, String name, Version version) {
            return requires(new Requires(mods, name, version));
        }

        public Builder requires(Set<Requires.Modifier> mods, String name) {
            return requires(new Requires(mods, name, null));
        }

        public Builder requires(String name) {
            return requires(new Requires(EnumSet.noneOf(Requires.Modifier.class), name, null));
        }

        public Builder exports(Exports exports) {
            if (exports != null) {
                this.exports.add(exports);
            }
            return this;
        }

        public Builder exports(Set<Exports.Modifier> mods, String source, Set<String> targets) {
            return exports(new Exports(mods, source, targets));
        }

        public Builder exports(Set<Exports.Modifier> mods, String source) {
            return exports(new Exports(mods, source, Collections.<String>emptySet()));
        }

        public Builder exports(String source, Set<String> targets) {
            return exports(new Exports(EnumSet.noneOf(Exports.Modifier.class), source, targets));
        }

        public Builder exports(String source) {
            return exports(new Exports(EnumSet.noneOf(Exports.Modifier.class), source,
                    Collections.<String>emptySet()));
        }

        public Builder opens(Opens opens) {
            if (opens != null) {
                this.opens.add(opens);
            }
            return this;
        }

        public Builder opens(Set<Opens.Modifier> mods, String source, Set<String> targets) {
            return opens(new Opens(mods, source, targets));
        }

        public Builder opens(Set<Opens.Modifier> mods, String source) {
            return opens(new Opens(mods, source, Collections.<String>emptySet()));
        }

        public Builder opens(String source, Set<String> targets) {
            return opens(new Opens(EnumSet.noneOf(Opens.Modifier.class), source, targets));
        }

        public Builder opens(String source) {
            return opens(new Opens(EnumSet.noneOf(Opens.Modifier.class), source,
                    Collections.<String>emptySet()));
        }

        public Builder uses(String service) {
            if (service != null) {
                uses.add(service);
            }
            return this;
        }

        public Builder provides(Provides provides) {
            if (provides != null) {
                this.provides.add(provides);
            }
            return this;
        }

        public Builder provides(String service, List<String> providers) {
            return provides(new Provides(service, providers));
        }

        public Builder packages(Set<String> packages) {
            if (packages != null) {
                this.packages = new HashSet<String>(packages);
            }
            return this;
        }

        public Builder version(Version version) {
            this.version = version;
            return this;
        }

        public Builder version(String version) {
            this.version = version == null ? null : Version.parse(version);
            return this;
        }

        public Builder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        public ModuleDescriptor build() {
            boolean open = modifiers.contains(Modifier.OPEN);
            boolean automatic = modifiers.contains(Modifier.AUTOMATIC);
            return new ModuleDescriptor(name, version, modifiers, requires, exports, opens,
                    uses, provides, packages, mainClass, open, automatic);
        }
    }

    private static final class ModuleInfoParser {
        private static final int MAGIC = 0xCAFEBABE;

        static ModuleInfo parse(InputStream in) throws IOException {
            DataInputStream data = new DataInputStream(in);
            if (data.readInt() != MAGIC) {
                return null;
            }
            data.readUnsignedShort();
            data.readUnsignedShort();
            ConstantPool pool = ConstantPool.read(data);
            data.readUnsignedShort();
            data.readUnsignedShort();
            data.readUnsignedShort();
            int interfaces = data.readUnsignedShort();
            for (int i = 0; i < interfaces; i++) {
                data.readUnsignedShort();
            }
            int fields = data.readUnsignedShort();
            for (int i = 0; i < fields; i++) {
                skipMember(data);
            }
            int methods = data.readUnsignedShort();
            for (int i = 0; i < methods; i++) {
                skipMember(data);
            }
            int attrs = data.readUnsignedShort();
            for (int i = 0; i < attrs; i++) {
                String attrName = pool.utf8(data.readUnsignedShort());
                int len = data.readInt();
                if ("Module".equals(attrName)) {
                    return ModuleInfo.read(data, pool);
                }
                skipFully(data, len);
            }
            return null;
        }

        private static void skipMember(DataInputStream data) throws IOException {
            data.readUnsignedShort();
            data.readUnsignedShort();
            data.readUnsignedShort();
            int attrs = data.readUnsignedShort();
            for (int i = 0; i < attrs; i++) {
                data.readUnsignedShort();
                int len = data.readInt();
                skipFully(data, len);
            }
        }

        private static void skipFully(DataInputStream data, int len) throws IOException {
            int remaining = len;
            while (remaining > 0) {
                int skipped = (int) data.skip(remaining);
                if (skipped <= 0) {
                    if (data.read() == -1) {
                        break;
                    }
                    skipped = 1;
                }
                remaining -= skipped;
            }
        }
    }

    private static final class ModuleInfo {
        final String name;

        ModuleInfo(String name) {
            this.name = name;
        }

        static ModuleInfo read(DataInputStream data, ConstantPool pool) throws IOException {
            int moduleNameIndex = data.readUnsignedShort();
            String name = pool.moduleName(moduleNameIndex);
            int requiresCount = data.readUnsignedShort();
            for (int i = 0; i < requiresCount; i++) {
                data.readUnsignedShort();
                data.readUnsignedShort();
                data.readUnsignedShort();
            }
            int exportsCount = data.readUnsignedShort();
            for (int i = 0; i < exportsCount; i++) {
                data.readUnsignedShort();
                data.readUnsignedShort();
                int targetCount = data.readUnsignedShort();
                for (int t = 0; t < targetCount; t++) {
                    data.readUnsignedShort();
                }
            }
            int opensCount = data.readUnsignedShort();
            for (int i = 0; i < opensCount; i++) {
                data.readUnsignedShort();
                data.readUnsignedShort();
                int targetCount = data.readUnsignedShort();
                for (int t = 0; t < targetCount; t++) {
                    data.readUnsignedShort();
                }
            }
            int usesCount = data.readUnsignedShort();
            for (int i = 0; i < usesCount; i++) {
                data.readUnsignedShort();
            }
            int providesCount = data.readUnsignedShort();
            for (int i = 0; i < providesCount; i++) {
                data.readUnsignedShort();
                int implCount = data.readUnsignedShort();
                for (int t = 0; t < implCount; t++) {
                    data.readUnsignedShort();
                }
            }
            return new ModuleInfo(name);
        }
    }

    private static final class ConstantPool {
        private final String[] utf8;
        private final int[] moduleNames;

        private ConstantPool(String[] utf8, int[] moduleNames) {
            this.utf8 = utf8;
            this.moduleNames = moduleNames;
        }

        String utf8(int index) {
            if (index <= 0 || index >= utf8.length) {
                return null;
            }
            return utf8[index];
        }

        String moduleName(int index) {
            if (index <= 0 || index >= moduleNames.length) {
                return null;
            }
            int utfIndex = moduleNames[index];
            return utf8(utfIndex);
        }

        static ConstantPool read(DataInputStream data) throws IOException {
            int count = data.readUnsignedShort();
            String[] utf8 = new String[count];
            int[] moduleNames = new int[count];
            for (int i = 1; i < count; i++) {
                int tag = data.readUnsignedByte();
                switch (tag) {
                    case 1:
                        utf8[i] = data.readUTF();
                        break;
                    case 3:
                    case 4:
                        data.readInt();
                        break;
                    case 5:
                    case 6:
                        data.readLong();
                        i++;
                        break;
                    case 7:
                    case 8:
                    case 16:
                        data.readUnsignedShort();
                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 18:
                        data.readUnsignedShort();
                        data.readUnsignedShort();
                        break;
                    case 15:
                        data.readUnsignedByte();
                        data.readUnsignedShort();
                        break;
                    case 19:
                        moduleNames[i] = data.readUnsignedShort();
                        break;
                    case 20:
                        data.readUnsignedShort();
                        break;
                    default:
                        throw new IOException("Unknown constant pool tag " + tag);
                }
            }
            return new ConstantPool(utf8, moduleNames);
        }
    }

    private static final class ByteBufferInputStream extends InputStream {
        private final ByteBuffer buffer;

        ByteBufferInputStream(ByteBuffer buffer) {
            this.buffer = buffer.slice();
        }

        @Override
        public int read() throws IOException {
            if (!buffer.hasRemaining()) {
                return -1;
            }
            return buffer.get() & 0xFF;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (!buffer.hasRemaining()) {
                return -1;
            }
            int toRead = Math.min(len, buffer.remaining());
            buffer.get(b, off, toRead);
            return toRead;
        }
    }
}
