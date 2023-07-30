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
package org.openhab.binding.plugwise.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class for sharing string utility methods.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public final class StringUtils {

    public static String lowerCamelToUpperUnderscore(String text) {
        return text.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
    }

    public static String upperUnderscoreToLowerCamel(String text) {
        final String delimiter = "_";
        StringBuilder upperCamelBuilder = new StringBuilder(text.length());
        for (String str : text.split(delimiter)) {
            if (upperCamelBuilder.isEmpty() && str.length() > 0) {
                upperCamelBuilder.append(str.substring(0, 1).toLowerCase());
            } else if (str.length() > 0) {
                upperCamelBuilder.append(str.substring(0, 1).toUpperCase());
            }
            if (str.length() > 1) {
                upperCamelBuilder.append(str.substring(1).toLowerCase());
            }
        }
        return upperCamelBuilder.toString();
    }

    /**
     * <p>
     * If a newline char exists at the end of the line it is removed
     * </p>
     *
     * <pre>
     * Util.chomp(null)          = null
     * Util.chomp("")            = ""
     * Util.chomp("abc \r")      = "abc "
     * Util.chomp("abc\n")       = "abc"
     * Util.chomp("abc\r\n")     = "abc"
     * Util.chomp("abc\r\n\r\n") = "abc\r\n"
     * Util.chomp("abc\n\r")     = "abc\n"
     * Util.chomp("abc\n\rabc")  = "abc\n\rabc"
     * Util.chomp("\r")          = ""
     * Util.chomp("\n")          = ""
     * Util.chomp("\r\n")        = ""
     * </pre>
     *
     * @param str nullable string
     * @return chomped string (nullable)
     */
    public static @Nullable String chomp(final @Nullable String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.endsWith("\r\n")) {
            return str.substring(0, str.length() - 2);
        } else if (str.endsWith("\r") || str.endsWith("\n")) {
            return str.substring(0, str.length() - 1);
        } else {
            return str;
        }
    }
}
