package j9compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java 8-compatible backport of {@link java.lang.invoke.VarHandle}.
 */
public final class VarHandle {

    public enum AccessMode {
        GET,
        SET,
        GET_VOLATILE,
        SET_VOLATILE,
        GET_OPAQUE,
        SET_OPAQUE,
        GET_ACQUIRE,
        SET_RELEASE,
        COMPARE_AND_SET,
        COMPARE_AND_EXCHANGE,
        COMPARE_AND_EXCHANGE_ACQUIRE,
        COMPARE_AND_EXCHANGE_RELEASE,
        WEAK_COMPARE_AND_SET_PLAIN,
        WEAK_COMPARE_AND_SET,
        WEAK_COMPARE_AND_SET_ACQUIRE,
        WEAK_COMPARE_AND_SET_RELEASE,
        GET_AND_SET,
        GET_AND_SET_ACQUIRE,
        GET_AND_SET_RELEASE,
        GET_AND_ADD,
        GET_AND_ADD_ACQUIRE,
        GET_AND_ADD_RELEASE,
        GET_AND_BITWISE_OR,
        GET_AND_BITWISE_OR_ACQUIRE,
        GET_AND_BITWISE_OR_RELEASE,
        GET_AND_BITWISE_AND,
        GET_AND_BITWISE_AND_ACQUIRE,
        GET_AND_BITWISE_AND_RELEASE,
        GET_AND_BITWISE_XOR,
        GET_AND_BITWISE_XOR_ACQUIRE,
        GET_AND_BITWISE_XOR_RELEASE
    }

    private final Class<?> varType;
    private final List<Class<?>> coordinateTypes;
    private final Access access;
    private final boolean exact;

    private VarHandle(Class<?> varType, List<Class<?>> coordinateTypes, Access access, boolean exact) {
        this.varType = varType;
        this.coordinateTypes = Collections.unmodifiableList(new ArrayList<Class<?>>(coordinateTypes));
        this.access = access;
        this.exact = exact;
    }

    static VarHandle forField(Field field) {
        if (field == null) {
            throw new NullPointerException("field");
        }
        boolean isStatic = java.lang.reflect.Modifier.isStatic(field.getModifiers());
        field.setAccessible(true);
        if (isStatic) {
            return new VarHandle(field.getType(), Collections.<Class<?>>emptyList(),
                    new FieldAccess(field, null, true), false);
        }
        List<Class<?>> coords = new ArrayList<Class<?>>();
        coords.add(field.getDeclaringClass());
        return new VarHandle(field.getType(), coords, new FieldAccess(field, null, false), false);
    }

    static VarHandle forArray(Class<?> arrayType) {
        if (arrayType == null || !arrayType.isArray()) {
            throw new IllegalArgumentException("arrayType");
        }
        List<Class<?>> coords = new ArrayList<Class<?>>();
        coords.add(arrayType);
        coords.add(int.class);
        return new VarHandle(arrayType.getComponentType(), coords, new ArrayAccess(arrayType), false);
    }

    public Class<?> varType() {
        return varType;
    }

    public List<Class<?>> coordinateTypes() {
        return coordinateTypes;
    }

    public boolean hasInvokeExactBehavior() {
        return exact;
    }

    public VarHandle withInvokeExactBehavior() {
        return new VarHandle(varType, coordinateTypes, access, true);
    }

    public VarHandle withInvokeBehavior() {
        return new VarHandle(varType, coordinateTypes, access, false);
    }

    public Object get(Object... args) {
        return access.get(args);
    }

    public void set(Object... args) {
        access.set(args);
    }

    public Object getVolatile(Object... args) {
        return access.get(args);
    }

    public void setVolatile(Object... args) {
        access.set(args);
    }

    public Object getOpaque(Object... args) {
        return access.get(args);
    }

    public void setOpaque(Object... args) {
        access.set(args);
    }

    public Object getAcquire(Object... args) {
        return access.get(args);
    }

    public void setRelease(Object... args) {
        access.set(args);
    }

