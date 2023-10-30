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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link TapoUtils} TapoUtils -
 * Utility Helper Functions
 *
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class TapoUtils {

    /************************************
     * CALCULATION UTILS
     ***********************************/
    /**
     * Limit Value between limits
     * 
     * @param value Integer
     * @param lowerLimit
     * @param upperLimit
     * @return
     */
    public static Integer limitVal(@Nullable Integer value, Integer lowerLimit, Integer upperLimit) {
        if (value == null || value < lowerLimit) {
            return lowerLimit;
        } else if (value > upperLimit) {
            return upperLimit;
        }
        return value;
    }

    /************************************
     * FORMAT UTILS
     ***********************************/
    /**
     * return value or default val if it's null
     * 
     * @param <T> Type of value
     * @param value value
     * @param defaultValue defaut value
     * @return
     */
    public static <T> T getValueOrDefault(@Nullable T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * compare tow values against an comparator and return the other one
     * if both are null, comparator will be returned - if both have values val2 will be returned
     * 
     * @param <T> Type of return value
     * @param val1 fist value to campare - will be returned if val2 is null or matches comparator
     * @param val2 second value to compare - will be returned if val1 is null or matches comparator
     * @param comparator compared values with this
     * @return
     */
    public static <T> T compareValuesAgainstComparator(@Nullable T val1, @Nullable T val2, T comparator) {
        if (val1 == null && val2 == null) {
            return comparator;
        } else if (val1 != null && (val2 == null || val2.equals(comparator))) {
            return Objects.requireNonNull(val1);
        } else if (val1 == null || val1.equals(comparator)) {
            return Objects.requireNonNull(val2);
        } else {
            return Objects.requireNonNull(val2);
        }
    }

    /**
     * Format MAC-Address replacing old division chars and add new one
     * 
     * @param mac unformated mac-Address
     * @param newDivisionChar new division char (e.g. ":","-" )
     * @return new formated mac-Address
     */
    public static String formatMac(String mac, char newDivisionChar) {
        String unformatedMac = unformatMac(mac);
        String formatedMac = unformatedMac.replaceAll("(.{2})", "$1" + newDivisionChar).substring(0, 17);
        return formatedMac;
    }

    /**
     * unformat MAC-Address replace all division chars
     * 
     * @param mac
     * @return
     */
    public static String unformatMac(String mac) {
        mac = mac.replace("-", "");
        mac = mac.replace(":", "");
        mac = mac.replace(".", "");
        return mac;
    }
}
