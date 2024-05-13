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
package org.openhab.binding.insteon2.internal.utils;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HexUtils} represents hex utility functions
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class HexUtils {
    public static String getHexString(byte b) {
        return getHexString(b & 0xFF, 2, true);
    }

    public static String getHexString(int i) {
        return getHexString(i, 2, true);
    }

    public static String getHexString(int i, int len) {
        return getHexString(i, len, true);
    }

    public static String getHexString(int i, int len, boolean addPrefix) {
        String fmt = "%" + (len > 0 ? "0" + len : "") + "X";
        String s = String.format(fmt, i);
        if (!s.isEmpty() && addPrefix) {
            s = "0x" + s;
        }
        return s;
    }

    public static String getHexString(byte[] bytes, int len) {
        return getHexString(bytes, len, true);
    }

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

    public static boolean isValidHexString(String s) {
        String hex = s.startsWith("0x") ? s.substring(2) : s;
        return hex.matches("\\p{XDigit}{1,2}");
    }

    public static boolean isValidHexStringArray(String[] strings, int from, int to) {
        String[] array = Arrays.copyOfRange(strings, from, to);
        for (int i = 0; i < array.length; i++) {
            if (!isValidHexString(array[i])) {
                return false;
            }
        }
        return true;
    }

    public static byte[] toByteArray(String[] strings, int from, int to) throws NumberFormatException {
        String[] array = Arrays.copyOfRange(strings, from, to);
        int len = array.length;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) toInteger(array[i]);
        }
        return bytes;
    }

    public static int toInteger(String s) throws NumberFormatException {
        String hex = s.startsWith("0x") ? s.substring(2) : s;
        return Integer.parseInt(hex, 16);
    }
}