    public boolean compareAndSet(Object... args) {
        return access.compareAndSet(args);
    }

    public Object compareAndExchange(Object... args) {
        return access.compareAndExchange(args);
    }

    public Object compareAndExchangeAcquire(Object... args) {
        return access.compareAndExchange(args);
    }

    public Object compareAndExchangeRelease(Object... args) {
        return access.compareAndExchange(args);
    }

    public boolean weakCompareAndSetPlain(Object... args) {
        return access.compareAndSet(args);
    }

    public boolean weakCompareAndSet(Object... args) {
        return access.compareAndSet(args);
    }

    public boolean weakCompareAndSetAcquire(Object... args) {
        return access.compareAndSet(args);
    }

    public boolean weakCompareAndSetRelease(Object... args) {
        return access.compareAndSet(args);
    }

    public Object getAndSet(Object... args) {
        return access.getAndSet(args);
    }

    public Object getAndSetAcquire(Object... args) {
        return access.getAndSet(args);
    }

    public Object getAndSetRelease(Object... args) {
        return access.getAndSet(args);
    }

    public Object getAndAdd(Object... args) {
        return access.getAndAdd(args);
    }

    public Object getAndAddAcquire(Object... args) {
        return access.getAndAdd(args);
    }

    public Object getAndAddRelease(Object... args) {
        return access.getAndAdd(args);
    }

    public Object getAndBitwiseOr(Object... args) {
        return access.getAndBitwiseOr(args);
    }

    public Object getAndBitwiseOrAcquire(Object... args) {
        return access.getAndBitwiseOr(args);
    }

    public Object getAndBitwiseOrRelease(Object... args) {
        return access.getAndBitwiseOr(args);
    }

    public Object getAndBitwiseAnd(Object... args) {
        return access.getAndBitwiseAnd(args);
    }

    public Object getAndBitwiseAndAcquire(Object... args) {
        return access.getAndBitwiseAnd(args);
    }

    public Object getAndBitwiseAndRelease(Object... args) {
        return access.getAndBitwiseAnd(args);
    }

    public Object getAndBitwiseXor(Object... args) {
        return access.getAndBitwiseXor(args);
    }

    public Object getAndBitwiseXorAcquire(Object... args) {
        return access.getAndBitwiseXor(args);
    }

    public Object getAndBitwiseXorRelease(Object... args) {
        return access.getAndBitwiseXor(args);
    }

    public MethodType accessModeType(AccessMode mode) {
        return accessModeType(mode, varType, coordinateTypes);
    }

    public boolean isAccessModeSupported(AccessMode mode) {
        return mode != null;
    }

    public MethodHandle toMethodHandle(AccessMode mode) {
        MethodType type = accessModeType(mode);
        MethodHandle handle;
        switch (mode) {
            case GET:
            case GET_ACQUIRE:
            case GET_OPAQUE:
            case GET_VOLATILE:
                handle = lookupHandle("invokeGet", Object[].class); break;
            case SET:
            case SET_OPAQUE:
            case SET_RELEASE:
            case SET_VOLATILE:
                handle = lookupHandle("invokeSet", Object[].class); break;
            case COMPARE_AND_SET:
            case WEAK_COMPARE_AND_SET:
            case WEAK_COMPARE_AND_SET_ACQUIRE:
            case WEAK_COMPARE_AND_SET_PLAIN:
            case WEAK_COMPARE_AND_SET_RELEASE:
                handle = lookupHandle("invokeCompareAndSet", Object[].class); break;
            case COMPARE_AND_EXCHANGE:
            case COMPARE_AND_EXCHANGE_ACQUIRE:
            case COMPARE_AND_EXCHANGE_RELEASE:
                handle = lookupHandle("invokeCompareAndExchange", Object[].class); break;
            case GET_AND_SET:
            case GET_AND_SET_ACQUIRE:
            case GET_AND_SET_RELEASE:
                handle = lookupHandle("invokeGetAndSet", Object[].class); break;
            case GET_AND_ADD:
            case GET_AND_ADD_ACQUIRE:
            case GET_AND_ADD_RELEASE:
                handle = lookupHandle("invokeGetAndAdd", Object[].class); break;
            case GET_AND_BITWISE_OR:
            case GET_AND_BITWISE_OR_ACQUIRE:
            case GET_AND_BITWISE_OR_RELEASE:
                handle = lookupHandle("invokeGetAndBitwiseOr", Object[].class); break;
            case GET_AND_BITWISE_AND:
            case GET_AND_BITWISE_AND_ACQUIRE:
            case GET_AND_BITWISE_AND_RELEASE:
                handle = lookupHandle("invokeGetAndBitwiseAnd", Object[].class); break;
            case GET_AND_BITWISE_XOR:
            case GET_AND_BITWISE_XOR_ACQUIRE:
            case GET_AND_BITWISE_XOR_RELEASE:
                handle = lookupHandle("invokeGetAndBitwiseXor", Object[].class); break;
            default:
                throw new UnsupportedOperationException("Unsupported access mode: " + mode);
        }
        MethodHandle adapted = handle.asCollector(Object[].class, coordinateTypes.size()
                + extraArgs(mode));
        return adapted.asType(type);
    }

