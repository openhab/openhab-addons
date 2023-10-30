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
package org.openhab.binding.tapocontrol.internal.helpers.utils;

import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

/**
 * {@link ByteUtils} ByteUtils -
 * Utility Helper Functions handling byte helper functions
 *
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class ByteUtils {

    /**
     * Truncate Byte Array
     * 
     * @param bytes full byteArray
     * @param srcPos startindex
     * @param newLength
     */
    public static byte[] truncateByteArray(byte[] bytes, int srcPos, int newLength) {
        if (bytes.length < newLength) {
            return bytes;
        } else {
            byte[] truncated = new byte[newLength];
            System.arraycopy(bytes, srcPos, truncated, 0, newLength);
            return truncated;
        }
    }

    /**
     * Concat Byte Arrays
     */
    public static byte[] concatBytes(byte[]... bytes) throws TapoErrorHandler {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            for (byte[] b : bytes) {
                outputStream.write(b);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new TapoErrorHandler(ERR_DATA_TRANSORMATION);
        }
    }

    /**
     * replace bytes in bytearray
     */
    public static byte[] replaceBytes(byte[] oldBytes, byte[] replace, int startPos) {
        System.arraycopy(replace, 0, oldBytes, startPos, replace.length);
        return oldBytes;
    }

    /**
     * HEX-STRING to byte convertion
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            }
        } catch (Exception e) {
        }
        return data;
    }

    /**
     * byte to hex-string
     */
    public static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    /**
     * byteArray to hex-string
     */
    public static String byteArrayToHex(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString();
    }

    /**
     * convert byteArray to int
     */
    public static int byteArrayToInt(byte[] bArr, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(byteOrder);
        byteBuffer.put(bArr);
        return byteBuffer.getInt();
    }

    /**
     * convert byteArray to short
     */
    public static short byteArrayToShort(byte[] bArr, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(byteOrder);
        byteBuffer.put(bArr);
        return byteBuffer.getShort();
    }

    /**
     * convert int to bytearray
     */
    public static byte[] intToByteArray(int i, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(byteOrder);
        byteBuffer.putInt(i);
        return byteBuffer.array();
    }

    /**
     * convert short to bytearray
     */
    public static byte[] shortToByteArray(short s, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.order(byteOrder);
        byteBuffer.putShort(s);
        return byteBuffer.array();
    }
}
