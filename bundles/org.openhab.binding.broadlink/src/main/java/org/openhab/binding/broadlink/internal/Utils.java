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
package org.openhab.binding.broadlink.internal;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;

/**
 * Utilities for working with the Broadlink devices.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class Utils {
    /**
     * Checks whether the status of the thing is online
     *
     * @param thing
     * @return true if thing status is online, false otherwise
     */
    public static boolean isOnline(Thing thing) {
        return thing.getStatus().equals(ThingStatus.ONLINE);
    }

    /**
     * Checks whether the status of the thing is offline
     *
     * @param thing
     * @return true if thing status is offline, false otherwise
     */
    public static boolean isOffline(Thing thing) {
        return thing.getStatus().equals(ThingStatus.OFFLINE);
    }

    /**
     * Slice the source array using the range(from, to)
     *
     * @param source the byte[] array to slice
     * @param from the starting point
     * @param to the end point
     * @return the sliced part of the byte array
     * @throws IllegalArgumentException if the slice is not possible
     */
    public static byte[] slice(byte source[], int from, int to) throws IllegalArgumentException {
        if (from > to) {
            throw new IllegalArgumentException("Can't slice; from: " + from + " is larger than to: " + to);
        }
        if (to - from > source.length) {
            throw new IllegalArgumentException(
                    "Can't slice; from: " + from + " - to: " + to + " is longer than source length: " + source.length);
        }
        if (to == from) {
            byte sliced[] = new byte[1];
            sliced[0] = source[from];
            return sliced;
        } else {
            byte sliced[] = new byte[to - from];
            System.arraycopy(source, from, sliced, 0, to - from);
            return sliced;
        }
    }

    /**
     * Pad the source byte[] based on the quotient
     *
     * @param source the byte[] to pad
     * @param quotient the quotient / part to pad
     * @return the padded byte[]
     */
    public static byte[] padTo(byte[] source, int quotient) {
        int modulo = source.length % quotient;
        if (modulo == 0) {
            return source;
        }

        int requiredNewSize = source.length + (quotient - modulo);
        byte[] padded = new byte[requiredNewSize];
        System.arraycopy(source, 0, padded, 0, source.length);

        return padded;
    }

    /**
     * Convert the source byte[] tp a hex string
     *
     * @param source the byte[] to convert
     * @return a string with a hex representation of the source
     */
    public static String toHexString(byte[] source) {
        StringBuilder stringBuilder = new StringBuilder(source.length * 2);
        for (byte b : source) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString();
    }

    /**
     * Encrypt the dat[] using the key[] with the ivSpec AES algorithm parameter
     *
     * @param key the key to use for the AES encryption
     * @param ivSpec the parameter for the AES encryption
     * @param data the byte[] to encrypt
     * @return the encrypted data[]
     * @throws IOException if the encryption has an issue
     */
    public static byte[] encrypt(byte key[], IvParameterSpec ivSpec, byte data[]) throws IOException {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(1, secretKey, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Decrypt the data byte[] using the supplied key and AES algorithm parameter ivSpec
     *
     * @param key the key to use for the AES decrypt
     * @param ivSpec the AES algorithm parameter to use
     * @param data the data to decrypt
     * @return the decrypted byte[]
     * @throws IOException
     */
    public static byte[] decrypt(byte key[], IvParameterSpec ivSpec, byte data[]) throws IOException {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(2, secretKey, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
