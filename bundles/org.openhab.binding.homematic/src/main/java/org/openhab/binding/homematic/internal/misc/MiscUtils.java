/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global utility class with helper methods.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class MiscUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiscUtils.class);

    /**
     * Replaces invalid characters of the text to fit into an openHAB UID.
     */
    public static String validateCharacters(String text, String textType, String replaceChar) {
        if (text == null) {
            return "EMPTY";
        }
        String cleanedText = text.replaceAll("[^A-Za-z0-9_-]", replaceChar);
        if (!text.equals(cleanedText)) {
            LOGGER.debug("{} '{}' contains invalid characters, new {} '{}'", textType, text, textType, cleanedText);
        }
        return cleanedText;
    }

    /**
     * Returns true, if the value is not null and true.
     */
    public static boolean isTrueValue(Object value) {
        return Boolean.TRUE.equals(value);
    }

    /**
     * Returns true, if the value is not null and false.
     */
    public static boolean isFalseValue(Object value) {
        return Boolean.FALSE.equals(value);
    }

    /**
     * Returns true, if str starts with search. Check is done case-insensitive.
     */
    public static boolean strStartsWithIgnoreCase(String str, String search) {
        if (str == null || search == null || search.length() > str.length()) {
            return false;
        }
        return str.substring(0, search.length()).equalsIgnoreCase(search);
    }

    /**
     * Returns true if address is a device
     */
    public static boolean isDevice(String address) {
        return isDevice(address, false);
    }

    /**
     * Returns true if address is a device. If allowBidCos ist true then addresses starting with "BidCos" classified as
     * devices, too.
     */
    public static boolean isDevice(String address, boolean allowBidCos) {
        if (address == null) {
            return false;
        }
        if (address.contains(":")) {
            return false;
        }
        if (allowBidCos && strStartsWithIgnoreCase(address.trim(), "BidCos")) {
            return true;
        }
        return !strStartsWithIgnoreCase(address.trim(), "BidCos");
    }

    /**
     * Changes all characters after whitespace to upper-case and all other character to lower case.
     */
    public static String capitalize(String value) {
        if (value == null) {
            return null;
        }
        char[] chars = value.toCharArray();
        boolean capitalizeNextChar = true;
        for (int i = 0; i < chars.length; i++) {
            if (Character.isWhitespace(chars[i])) {
                capitalizeNextChar = true;
            } else {
                if (capitalizeNextChar) {
                    chars[i] = Character.toTitleCase(chars[i]);
                    capitalizeNextChar = false;
                } else {
                    chars[i] = Character.toLowerCase(chars[i]);
                }
            }
        }
        return new String(chars);
    }
}
