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
package org.openhab.binding.paradoxalarm.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxUtil} Utility class for different calculations / manipulations of data in the model and
 * communicators.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxUtil {

    private static final String SPACE_DELIMITER = " ";
    private static final Logger logger = LoggerFactory.getLogger(ParadoxUtil.class);

    public static byte calculateChecksum(byte[] payload) {
        int result = 0;
        for (byte everyByte : payload) {
            result += everyByte;
        }

        return (byte) (result % 256);
    }

    public static byte getBit(int value, int bitNumber) {
        return (byte) ((value >> bitNumber) & 1);
    }

    public static boolean isBitSet(int value, int bitNumber) {
        return ((value >> bitNumber) & 1) == 1;
    }

    public static void printPacket(String description, byte[] array) {
        if (logger.isTraceEnabled()) {
            logger.trace("Packet payload size: {}", array[1]);
            printByteArray(description, array, array[1] + 16);
        }
    }

    public static void printByteArray(String description, byte[] array) {
        if (array == null) {
            logger.trace("Array is null");
            return;
        }
        printByteArray(description, array, array.length);
    }

    public static void printByteArray(String description, byte[] array, int length) {
        if (!logger.isTraceEnabled()) {
            return;
        }

        String result = byteArrayToString(array, length);
        if (!result.isEmpty()) {
            logger.trace("{}", description + SPACE_DELIMITER + result);
        }
    }

    public static String byteArrayToString(byte[] array) {
        return byteArrayToString(array, array.length);
    }

    /**
     *
     * Returns passed array as HEX string. On every 8 bytes we put space for better readability. Example 16
     * bytes array output: AA47000263000000 03EE00EEEEEEB727
     *
     * @param array
     * @param length
     * @return String
     */
    public static String byteArrayToString(byte[] array, int length) {
        if (array == null) {
            throw new IllegalArgumentException("Array must not be null.");
        }
        if (length > array.length) {
            throw new IllegalArgumentException("Length should be lower than or equal to array length. Length=" + length
                    + ". Array length=" + array.length);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i != 0 && i % 8 == 0) {
                sb.append(SPACE_DELIMITER);
            }
            sb.append(String.format("%02X", array[i]));
        }
        return sb.toString();
    }

    public static byte setBit(byte byteValue, int i, int j) {
        if (j == 1) {
            return (byte) (byteValue | (1 << i));
        } else {
            return (byte) (byteValue & ~(1 << i));
        }
    }

    public static byte getHighNibble(byte value) {
        return (byte) ((value & 0xF0) >> 4);
    }

    public static byte getLowNibble(byte value) {
        return (byte) (value & 0x0F);
    }

    public static byte[] mergeByteArrays(byte[]... arrays) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (byte[] array : arrays) {
                outputStream.write(array);
            }
            byte[] byteArray = outputStream.toByteArray();
            return byteArray;
        } catch (IOException e) {
            logger.warn("Exception merging arrays:", e);
            return new byte[0];
        }
    }

    public static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).order(ByteOrder.BIG_ENDIAN).putInt(value).array();
    }

    public static byte[] shortToByteArray(short value) {
        return ByteBuffer.allocate(Short.SIZE / Byte.SIZE).order(ByteOrder.BIG_ENDIAN).putShort(value).array();
    }

    public static byte[] stringToBCD(String pcPassword) {
        return stringToBCD(pcPassword, 4);
    }

    public static byte[] stringToBCD(String pcPassword, int numberOfDigits) {
        byte[] result = new byte[numberOfDigits / 2];
        for (int i = 0, j = 0; i < 2; i++, j += 2) {
            String substring = pcPassword.substring(j, j + 1);
            int parseInt = Integer.parseInt(substring);
            result[i] = (byte) ((parseInt & 0x0F) << 4);

            substring = pcPassword.substring(j + 1, j + 2);
            parseInt = Integer.parseInt(substring);
            result[i] |= (byte) (parseInt & 0x0F);
        }
        return result;
    }

    /**
     * This method fills array with 0xEE based on rate.
     * Example: If input array length is 5 and rate is 8 the array will be extended with 3 more bytes filled with 0xEE
     *
     * @param inputArray
     * @param rate
     * @return byte[]
     */
    public static byte[] extendArray(byte[] inputArray, int rate) {
        if (inputArray == null || inputArray.length % rate == 0) {
            return inputArray;
        }

        final int newLength = inputArray.length + (rate - inputArray.length % rate);
        byte[] result = new byte[newLength];
        for (int i = 0; i < result.length; i++) {
            if (i < inputArray.length) {
                result[i] = inputArray[i];
            } else {
                result[i] = (byte) 0xEE;
            }
        }
        return result;
    }

    /**
     * Returns bytes from string with standard US_ASCII standard charset to ensure everywhere in the binding we use same
     * charset.
     *
     * @param str
     * @return byte[]
     *
     */
    public static byte[] getBytesFromString(String str) {
        if (str == null) {
            throw new IllegalArgumentException("String must not be null !");
        }

        return str.getBytes(StandardCharsets.US_ASCII);
    }

    public static int[] toIntArray(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input array must not be null");
        }
        int[] result = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = input[i] & 0xFF;
        }

        return result;
    }

    public static byte[] toByteArray(int[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input array must not be null");
        }
        byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = (byte) (input[i]);
        }

        return result;
    }
}
