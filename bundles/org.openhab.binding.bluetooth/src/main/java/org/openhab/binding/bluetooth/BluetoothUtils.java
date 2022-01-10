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
package org.openhab.binding.bluetooth;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility class for parsing or formatting bluetooth characteristic data.
 *
 * @author Connor Petty - Initial Contribution
 *
 */
public class BluetoothUtils {

    public static final Logger logger = LoggerFactory.getLogger(BluetoothUtils.class);

    public static final int FORMAT_UINT8 = 0x11;
    public static final int FORMAT_UINT16 = 0x12;
    public static final int FORMAT_UINT32 = 0x14;
    public static final int FORMAT_SINT8 = 0x21;
    public static final int FORMAT_SINT16 = 0x22;
    public static final int FORMAT_SINT32 = 0x24;
    public static final int FORMAT_SFLOAT = 0x32;
    public static final int FORMAT_FLOAT = 0x34;

    /**
     * Converts a byte array to an int array
     *
     * @param value
     * @return
     */
    public static int[] toIntArray(byte[] value) {
        if (value == null) {
            return null;
        }
        int[] ret = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            ret[i] = value[i];
        }
        return ret;
    }

    public static byte[] toByteArray(int[] value) {
        if (value == null) {
            return null;
        }
        byte[] ret = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            ret[i] = (byte) (value[i] & 0xFF);
        }
        return ret;
    }

    /**
     * Return the stored value of this characteristic.
     *
     */
    public static Integer getIntegerValue(byte[] value, int formatType, int offset) {
        if ((offset + getTypeLen(formatType)) > value.length) {
            return null;
        }

        switch (formatType) {
            case FORMAT_UINT8:
                return unsignedByteToInt(value[offset]);

            case FORMAT_UINT16:
                return unsignedBytesToInt(value[offset], value[offset + 1]);

            case FORMAT_UINT32:
                return unsignedBytesToInt(value[offset], value[offset + 1], value[offset + 2], value[offset + 3]);

            case FORMAT_SINT8:
                return unsignedToSigned(unsignedByteToInt(value[offset]), 8);

            case FORMAT_SINT16:
                return unsignedToSigned(unsignedBytesToInt(value[offset], value[offset + 1]), 16);

            case FORMAT_SINT32:
                return unsignedToSigned(
                        unsignedBytesToInt(value[offset], value[offset + 1], value[offset + 2], value[offset + 3]), 32);
            default:
                logger.error("Unknown format type {} - no int value can be provided for it.", formatType);
        }

        return null;
    }

    /**
     * Return the stored value of this characteristic. This doesn't read the remote data.
     *
     */
    public static Float getFloatValue(byte[] value, int formatType, int offset) {
        if ((offset + getTypeLen(formatType)) > value.length) {
            return null;
        }

        switch (formatType) {
            case FORMAT_SFLOAT:
                return bytesToFloat(value[offset], value[offset + 1]);
            case FORMAT_FLOAT:
                return bytesToFloat(value[offset], value[offset + 1], value[offset + 2], value[offset + 3]);
            default:
                logger.error("Unknown format type {} - no float value can be provided for it.", formatType);
        }

        return null;
    }

    /**
     * Return the stored value of this characteristic. This doesn't read the remote data.
     *
     */
    public static String getStringValue(byte[] value, int offset) {
        if (value == null || offset > value.length) {
            return null;
        }
        byte[] strBytes = new byte[value.length - offset];
        for (int i = 0; i < (value.length - offset); ++i) {
            strBytes[i] = value[offset + i];
        }
        return new String(strBytes, StandardCharsets.UTF_8);
    }

    /**
     * Set the local value of this characteristic.
     *
     * @param value the value to set
     * @param formatType the format of the value (as one of the FORMAT_* constants in this class)
     * @param offset the offset to use when interpreting the value
     * @return true, if it has been set successfully
     */
    public static boolean setValue(byte[] dest, int value, int formatType, int offset) {
        int len = offset + getTypeLen(formatType);
        if (dest == null || len > dest.length) {
            return false;
        }
        int val = value;
        switch (formatType) {
            case FORMAT_SINT8:
                val = intToSignedBits(value, 8);
                // Fall-through intended
            case FORMAT_UINT8:
                dest[offset] = (byte) (val & 0xFF);
                break;

            case FORMAT_SINT16:
                val = intToSignedBits(value, 16);
                // Fall-through intended
            case FORMAT_UINT16:
                dest[offset] = (byte) (val & 0xFF);
                dest[offset + 1] = (byte) ((val >> 8) & 0xFF);
                break;

            case FORMAT_SINT32:
                val = intToSignedBits(value, 32);
                // Fall-through intended
            case FORMAT_UINT32:
                dest[offset] = (byte) (val & 0xFF);
                dest[offset + 1] = (byte) ((val >> 8) & 0xFF);
                dest[offset + 2] = (byte) ((val >> 16) & 0xFF);
                dest[offset + 2] = (byte) ((val >> 24) & 0xFF);
                break;

            default:
                return false;
        }
        return true;
    }

    /**
     * Set the local value of this characteristic.
     *
     * @param mantissa the mantissa of the value
     * @param exponent the exponent of the value
     * @param formatType the format of the value (as one of the FORMAT_* constants in this class)
     * @param offset the offset to use when interpreting the value
     * @return true, if it has been set successfully
     *
     */
    public static boolean setValue(byte[] dest, int mantissa, int exponent, int formatType, int offset) {
        int len = offset + getTypeLen(formatType);
        if (dest == null || len > dest.length) {
            return false;
        }

        switch (formatType) {
            case FORMAT_SFLOAT:
                int m = intToSignedBits(mantissa, 12);
                int exp = intToSignedBits(exponent, 4);
                dest[offset] = (byte) (m & 0xFF);
                dest[offset + 1] = (byte) ((m >> 8) & 0x0F);
                dest[offset + 1] += (byte) ((exp & 0x0F) << 4);
                break;

            case FORMAT_FLOAT:
                m = intToSignedBits(mantissa, 24);
                exp = intToSignedBits(exponent, 8);
                dest[offset] = (byte) (m & 0xFF);
                dest[offset + 1] = (byte) ((m >> 8) & 0xFF);
                dest[offset + 2] = (byte) ((m >> 16) & 0xFF);
                dest[offset + 2] += (byte) (exp & 0xFF);
                break;

            default:
                return false;
        }

        return true;
    }

    /**
     * Returns the size of the requested value type.
     */
    private static int getTypeLen(int formatType) {
        return formatType & 0xF;
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private static int unsignedByteToInt(int value) {
        return value & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private static int unsignedBytesToInt(int value1, int value2) {
        return value1 + (value2 << 8);
    }

    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    private static int unsignedBytesToInt(int value1, int value2, int value3, int value4) {
        return value1 + (value2 << 8) + (value3 << 16) + (value4 << 24);
    }

    /**
     * Convert signed bytes to a 16-bit short float value.
     */
    private static float bytesToFloat(int value1, int value2) {
        int mantissa = unsignedToSigned(unsignedByteToInt(value1) + ((unsignedByteToInt(value2) & 0x0F) << 8), 12);
        int exponent = unsignedToSigned(unsignedByteToInt(value2) >> 4, 4);
        return (float) (mantissa * Math.pow(10, exponent));
    }

    /**
     * Convert signed bytes to a 32-bit short float value.
     */
    private static float bytesToFloat(int value1, int value2, int value3, int value4) {
        int mantissa = unsignedToSigned(
                unsignedByteToInt(value1) + (unsignedByteToInt(value2) << 8) + (unsignedByteToInt(value3) << 16), 24);
        return (float) (mantissa * Math.pow(10, value4));
    }

    /**
     * Convert an unsigned integer to a two's-complement signed value.
     */
    private static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            return -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        } else {
            return unsigned;
        }
    }

    /**
     * Convert an integer into the signed bits of the specified length.
     */
    private static int intToSignedBits(int i, int size) {
        if (i < 0) {
            return (1 << size - 1) + (i & ((1 << size - 1) - 1));
        } else {
            return i;
        }
    }
}
