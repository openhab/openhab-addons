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
package org.openhab.binding.insteon.internal.utils;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HexUtils} represents hex utility functions
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class HexUtils {
    /**
     * Returns a hex string for a given byte
     *
     * @param b the byte
     * @return the formatted hex string
     */
    public static String getHexString(byte b) {
        return getHexString(b & 0xFF, 2, true);
    }

    /**
     * Returns a hex string for a given integer
     *
     * @param i the integer
     * @return the formatted hex string
     */
    public static String getHexString(int i) {
        return getHexString(i, 2, true);
    }

    /**
     * Returns a hex string for a given integer and length
     *
     * @param i the integer
     * @param len the string length
     * @return the formatted hex string
     */
    public static String getHexString(int i, int len) {
        return getHexString(i, len, true);
    }

    /**
     * Returns a hex string for a given integer, length and prefix flag
     *
     * @param i the integer
     * @param len the string length
     * @param addPrefix if hex prefix should be added
     * @return the formatted hex string
     */
    public static String getHexString(int i, int len, boolean addPrefix) {
        String fmt = "%" + (len > 0 ? "0" + len : "") + "X";
        String s = String.format(fmt, i);
        if (!s.isEmpty() && addPrefix) {
            s = "0x" + s;
        }
        return s;
    }

    /**
     * Returns a hex string for a given byte array and length
     *
     * @param bytes the byte array
     * @param len the string length
     * @return the formatted hex string
     */
    public static String getHexString(byte[] bytes, int len) {
        return getHexString(bytes, len, true);
    }

    /**
     * Returns a hex string for a given byte array, length and prefix flag
     *
     * @param bytes the byte array
     * @param len the string length
     * @param addPrefix if hex prefix should be added
     * @return the formatted hex string
     */
    public static String getHexString(byte[] bytes, int len, boolean addPrefix) {
        String s = "";
        for (int i = 0; i < bytes.length && i < len; i++) {
            s += String.format("%02X", bytes[i] & 0xFF);
        }
        if (!s.isEmpty() && addPrefix) {
            s = "0x" + s;
        }
        return s;
    }

    /**
     * Returns if a hex string is valid
     *
     * @param s the string to validate
     * @return true if valid hex string
     */
    public static boolean isValidHexString(String s) {
        String hex = s.startsWith("0x") ? s.substring(2) : s;
        return hex.matches("\\p{XDigit}{1,2}");
    }

    /**
     * Returns if a hex string array is valid
     *
     * @param strings the string array to validate
     * @param from the array start index
     * @param to the array end index
     * @return true if valid hex string array
     */
    public static boolean isValidHexStringArray(String[] strings, int from, int to) {
        String[] array = Arrays.copyOfRange(strings, from, to);
        for (int i = 0; i < array.length; i++) {
            if (!isValidHexString(array[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a hex string as byte array
     *
     * @param s the hex string to convert
     * @return the converted byte array
     * @throws IllegalArgumentException if invalid hex string
     */
    public static byte[] toByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("string length not even: " + len);
        }
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) toInteger(s.substring(i, i + 2));
        }
        return bytes;
    }

    /**
     * Returns a hex string array as byte array
     *
     * @param strings the hex string array to convert
     * @param from the array start index
     * @param to the array end index
     * @return the converted byte array
     * @throws NumberFormatException
     */
    public static byte[] toByteArray(String[] strings, int from, int to) throws NumberFormatException {
        String[] array = Arrays.copyOfRange(strings, from, to);
        int len = array.length;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) toInteger(array[i]);
        }
        return bytes;
    }

    /**
     * Returns a hex string as an integer
     *
     * @param s the hex string to convert
     * @return the converted integer
     * @throws NumberFormatException
     */
    public static int toInteger(String s) throws NumberFormatException {
        String hex = s.startsWith("0x") ? s.substring(2) : s;
        return Integer.parseInt(hex, 16);
    }
}
