/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Byte utility functions
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class ByteUtils {
    public static String getBinaryString(byte b) {
        return getBinaryString(b & 0xFF);
    }

    public static String getBinaryString(int b) {
        String binary = Integer.toBinaryString(b);
        return String.format("%8s", binary).replace(" ", "0");
    }

    public static String getHexString(byte b) {
        return getHexString(b & 0xFF, 2);
    }

    public static String getHexString(int i) {
        return getHexString(i, 2);
    }

    public static String getHexString(int i, int len) {
        String fmt = "0x%" + (len > 0 ? "0" + len : "") + "X";
        return String.format(fmt, i);
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

    public static boolean isValidHexStringArray(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            if (!strings[i].matches("\\p{XDigit}{1,2}")) {
                return false;
            }
        }
        return true;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static int hexStringToInteger(String s) throws NumberFormatException {
        String hex = s.startsWith("0x") ? s.substring(2) : s;
        return Integer.parseInt(hex, 16);
    }
}
