/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.gardena.internal.util;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gardena.internal.exception.GardenaException;

/**
 * Utility class to read nested properties.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class PropertyUtils {

    /**
     * Returns true if the property is null.
     */
    public static boolean isNull(@Nullable Object instance, String propertyPath) throws GardenaException {
        return getPropertyValue(instance, propertyPath, Object.class) == null;
    }

    /**
     * Returns the property value from the object instance, nested properties are possible.
     */
    public static <T> @Nullable T getPropertyValue(@Nullable Object instance, String propertyPath, Class<T> resultClass)
            throws GardenaException {
        String[] properties = propertyPath.split("\\.");
        return getPropertyValue(instance, properties, resultClass, 0);
    }

    /**
     * Iterates through the nested properties and returns the field value.
     */
    @SuppressWarnings("unchecked")
    private static <T> @Nullable T getPropertyValue(@Nullable Object instance, String[] properties,
            Class<T> resultClass, int nestedIndex) throws GardenaException {
        if (instance == null) {
            return null;
        }
        try {
            String propertyName = properties[nestedIndex];
            Field field = instance.getClass().getField(propertyName);
            Object result = field.get(instance);
            if (nestedIndex + 1 < properties.length) {
                return getPropertyValue(result, properties, resultClass, nestedIndex + 1);
            }
            return (T) result;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new GardenaException(ex.getMessage(), ex);
        }
    }
}
