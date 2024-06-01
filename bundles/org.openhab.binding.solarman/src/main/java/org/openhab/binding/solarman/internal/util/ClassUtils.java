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
package org.openhab.binding.solarman.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link StringUtils} common class utility functions
 *
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class ClassUtils {
    public static String getShortClassName(@Nullable Class<?> cls) {
        if (cls == null) {
            return "";
        }

        String className = cls.getName();
        if (className.isEmpty()) {
            return "";
        }

        // Handle array types
        if (className.startsWith("[")) {
            int arrayDimension = 0;
            while (className.charAt(arrayDimension) == '[') {
                arrayDimension++;
            }
            String baseType = className.substring(arrayDimension);
            StringBuilder result = new StringBuilder();
            switch (baseType.charAt(0)) {
                case 'L' -> { // Object array
                    baseType = baseType.substring(1, baseType.length() - 1); // Strip 'L' and ';'
                    result.append(getSimpleName(baseType));
                }
                case 'B' -> result.append("byte");
                case 'C' -> result.append("char");
                case 'D' -> result.append("double");
                case 'F' -> result.append("float");
                case 'I' -> result.append("int");
                case 'J' -> result.append("long");
                case 'S' -> result.append("short");
                case 'Z' -> result.append("boolean");
                default -> result.append(baseType);
            }
            result.append("[]".repeat(arrayDimension));
            return result.toString();
        }

        return getSimpleName(className);
    }

    /**
     * Extracts the simple class name from a fully qualified class name.
     *
     * @param className the fully qualified class name
     * @return the simple class name
     */
    private static String getSimpleName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return className; // No package
        }
        return className.substring(lastDotIndex + 1);
    }
}
