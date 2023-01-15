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
package org.openhab.binding.jeelink.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for strings
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public final class StringUtils {

    /**
     * <p>
     * Capitalizes a String changing the first character to title case.
     * No other characters are changed.
     * </p>
     *
     * <pre>
     * StringUtils.capitalize(null)  = null
     * StringUtils.capitalize("")    = ""
     * StringUtils.capitalize("cat") = "Cat"
     * StringUtils.capitalize("cAt") = "CAt"
     * StringUtils.capitalize("'cat'") = "'cat'"
     * </pre>
     *
     * @param val the String to capitalize, may not be null
     * @return the capitalized String
     */
    public static String capitalize(String val) {
        if (val.length() == 0) {
            return val;
        }
        return val.substring(0, 1).toUpperCase() + val.substring(1);
    }
}
