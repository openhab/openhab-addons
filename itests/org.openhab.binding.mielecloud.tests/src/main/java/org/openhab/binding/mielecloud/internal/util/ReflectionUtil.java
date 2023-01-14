/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mielecloud.internal.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class for reflection operations such as accessing private fields or methods.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public final class ReflectionUtil {
    private ReflectionUtil() {
    }

    /**
     * Gets a private attribute.
     *
     * @param object The object to get the attribute from.
     * @param fieldName The name of the field to get.
     * @return The obtained value.
     * @throws SecurityException if the operation is not allowed.
     * @throws NoSuchFieldException if no field with the given name exists.
     * @throws IllegalAccessException if the field is enforcing Java language access control and is inaccessible.
     * @throws IllegalArgumentException if one of the passed parameters is invalid.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPrivate(Object object, String fieldName)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Field field = getFieldFromClassHierarchy(object.getClass(), fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    private static Field getFieldFromClassHierarchy(Class<?> clazz, String fieldName)
            throws NoSuchFieldException, SecurityException {
        Class<?> iteratedClass = clazz;
        do {
            try {
                return iteratedClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }
            iteratedClass = iteratedClass.getSuperclass();
        } while (iteratedClass != null);
        throw new NoSuchFieldException();
    }

    /**
     * Sets a private attribute.
     *
     * @param object The object to set the attribute on.
     * @param fieldName The name of the field to set.
     * @param value The value to set.
     * @throws SecurityException if the operation is not allowed.
     * @throws NoSuchFieldException if no field with the given name exists.
     * @throws IllegalAccessException if the field is enforcing Java language access control and is inaccessible.
     * @throws IllegalArgumentException if one of the passed parameters is invalid.
     */
    public static void setPrivate(Object object, String fieldName, @Nullable Object value)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    /**
     * Invokes a private method on an object.
     *
     * @param object The object to invoke the method on.
     * @param methodName The name of the method to invoke.
     * @param parameters The parameters of the method invocation.
     * @return The method call's return value.
     * @throws NoSuchMethodException if no method with the given parameters or name exists.
     * @throws SecurityException if the operation is not allowed.
     * @throws IllegalAccessException if the method is enforcing Java language access control and is inaccessible.
     * @throws IllegalArgumentException if one of the passed parameters is invalid.
     * @throws InvocationTargetException if the invoked method throws an exception.
     */
    public static <T> T invokePrivate(Object object, String methodName, Object... parameters)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException {
        Class<?>[] parameterTypes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }

        return invokePrivate(object, methodName, parameterTypes, parameters);
    }

    /**
     * Invokes a private method on an object.
     *
     * @param object The object to invoke the method on.
     * @param methodName The name of the method to invoke.
     * @param parameterTypes The types of the parameters.
     * @param parameters The parameters of the method invocation.
     * @return The method call's return value.
     * @throws NoSuchMethodException if no method with the given parameters or name exists.
     * @throws SecurityException if the operation is not allowed.
     * @throws IllegalAccessException if the method is enforcing Java language access control and is inaccessible.
     * @throws IllegalArgumentException if one of the passed parameters is invalid.
     * @throws InvocationTargetException if the invoked method throws an exception.
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokePrivate(Object object, String methodName, Class<?>[] parameterTypes, Object... parameters)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException {
        Method method = getMethodFromClassHierarchy(object.getClass(), methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return (T) method.invoke(object, parameters);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    private static Method getMethodFromClassHierarchy(Class<?> clazz, String methodName, Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        Class<?> iteratedClass = clazz;
        do {
            try {
                return iteratedClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
            }
            iteratedClass = iteratedClass.getSuperclass();
        } while (iteratedClass != null);
        throw new NoSuchMethodException();
    }
}
