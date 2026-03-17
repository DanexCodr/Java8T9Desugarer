package j9compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

/**
 * MethodHandles helpers for remapping Java 9 lookups to backport methods.
 */
public final class MethodHandlesBackport {

    private MethodHandlesBackport() {}

    public static MethodHandle findVirtual(MethodHandles.Lookup lookup,
                                           Class<?> owner,
                                           String name,
                                           MethodType type)
            throws NoSuchMethodException, IllegalAccessException {
        try {
            return lookup.findVirtual(owner, name, type);
        } catch (NoSuchMethodException e) {
            MethodHandle fallback = findBackportVirtual(lookup, owner, name, type);
            if (fallback != null) {
                return fallback;
            }
            throw e;
        }
    }

    public static MethodHandle findStatic(MethodHandles.Lookup lookup,
                                          Class<?> owner,
                                          String name,
                                          MethodType type)
            throws NoSuchMethodException, IllegalAccessException {
        try {
            return lookup.findStatic(owner, name, type);
        } catch (NoSuchMethodException e) {
            MethodHandle fallback = findBackportStatic(lookup, owner, name, type);
            if (fallback != null) {
                return fallback;
            }
            throw e;
        }
    }

    public static MethodHandle findSpecial(MethodHandles.Lookup lookup,
                                           Class<?> owner,
                                           String name,
                                           MethodType type,
                                           Class<?> specialCaller)
            throws NoSuchMethodException, IllegalAccessException {
        try {
            return lookup.findSpecial(owner, name, type, specialCaller);
        } catch (NoSuchMethodException e) {
            MethodHandle fallback = findBackportVirtual(lookup, owner, name, type);
            if (fallback != null) {
                return fallback;
            }
            throw e;
        }
    }

    public static MethodHandle findConstructor(MethodHandles.Lookup lookup,
                                               Class<?> owner,
                                               MethodType type)
            throws NoSuchMethodException, IllegalAccessException {
        return lookup.findConstructor(owner, type);
    }

    public static VarHandle findVarHandle(MethodHandles.Lookup lookup,
                                          Class<?> owner,
                                          String name,
                                          Class<?> type)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = owner.getDeclaredField(name);
        if (!field.getType().equals(type)) {
            throw new NoSuchFieldException(name);
        }
        return VarHandle.forField(field);
    }

    public static VarHandle findStaticVarHandle(MethodHandles.Lookup lookup,
                                                Class<?> owner,
                                                String name,
                                                Class<?> type)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = owner.getDeclaredField(name);
        if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
            throw new NoSuchFieldException(name);
        }
        if (!field.getType().equals(type)) {
            throw new NoSuchFieldException(name);
        }
        return VarHandle.forField(field);
    }

    public static VarHandle arrayElementVarHandle(Class<?> arrayType) {
        return VarHandle.forArray(arrayType);
    }

    private static MethodHandle findBackportVirtual(MethodHandles.Lookup lookup,
                                                    Class<?> owner,
                                                    String name,
                                                    MethodType type)
            throws NoSuchMethodException, IllegalAccessException {
        BackportMappings.BackportMethod mapping = BackportMappings.find(owner, name,
                type.parameterArray());
        if (mapping == null || !mapping.needsReceiver) {
            return null;
        }
        MethodType remapped = type.insertParameterTypes(0, owner);
        return lookup.findStatic(mapping.method.getDeclaringClass(),
                mapping.method.getName(), remapped);
    }

    private static MethodHandle findBackportStatic(MethodHandles.Lookup lookup,
                                                   Class<?> owner,
                                                   String name,
                                                   MethodType type)
            throws NoSuchMethodException, IllegalAccessException {
        BackportMappings.BackportMethod mapping = BackportMappings.find(owner, name,
                type.parameterArray());
        if (mapping == null || mapping.needsReceiver) {
            return null;
        }
        return lookup.findStatic(mapping.method.getDeclaringClass(),
                mapping.method.getName(), type);
    }
}
