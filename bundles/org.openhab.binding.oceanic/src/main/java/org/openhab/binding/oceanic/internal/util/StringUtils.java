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
package org.openhab.binding.oceanic.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class for utility methods
 *
 * @author Leo Siepel - Initial contribution
 */

@NonNullByDefault
public class StringUtils {

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
