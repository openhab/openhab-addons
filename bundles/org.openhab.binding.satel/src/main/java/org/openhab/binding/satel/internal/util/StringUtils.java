/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Replacement class for Apache's StringUtils.
 *
 * @author Krzysztof Goworek - Initial contribution
 *
 */
@NonNullByDefault
public class StringUtils {

    /**
     * Checks if a string is empty or null.
     *
     * @param str the string to check
     * @return <code>true</code> if given string is empty or null
     */
    public static boolean isEmpty(@Nullable String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Checks if a string is not empty and not null.
     *
     * @param str the string to check
     * @return <code>true</code> if given string is not empty and not null
     */
    public static boolean isNotEmpty(@Nullable String str) {
        return !isEmpty(str);
    }

    /**
     * Checks if a string is null or empty or all characters are whitespace.
     *
     * @param str the string to check
     * @return <code>true</code> if given string is blank
     */
    public static boolean isBlank(@Nullable String str) {
        return str == null || str.isBlank();
    }

    /**
     * Checks if a string is not null, not empty and contains at least one non-whitespace character.
     *
     * @param str the string to check
     * @return <code>true</code> if given string is not blank
     */
    public static boolean isNotBlank(@Nullable String str) {
        return !isBlank(str);
    }
}