    public static void fullFence() {
        // no-op in Java 8
    }

    public static void acquireFence() {
        // no-op in Java 8
    }

    public static void releaseFence() {
        // no-op in Java 8
    }

    public static void loadLoadFence() {
        // no-op in Java 8
    }

    public static void storeStoreFence() {
        // no-op in Java 8
    }

    private MethodHandle lookupHandle(String name, Class<?> argType) {
        try {
            return MethodHandles.lookup().findVirtual(VarHandle.class, name,
                    MethodType.methodType(Object.class, argType)).bindTo(this);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private Object invokeGet(Object[] args) {
        return get(args);
    }

    private Object invokeSet(Object[] args) {
        set(args);
        return null;
    }

    private Object invokeCompareAndSet(Object[] args) {
        return Boolean.valueOf(compareAndSet(args));
    }

    private Object invokeCompareAndExchange(Object[] args) {
        return compareAndExchange(args);
    }

    private Object invokeGetAndSet(Object[] args) {
        return getAndSet(args);
    }

    private Object invokeGetAndAdd(Object[] args) {
        return getAndAdd(args);
    }

    private Object invokeGetAndBitwiseOr(Object[] args) {
        return getAndBitwiseOr(args);
    }

    private Object invokeGetAndBitwiseAnd(Object[] args) {
        return getAndBitwiseAnd(args);
    }

    private Object invokeGetAndBitwiseXor(Object[] args) {
        return getAndBitwiseXor(args);
    }

    private static MethodType accessModeType(AccessMode mode,
                                             Class<?> varType,
                                             List<Class<?>> coordinates) {
        List<Class<?>> params = new ArrayList<Class<?>>(coordinates);
        switch (mode) {
            case GET:
            case GET_VOLATILE:
            case GET_OPAQUE:
            case GET_ACQUIRE:
                return MethodType.methodType(varType, params.toArray(new Class<?>[0]));
            case SET:
            case SET_VOLATILE:
            case SET_OPAQUE:
            case SET_RELEASE:
                params.add(varType);
                return MethodType.methodType(void.class, params.toArray(new Class<?>[0]));
            case COMPARE_AND_SET:
            case WEAK_COMPARE_AND_SET:
            case WEAK_COMPARE_AND_SET_ACQUIRE:
            case WEAK_COMPARE_AND_SET_PLAIN:
            case WEAK_COMPARE_AND_SET_RELEASE:
                params.add(varType);
                params.add(varType);
                return MethodType.methodType(boolean.class, params.toArray(new Class<?>[0]));
            case COMPARE_AND_EXCHANGE:
            case COMPARE_AND_EXCHANGE_ACQUIRE:
            case COMPARE_AND_EXCHANGE_RELEASE:
                params.add(varType);
                params.add(varType);
                return MethodType.methodType(varType, params.toArray(new Class<?>[0]));
            case GET_AND_SET:
            case GET_AND_SET_ACQUIRE:
            case GET_AND_SET_RELEASE:
            case GET_AND_ADD:
            case GET_AND_ADD_ACQUIRE:
            case GET_AND_ADD_RELEASE:
            case GET_AND_BITWISE_OR:
            case GET_AND_BITWISE_OR_ACQUIRE:
            case GET_AND_BITWISE_OR_RELEASE:
            case GET_AND_BITWISE_AND:
            case GET_AND_BITWISE_AND_ACQUIRE:
            case GET_AND_BITWISE_AND_RELEASE:
            case GET_AND_BITWISE_XOR:
            case GET_AND_BITWISE_XOR_ACQUIRE:
            case GET_AND_BITWISE_XOR_RELEASE:
                params.add(varType);
                return MethodType.methodType(varType, params.toArray(new Class<?>[0]));
            default:
                throw new UnsupportedOperationException("Unsupported access mode: " + mode);
        }
    }

    private static int extraArgs(AccessMode mode) {
        switch (mode) {
            case SET:
            case SET_VOLATILE:
            case SET_OPAQUE:
            case SET_RELEASE:
                return 1;
            case COMPARE_AND_SET:
            case WEAK_COMPARE_AND_SET:
            case WEAK_COMPARE_AND_SET_ACQUIRE:
            case WEAK_COMPARE_AND_SET_PLAIN:
            case WEAK_COMPARE_AND_SET_RELEASE:
                return 2;
            case COMPARE_AND_EXCHANGE:
            case COMPARE_AND_EXCHANGE_ACQUIRE:
            case COMPARE_AND_EXCHANGE_RELEASE:
                return 2;
            case GET_AND_SET:
            case GET_AND_SET_ACQUIRE:
            case GET_AND_SET_RELEASE:
            case GET_AND_ADD:
            case GET_AND_ADD_ACQUIRE:
            case GET_AND_ADD_RELEASE:
            case GET_AND_BITWISE_OR:
            case GET_AND_BITWISE_OR_ACQUIRE:
            case GET_AND_BITWISE_OR_RELEASE:
            case GET_AND_BITWISE_AND:
            case GET_AND_BITWISE_AND_ACQUIRE:
            case GET_AND_BITWISE_AND_RELEASE:
            case GET_AND_BITWISE_XOR:
            case GET_AND_BITWISE_XOR_ACQUIRE:
            case GET_AND_BITWISE_XOR_RELEASE:
                return 1;
            default:
                return 0;
        }
    }

    private interface Access {
        Object get(Object[] args);
        void set(Object[] args);
        boolean compareAndSet(Object[] args);
        Object compareAndExchange(Object[] args);
        Object getAndSet(Object[] args);
        Object getAndAdd(Object[] args);
        Object getAndBitwiseOr(Object[] args);
        Object getAndBitwiseAnd(Object[] args);
        Object getAndBitwiseXor(Object[] args);
    }

    private static final class FieldAccess implements Access {
        private final Field field;
        private final Object staticBase;
        private final boolean isStatic;
        private final Object lock = new Object();

        FieldAccess(Field field, Object staticBase, boolean isStatic) {
            this.field = field;
            this.staticBase = staticBase;
            this.isStatic = isStatic;
        }

        private Object receiver(Object[] args) {
            if (isStatic) {
                return staticBase;
            }
            if (args.length == 0 || args[0] == null) {
                throw new NullPointerException("receiver");
            }
            return args[0];
        }

        private int valueIndex() {
            return isStatic ? 0 : 1;
        }

        private Object[] setArgs(Object receiver, Object value) {
            if (isStatic) {
                return new Object[]{value};
            }
            return new Object[]{receiver, value};
        }

        @Override
        public Object get(Object[] args) {
            synchronized (lock) {
                try {
                    return field.get(receiver(args));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        @Override
        public void set(Object[] args) {
            int index = valueIndex();
            if (args.length <= index) {
                throw new IllegalArgumentException("missing value");
            }
            synchronized (lock) {
                try {
                    field.set(receiver(args), args[index]);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        @Override
        public boolean compareAndSet(Object[] args) {
            int index = valueIndex();
            if (args.length <= index + 1) {
                throw new IllegalArgumentException("missing compare arguments");
            }
            synchronized (lock) {
                Object current = get(args);
                if (equalsValue(current, args[index])) {
                    set(setArgs(receiver(args), args[index + 1]));
                    return true;
                }
                return false;
            }
        }

        @Override
        public Object compareAndExchange(Object[] args) {
            int index = valueIndex();
            if (args.length <= index + 1) {
                throw new IllegalArgumentException("missing compare arguments");
            }
            synchronized (lock) {
                Object current = get(args);
                if (equalsValue(current, args[index])) {
                    set(setArgs(receiver(args), args[index + 1]));
                }
                return current;
            }
        }

        @Override
        public Object getAndSet(Object[] args) {
            int index = valueIndex();
            if (args.length <= index) {
                throw new IllegalArgumentException("missing value");
            }
            synchronized (lock) {
                Object current = get(args);
                set(setArgs(receiver(args), args[index]));
                return current;
            }
        }

        @Override
        public Object getAndAdd(Object[] args) {
            int index = valueIndex();
            if (args.length <= index) {
                throw new IllegalArgumentException("missing value");
            }
            synchronized (lock) {
                Number delta = toNumber(args[index]);
                Object current = get(args);
                Object next = addNumber(current, delta);
                set(setArgs(receiver(args), next));
                return current;
            }
        }

        @Override
        public Object getAndBitwiseOr(Object[] args) {
            return updateBitwise(args, BitwiseOp.OR);
        }

        @Override
        public Object getAndBitwiseAnd(Object[] args) {
            return updateBitwise(args, BitwiseOp.AND);
        }

        @Override
        public Object getAndBitwiseXor(Object[] args) {
            return updateBitwise(args, BitwiseOp.XOR);
        }

        private Object updateBitwise(Object[] args, BitwiseOp op) {
            int index = valueIndex();
            if (args.length <= index) {
                throw new IllegalArgumentException("missing value");
            }
            synchronized (lock) {
                Number delta = toNumber(args[index]);
                Object current = get(args);
                Object next = applyBitwise(current, delta, op);
                set(setArgs(receiver(args), next));
                return current;
            }
        }
    }

    private static final class ArrayAccess implements Access {
        private final Class<?> arrayType;
        private final Object lock = new Object();

        ArrayAccess(Class<?> arrayType) {
            this.arrayType = arrayType;
        }

        private Object array(Object[] args) {
            if (args.length == 0 || args[0] == null) {
                throw new NullPointerException("array");
            }
            return args[0];
        }

        private int index(Object[] args) {
            if (args.length < 2) {
                throw new IllegalArgumentException("missing index");
            }
            return ((Number) args[1]).intValue();
        }

        private int valueIndex() {
            return 2;
        }

        @Override
        public Object get(Object[] args) {
            synchronized (lock) {
                return Array.get(array(args), index(args));
            }
        }

        @Override
        public void set(Object[] args) {
            int idx = valueIndex();
            if (args.length <= idx) {
                throw new IllegalArgumentException("missing value");
            }
            synchronized (lock) {
                Array.set(array(args), index(args), args[idx]);
            }
        }

        @Override
        public boolean compareAndSet(Object[] args) {
            int idx = valueIndex();
            if (args.length <= idx + 1) {
                throw new IllegalArgumentException("missing compare args");
            }
            synchronized (lock) {
                Object current = get(args);
                if (equalsValue(current, args[idx])) {
                    set(new Object[]{array(args), index(args), args[idx + 1]});
                    return true;
                }
                return false;
            }
        }

        @Override
        public Object compareAndExchange(Object[] args) {
            int idx = valueIndex();
            if (args.length <= idx + 1) {
                throw new IllegalArgumentException("missing compare args");
            }
            synchronized (lock) {
                Object current = get(args);
                if (equalsValue(current, args[idx])) {
                    set(new Object[]{array(args), index(args), args[idx + 1]});
                }
                return current;
            }
        }

        @Override
        public Object getAndSet(Object[] args) {
            int idx = valueIndex();
            if (args.length <= idx) {
                throw new IllegalArgumentException("missing value");
            }
            synchronized (lock) {
                Object current = get(args);
                set(new Object[]{array(args), index(args), args[idx]});
                return current;
            }
        }

        @Override
        public Object getAndAdd(Object[] args) {
            int idx = valueIndex();
            if (args.length <= idx) {
                throw new IllegalArgumentException("missing value");
            }
            synchronized (lock) {
                Number delta = toNumber(args[idx]);
                Object current = get(args);
                Object next = addNumber(current, delta);
                set(new Object[]{array(args), index(args), next});
                return current;
            }
        }

        @Override
        public Object getAndBitwiseOr(Object[] args) {
            return updateBitwise(args, BitwiseOp.OR);
        }

        @Override
        public Object getAndBitwiseAnd(Object[] args) {
            return updateBitwise(args, BitwiseOp.AND);
        }

        @Override
        public Object getAndBitwiseXor(Object[] args) {
            return updateBitwise(args, BitwiseOp.XOR);
        }

        private Object updateBitwise(Object[] args, BitwiseOp op) {
            int idx = valueIndex();
            if (args.length <= idx) {
                throw new IllegalArgumentException("missing value");
            }
            synchronized (lock) {
                Number delta = toNumber(args[idx]);
                Object current = get(args);
                Object next = applyBitwise(current, delta, op);
                set(new Object[]{array(args), index(args), next});
                return current;
            }
        }
    }

    private enum BitwiseOp { OR, AND, XOR }

    private static boolean equalsValue(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }

    private static Number toNumber(Object value) {
        if (!(value instanceof Number)) {
            throw new UnsupportedOperationException("Numeric operation on non-numeric type");
        }
        return (Number) value;
    }

    private static Object addNumber(Object current, Number delta) {
        if (current instanceof Integer) {
            return ((Integer) current) + delta.intValue();
        }
        if (current instanceof Long) {
            return ((Long) current) + delta.longValue();
        }
        if (current instanceof Short) {
            return (short) (((Short) current) + delta.intValue());
        }
        if (current instanceof Byte) {
            return (byte) (((Byte) current) + delta.intValue());
        }
        if (current instanceof Float) {
            return ((Float) current) + delta.floatValue();
        }
        if (current instanceof Double) {
            return ((Double) current) + delta.doubleValue();
        }
        throw new UnsupportedOperationException("Numeric operation on non-numeric type");
    }

    private static Object applyBitwise(Object current, Number delta, BitwiseOp op) {
        if (current instanceof Integer) {
            int a = (Integer) current;
            int b = delta.intValue();
            return op == BitwiseOp.OR ? (a | b) : op == BitwiseOp.AND ? (a & b) : (a ^ b);
        }
        if (current instanceof Long) {
            long a = (Long) current;
            long b = delta.longValue();
            return op == BitwiseOp.OR ? (a | b) : op == BitwiseOp.AND ? (a & b) : (a ^ b);
        }
        if (current instanceof Short) {
            short a = (Short) current;
            short b = delta.shortValue();
            return op == BitwiseOp.OR ? (short) (a | b)
                    : op == BitwiseOp.AND ? (short) (a & b)
                    : (short) (a ^ b);
        }
        if (current instanceof Byte) {
            byte a = (Byte) current;
            byte b = delta.byteValue();
            return op == BitwiseOp.OR ? (byte) (a | b)
                    : op == BitwiseOp.AND ? (byte) (a & b)
                    : (byte) (a ^ b);
        }
        throw new UnsupportedOperationException("Bitwise operation on non-integral type");
    }
}
