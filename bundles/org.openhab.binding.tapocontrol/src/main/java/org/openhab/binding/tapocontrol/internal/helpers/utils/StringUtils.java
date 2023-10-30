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
package org.openhab.binding.tapocontrol.internal.helpers.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link StringUtils} StringUtils -
 * Utility Helper Functions handling String helper functions
 *
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class StringUtils {
    /**
     * Return Boolean from string
     * 
     * @param s - string to be converted
     * @param defVal - Default Value
     */
    public Boolean stringToBool(@Nullable String s, boolean defVal) {
        if (s == null) {
            return defVal;
        }
        try {
            return Boolean.parseBoolean(s);
        } catch (Exception e) {
            return defVal;
        }
    }

    /**
     * Return Integer from string
     * 
     * @param s - string to be converted
     * @param defVal - Default Value
     */
    public Integer stringToInteger(@Nullable String s, Integer defVal) {
        if (s == null) {
            return defVal;
        }
        try {
            return Integer.valueOf(s);
        } catch (Exception e) {
            return defVal;
        }
    }
}
