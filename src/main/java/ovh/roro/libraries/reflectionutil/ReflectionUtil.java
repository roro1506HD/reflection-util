package ovh.roro.libraries.reflectionutil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public class ReflectionUtil {

    public static final @NotNull ObfuscationHelper OBFUSCATION_HELPER = new ObfuscationHelper();

    private static final @NotNull MethodHandles.Lookup LOOKUP;
    private static final @NotNull Unsafe UNSAFE;

    private ReflectionUtil() throws IllegalAccessException {
        throw new IllegalAccessException("This class cannot be instantiated");
    }

    static {
        MethodHandles.Lookup lookup;
        Unsafe unsafe;

        try {
            lookup = MethodHandles.lookup();
            unsafe = (Unsafe) ReflectionUtil.getField(Unsafe.class, "theUnsafe").get(null);
        } catch (Throwable ex) {
            throw new IllegalStateException("Couldn't get Unsafe", ex);
        }

        LOOKUP = lookup;
        UNSAFE = unsafe;
    }

    public static @NotNull Class<?> getClass(@NotNull String classPath) {
        try {
            String obfClassName = ReflectionUtil.OBFUSCATION_HELPER.getObfClassName(classPath);

            return Class.forName(Objects.requireNonNullElse(obfClassName, classPath));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not find class", ex);
        }
    }

    public static @NotNull Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        try {
            Field field = clazz.getDeclaredField(ReflectionUtil.OBFUSCATION_HELPER.getObfFieldName(clazz, fieldName));
            field.setAccessible(true);

            return field;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not find field", ex);
        }
    }

    public static @NotNull Method getMethod(@NotNull Class<?> clazz, @NotNull String methodName, @NotNull Class<?> @NotNull ... params) {
        try {
            Class<?>[] args = new Class<?>[params.length - 1];
            Class<?> returnType = params[params.length - 1];

            System.arraycopy(params, 0, args, 0, args.length);

            Method method = clazz.getDeclaredMethod(ReflectionUtil.OBFUSCATION_HELPER.getObfMethodName(clazz, methodName, args, returnType), args);
            method.setAccessible(true);

            return method;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not find method", ex);
        }
    }

    public static @NotNull MethodHandle getMethodHandle(@NotNull String className, @NotNull String methodName, @NotNull Class<?> @NotNull ... params) {
        Class<?> clazz = ReflectionUtil.getClass(className);

        return ReflectionUtil.getMethodHandle(clazz, methodName, params);
    }

    public static @NotNull MethodHandle getMethodHandle(@NotNull Class<?> clazz, @NotNull String methodName, @NotNull Class<?> @NotNull ... params) {
        try {
            Method method = ReflectionUtil.getMethod(clazz, methodName, params);

            return ReflectionUtil.LOOKUP.unreflect(method);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not find MethodHandle");
        }
    }

    public static @NotNull FieldAccessor getFieldAccessor(@NotNull Class<?> clazz, @NotNull String fieldName) {
        Field field = ReflectionUtil.getField(clazz, fieldName);

        return new FieldAccessor(clazz, field, ReflectionUtil.UNSAFE);
    }

    public static @NotNull FieldAccessor getFieldAccessor(@NotNull Class<?> clazz, @NotNull Field field) {
        return new FieldAccessor(clazz, field, ReflectionUtil.UNSAFE);
    }

    public static @NotNull FieldAccessor getFieldAccessor(@NotNull String className, @NotNull String fieldName) {
        Class<?> clazz = ReflectionUtil.getClass(className);

        return ReflectionUtil.getFieldAccessor(clazz, fieldName);
    }

    public static @NotNull FieldAccessor getFieldAccessor(@Nullable Class<?> clazz, long fieldOffset, boolean isStatic) {
        return new FieldAccessor(clazz, ReflectionUtil.UNSAFE, isStatic, fieldOffset);
    }
}
