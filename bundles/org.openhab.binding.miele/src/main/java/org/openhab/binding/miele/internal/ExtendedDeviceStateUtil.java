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
package org.openhab.binding.miele.internal;

import java.nio.charset.StandardCharsets;

/**
 * The {@link ExtendedDeviceStateUtil} class contains utility methods for parsing
 * ExtendedDeviceState information
 *
 * @author Jacob Laursen - Added power/water consumption channels
 */
public class ExtendedDeviceStateUtil {
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    /**
     * Convert byte array to hex representation.
     */
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars, StandardCharsets.UTF_8);
    }

    /**
     * Convert string consisting of 8 bit characters to byte array.
     * Note: This simple operation has been extracted and put here to document
     * and ensure correct behavior for 8 bit characters that should be turned
     * into single bytes without any UTF-8 encoding.
     */
    public static byte[] stringToBytes(String input) {
        return input.getBytes(StandardCharsets.ISO_8859_1);
    }
}
