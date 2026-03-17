package j9compat;

/**
 * Java 8-compatible helper for {@link java.lang.Class#getModule()}.
 */
public final class ModuleBackport {

    private ModuleBackport() {}

    public static Module getModule(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        return ModuleLayer.boot().unnamedModule();
    }
}
