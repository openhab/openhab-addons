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
package org.openhab.binding.elroconnects.internal.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;

/**
 * The {@link ElroConnectsUtil} contains a few utility methods for the ELRO Connects binding.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public final class ElroConnectsUtil {

    private static final int POLYNOMIAL = 0x0000a001; // polynomial for CRC calculation

    public static int encode(int value) {
        return (((value ^ 0xFFFFFFFF) + 0x10000) ^ 0x123) ^ 0x1234;
    }

    public static int decode(int value, int msgId) {
        return (byte) (0xFFFF + ~((value ^ 0x1234) ^ msgId));
    }

    /**
     * Encode input string into hex
     *
     * @param input
     * @param length byte length for input string in UTF-8 encoding, further characters will be cut
     * @return encoded hex string cut to length
     */
    public static String encode(String input, int length) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        String content = "@".repeat((length > bytes.length) ? (length - bytes.length) : 0)
                + new String(bytes, StandardCharsets.UTF_8);
        bytes = Arrays.copyOf(content.getBytes(StandardCharsets.UTF_8), length + 1);
        bytes[length] = (byte) "$".charAt(0);
        return HexUtils.bytesToHex(bytes);
    }

    /**
     * Decode hex string using UTF-8 encoding and drop leading @ characters and trailing $
     *
     * @param input hex string
     * @return string contained in input
     */
    public static String decode(String input) {
        return (new String(HexUtils.hexToBytes(input), StandardCharsets.UTF_8)).replaceAll("[@$]*", "");
    }

    /**
     * Compare first bytes of byte representation of input strings in UTF-8 encoding
     *
     * @param string1
     * @param string2
     * @param length number of bytes to compare
     * @return true if equal
     */
    public static boolean equals(String string1, String string2, int length) {
        byte[] bytes1 = Arrays.copyOf(string1.getBytes(StandardCharsets.UTF_8), length);
        byte[] bytes2 = Arrays.copyOf(string2.getBytes(StandardCharsets.UTF_8), length);
        return Arrays.equals(bytes1, bytes2);
    }

    /**
     * Calculate CRC-16 for input string. The input string should be treated as ASCII characters. The calculation is
     * based on the MODBUS CRC-16 calculation.
     *
     * @param input
     * @return crc hex format
     */
    public static String crc16(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.US_ASCII);
        int crc = 0x0000ffff;
        for (byte curByte : bytes) {
            crc ^= curByte;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x00000001) == 1) {
                    crc >>= 1;
                    crc ^= POLYNOMIAL;
                } else {
                    crc >>= 1;
                }
            }
        }
        return Integer.toHexString(crc);
    }

    public static String stringOrEmpty(@Nullable String data) {
        return (data == null ? "" : data);
    }

    public static int intOrZero(@Nullable Integer data) {
        return (data == null ? 0 : data);
    }
}
