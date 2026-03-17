package j9compat;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Reflection helpers that redirect Java 9 API lookups to backport methods.
 */
public final class ReflectionBackport {

    private static final Map<Method, Boolean> RECEIVER_AWARE =
            Collections.synchronizedMap(new WeakHashMap<Method, Boolean>());

    private ReflectionBackport() {}

    public static Method getMethod(Class<?> type, String name, Class<?>... params)
            throws NoSuchMethodException {
        try {
            return type.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            BackportMappings.BackportMethod backport = BackportMappings.find(type, name, params);
            if (backport != null) {
                track(backport);
                return backport.method;
            }
            throw e;
        }
    }

    public static Method getDeclaredMethod(Class<?> type, String name, Class<?>... params)
            throws NoSuchMethodException {
        try {
            return type.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            BackportMappings.BackportMethod backport = BackportMappings.find(type, name, params);
            if (backport != null) {
                track(backport);
                return backport.method;
            }
            throw e;
        }
    }

    public static Method[] getMethods(Class<?> type) {
        return type.getMethods();
    }

    public static Method[] getDeclaredMethods(Class<?> type) {
        return type.getDeclaredMethods();
    }

    public static Object invoke(Method method, Object obj, Object... args) throws Exception {
        if (method == null) {
            throw new NullPointerException("method");
        }
        Boolean needsReceiver = RECEIVER_AWARE.get(method);
        if (needsReceiver != null && needsReceiver.booleanValue()) {
            if (obj == null) {
                throw new NullPointerException("receiver");
            }
            Object[] input = args == null ? new Object[0] : args;
            Object[] merged = new Object[input.length + 1];
            merged[0] = obj;
            System.arraycopy(input, 0, merged, 1, input.length);
            return method.invoke(null, merged);
        }
        return method.invoke(obj, args);
    }

    private static void track(BackportMappings.BackportMethod backport) {
        RECEIVER_AWARE.put(backport.method, Boolean.valueOf(backport.needsReceiver));
    }
}
