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
package org.openhab.binding.gardena.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Some String operations from commons lang.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class StringUtils {
    /**
     * Gets the substring before the first occurrence of a separator.
     */
    public static @Nullable String substringBefore(@Nullable String str, String separator) {
        if (str != null && !str.isEmpty()) {
            int pos = str.indexOf(separator);
            return pos == -1 ? str : str.substring(0, pos);
        } else {
            return str;
        }
    }

    /**
     * Gets the substring before the last occurrence of a separator.
     */
    public static @Nullable String substringBeforeLast(@Nullable String str, String separator) {
        if (str != null && !str.isEmpty()) {
            int pos = str.lastIndexOf(separator);
            return pos == -1 ? str : str.substring(0, pos);
        } else {
            return str;
        }
    }

    /**
     * Gets the substring after the last occurrence of a separator.
     */
    public static @Nullable String substringAfterLast(@Nullable String str, String separator) {
        if (str != null && !str.isEmpty()) {
            int pos = str.lastIndexOf(separator);
            return pos != -1 && pos != str.length() - separator.length() ? str.substring(pos + separator.length()) : "";
        } else {
            return str;
        }
    }
}
