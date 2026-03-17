package j9compat;

/**
 * Guard for desugared private interface methods.
 */
public final class PrivateInterfaceAccess {

    private PrivateInterfaceAccess() {}

    public static void checkCaller(String ownerInternalName) {
        if (ownerInternalName == null) {
            return;
        }
        String owner = ownerInternalName.replace('/', '.');
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        boolean sawOwner = false;
        for (StackTraceElement element : trace) {
            String className = element.getClassName();
            if (!sawOwner) {
                if (className.equals(owner)) {
                    sawOwner = true;
                }
                continue;
            }
            if (className.equals(owner)) {
                return;
            }
            if (className.startsWith("java.lang.invoke.")
                    || className.startsWith("java.lang.reflect.")
                    || className.startsWith("sun.reflect.")) {
                return;
            }
            throw new IllegalAccessError("Illegal access to private interface method in " + owner);
        }
    }
}
