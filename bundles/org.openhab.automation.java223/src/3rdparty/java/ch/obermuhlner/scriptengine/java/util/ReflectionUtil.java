package ch.obermuhlner.scriptengine.java.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ReflectionUtil {
    private ReflectionUtil() {
        // does nothing
    }

    public static boolean matchesArguments(Constructor<?> constructor, Object[] arguments) {
        return matchesArguments(constructor.getParameterTypes(), arguments);
    }

    public static boolean matchesArguments(Method method, Object[] arguments) {
        return matchesArguments(method.getParameterTypes(), arguments);
    }

    public static boolean matchesArguments(Class<?>[] argumentTypes, Object[] arguments) {
        if (arguments.length != argumentTypes.length) {
            return false;
        }

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] == null) {
                if (argumentTypes[i].isPrimitive()) {
                    return false;
                }
            } else {
                if (!matchesType(argumentTypes[i], arguments[i].getClass())) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean matchesType(Class<?> parameterType, Class<?> argumentType) {
        if ((parameterType == int.class && argumentType == Integer.class)
                || (parameterType == long.class && argumentType == Long.class)
                || (parameterType == short.class && argumentType == Short.class)
                || (parameterType == byte.class && argumentType == Byte.class)
                || (parameterType == boolean.class && argumentType == Boolean.class)
                || (parameterType == float.class && argumentType == Float.class)
                || (parameterType == double.class && argumentType == Double.class)
                || (parameterType == char.class && argumentType == Character.class)) {
            return true;
        }
        return parameterType.isAssignableFrom(argumentType);
    }

}
