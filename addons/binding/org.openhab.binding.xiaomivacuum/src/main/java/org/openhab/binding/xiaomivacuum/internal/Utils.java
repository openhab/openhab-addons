/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.internal;

/**
 * Utility class for common tasks within the Xiaomi vacuum binding.
 *
 * @author Marcel Verpaalen
 *
 */

public final class Utils {

    /**
     * Convert a string representation of hexadecimal to a byte array.
     *
     * For example: String s = "00010203" returned byte array is {0x00, 0x01, 0x03}
     *
     * @param s
     * @return byte array equivalent to hex string
     **/
    public static byte[] hexStringToByteArray(String s) {
        s = s.replace(" ", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Convert a byte array to a string representation of hexadecimals.
     *
     * For example: byte array is {0x00, 0x01, 0x03} returned String s =
     * "00 01 02 03"
     *
     * @param byte array
     * @return String equivalent to hex string
     **/
    static final String HEXES = "0123456789ABCDEF";

    public static String getSpacedHex(byte[] raw) {
        if (raw == null) {
            return "";
        }
        final StringBuilder hex = new StringBuilder(3 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F))).append(" ");
        }
        hex.delete(hex.length() - 1, hex.length());
        return hex.toString();
    }

    public static String getHex(byte[] raw) {
        if (raw == null) {
            return "";
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
