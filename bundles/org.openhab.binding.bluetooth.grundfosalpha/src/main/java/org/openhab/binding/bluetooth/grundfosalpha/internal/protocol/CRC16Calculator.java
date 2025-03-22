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
package org.openhab.binding.bluetooth.grundfosalpha.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for checksum calculation using the CRC-16-CCITT algorithm.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class CRC16Calculator {

    public static final int[] LOOKUP_TABLE = { 0, 4129, 8258, 12387, 16516, 20645, 24774, 28903, 33032, 37161, 41290,
            45419, 49548, 53677, 57806, 61935, 4657, 528, 12915, 8786, 21173, 17044, 29431, 25302, 37689, 33560, 45947,
            41818, 54205, 50076, 62463, 58334, 9314, 13379, 1056, 5121, 25830, 29895, 17572, 21637, 42346, 46411, 34088,
            38153, 58862, 62927, 50604, 54669, 13907, 9842, 5649, 1584, 30423, 26358, 22165, 18100, 46939, 42874, 38681,
            34616, 63455, 59390, 55197, 51132, 18628, 22757, 26758, 30887, 2112, 6241, 10242, 14371, 51660, 55789,
            59790, 63919, 35144, 39273, 43274, 47403, 23285, 19156, 31415, 27286, 6769, 2640, 14899, 10770, 56317,
            52188, 64447, 60318, 39801, 35672, 47931, 43802, 27814, 31879, 19684, 23749, 11298, 15363, 3168, 7233,
            60846, 64911, 52716, 56781, 44330, 48395, 36200, 40265, 32407, 28342, 24277, 20212, 15891, 11826, 7761,
            3696, 65439, 61374, 57309, 53244, 48923, 44858, 40793, 36728, 37256, 33193, 45514, 41451, 53516, 49453,
            61774, 57711, 4224, 161, 12482, 8419, 20484, 16421, 28742, 24679, 33721, 37784, 41979, 46042, 49981, 54044,
            58239, 62302, 689, 4752, 8947, 13010, 16949, 21012, 25207, 29270, 46570, 42443, 38312, 34185, 62830, 58703,
            54572, 50445, 13538, 9411, 5280, 1153, 29798, 25671, 21540, 17413, 42971, 47098, 34713, 38840, 59231, 63358,
            50973, 55100, 9939, 14066, 1681, 5808, 26199, 30326, 17941, 22068, 55628, 51565, 63758, 59695, 39368, 35305,
            47498, 43435, 22596, 18533, 30726, 26663, 6336, 2273, 14466, 10403, 52093, 56156, 60223, 64286, 35833,
            39896, 43963, 48026, 19061, 23124, 27191, 31254, 2801, 6864, 10931, 14994, 64814, 60687, 56684, 52557,
            48554, 44427, 40424, 36297, 31782, 27655, 23652, 19525, 15522, 11395, 7392, 3265, 61215, 65342, 53085,
            57212, 44955, 49082, 36825, 40952, 28183, 32310, 20053, 24180, 11923, 16050, 3793, 7920 };

    private static final int CRC16_INITIAL_VALUE = 0xffff;
    private static final int CRC16_FINAL_XOR = 0xffff;
    private static final int BYTE_MASK = 0xff;

    private CRC16Calculator() {
        // Prevent instantiation
    }

    /**
     * Validates the CRC-16 checksum for the given data.
     *
     * @param data The data to validate, including checksum bytes.
     * @return True if the checksum is valid, false otherwise.
     */
    public static boolean check(byte[] data) {
        if (data.length < 3) {
            throw new IllegalArgumentException("Data array must contain at least 3 bytes.");
        }

        int dataLength = (data[1] & BYTE_MASK);
        if (dataLength + 4 > data.length) {
            throw new IllegalArgumentException("Invalid data length specified in the array.");
        }

        int crcValue = calculate(data, 1, dataLength + 1);
        byte lowByte = getLowByte(crcValue);
        byte highByte = getHighByte(crcValue);

        return data[dataLength + 2] == highByte && data[dataLength + 3] == lowByte;
    }

    /**
     * Calculates the CRC-16 checksum for the given data and puts it into
     * the last two bytes of the given data array.
     *
     * @param data The data array to calculate CRC-16 with the two last bytes reserved for the result
     * @param length The message size excluding start delimiter/size (first two bytes) and CRC-16 checksum (last two
     *            bytes)
     */
    public static void put(byte[] data, int length) {
        int dataLength = (data[1] & BYTE_MASK);
        if (dataLength + 4 > data.length) {
            throw new IllegalArgumentException("Invalid data length specified in the array.");
        }
        int crcValue = calculate(data, 1, length + 1);
        data[length + 2] = getHighByte(crcValue);
        data[length + 3] = getLowByte(crcValue);
    }

    private static int calculate(byte[] data, int start, int length) {
        int value = CRC16_INITIAL_VALUE;

        for (int i = 0; i < length; i++) {
            int inputByte = data[start + i] & BYTE_MASK;
            int tableIndex = (inputByte ^ (value >> 8)) & BYTE_MASK;
            value = (LOOKUP_TABLE[tableIndex] ^ (value << 8)) & 0xffff;
        }

        return value ^ CRC16_FINAL_XOR;
    }

    private static byte getLowByte(int word) {
        return (byte) (word & BYTE_MASK);
    }

    private static byte getHighByte(int word) {
        return (byte) ((word >> 8) & BYTE_MASK);
    }
}
