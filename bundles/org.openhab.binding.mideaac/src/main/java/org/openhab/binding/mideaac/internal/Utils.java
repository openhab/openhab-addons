/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jose4j.base64url.Base64;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link Utils} class defines common byte and String array methods
 * which are used across the whole binding.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - JavaDoc, reversed array and refined query String method
 */
@NonNullByDefault
public class Utils {
    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    static byte[] empty = new byte[0];

    /**
     * Converts byte array to binary string
     * 
     * @param bytes bytes to convert
     * @return string of hex chars
     */
    public static String bytesToBinary(byte[] bytes) {
        String s1 = "";
        for (int j = 0; j < bytes.length; j++) {
            s1 = s1.concat(Integer.toBinaryString(bytes[j] & 255 | 256).substring(1));
            s1 = s1.concat(" ");
        }
        return s1;
    }

    /**
     * Validates the IP address format
     * 
     * @param ip string of IP Address
     * @return IP pattern OK
     */
    public static boolean validateIP(final String ip) {
        String pattern = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        return ip.matches(pattern);
    }

    /**
     * Converts hex string to a byte array
     * 
     * @param string string to convert to byte array
     * @return byte [] array
     */
    public static byte[] hexStringToByteArray(String string) {
        return HexUtils.hexToBytes(string);
    }

    /**
     * Adds two byte arrays together
     * 
     * @param a input byte array 1
     * @param b input byte array 2
     * @return byte array
     */
    public static byte[] concatenateArrays(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /**
     * Arrange byte order
     * 
     * @param i input
     * @return @return byte array
     */
    public static byte[] toBytes(short i) {
        ByteBuffer b = ByteBuffer.allocate(2);
        b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
        b.putShort(i);
        return b.array();
    }

    /**
     * Combine byte arrays
     * 
     * @param array1 input array
     * @param array2 input array
     * @return byte array
     */
    public static byte[] strxor(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length];
        int i = 0;
        for (byte b : array1) {
            result[i] = (byte) (b ^ array2[i++]);
        }
        return result;
    }

    /**
     * Create URL safe token
     * 
     * @param nbytes number of bytes
     * @return encoded string
     */
    public static String tokenUrlsafe(int nbytes) {
        Random r = new Random();
        byte[] bytes = new byte[nbytes];
        r.nextBytes(bytes);
        return Base64.encode(bytes);
    }

    /**
     * Extracts 6 bits and reorders them based on signed or unsigned
     * 
     * @param i input
     * @param order byte order
     * @return reordered array
     */
    public static byte[] toIntTo6ByteArray(long i, ByteOrder order) {
        final ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(order);

        bb.putLong(i);

        if (order == ByteOrder.BIG_ENDIAN) {
            return Arrays.copyOfRange(bb.array(), 2, 8);
        }

        if (order == ByteOrder.LITTLE_ENDIAN) {
            return Arrays.copyOfRange(bb.array(), 0, 6);
        }
        return empty;
    }

    /**
     * String Builder for Hash
     * 
     * @param json JSON object
     * @return string
     */
    public static String getQueryString(JsonObject json, boolean hash) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> keys = json.keySet().stream().sorted().iterator();
        while (keys.hasNext()) {
            @Nullable
            String key = keys.next();
            String value = json.get(key).getAsString();

            try {
                String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());

                if (hash) {
                    // For hash generation, preserve + and @ characters in values.
                    encodedValue = encodedValue.replace("%2B", "+");
                    encodedValue = encodedValue.replace("%40", "@");
                }

                // Append the encoded key and value to the query string
                sb.append(encodedKey).append("=").append(encodedValue);

                if (keys.hasNext()) {
                    sb.append("&"); // To allow for another argument.
                }
            } catch (UnsupportedEncodingException e) {
                logger.debug("Error encoding key and value", e);
            }
        }
        return sb.toString();
    }

    /**
     * Used to reverse (or unreverse) the deviceId
     * 
     * @param array input array
     * @return reversed array
     */
    public static byte[] reverse(byte[] array) {
        int left = 0;
        int right = array.length - 1;
        while (left < right) {
            byte temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
        return array;
    }
}
