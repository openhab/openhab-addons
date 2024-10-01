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
package org.openhab.binding.tapocontrol.internal.helpers;

import static java.util.Base64.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class for Encoding, Crypting, and Decrypting
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoEncoder {
    private static final String SHA1_ALGORITHM = "SHA1";
    private static final String SHA256_ALGORITHM = "SHA256";

    /* b64 encode */
    public static String b64Encode(String textToEncode) {
        byte[] message = null;
        message = textToEncode.getBytes(StandardCharsets.UTF_8);
        return getMimeEncoder().encodeToString(message);
    }

    /* b64 decode */
    public static String b64Decode(String textToDecode) {
        byte[] decoded = getMimeDecoder().decode(textToDecode);
        return byteArrayToString(decoded);
    }

    /**
     * Checks if a string is encoded to Base64.
     *
     * @param input The string to be checked.
     * @return Returns true if the string is Base64 encoded, false otherwise.
     */
    public static boolean isBase64Encoded(String input) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(input.getBytes());
            String decodedString = new String(decodedBytes);
            String reencodedString = Base64.getEncoder().encodeToString(decodedString.getBytes());
            return input.equals(reencodedString);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /* create sha1 hash (string) */
    public static String sha1Encode(String textToEncode) throws NoSuchAlgorithmException {
        byte[] digest = sha1Encode(textToEncode.getBytes(StandardCharsets.UTF_8));
        return byteArrayToString(digest);
    }

    /* create sha1 hash (byte[]) */
    public static byte[] sha1Encode(byte[] bArr) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(SHA1_ALGORITHM).digest(bArr);
    }

    /* create sha256 hash (string) */
    public static String sha256Encode(String textToEncode) throws NoSuchAlgorithmException {
        byte[] digest = textToEncode.getBytes(StandardCharsets.UTF_8);
        return byteArrayToString(sha256Encode(digest));
    }

    /* create sha256 hash (byte[])) */
    public static byte[] sha256Encode(byte[] bArr) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(SHA256_ALGORITHM).digest(bArr);
    }

    /* get CRC32-Checksum from byteArray */
    public static long crc32Checksum(byte[] bArr) {
        CRC32 crc32 = new CRC32();
        crc32.update(bArr);
        return crc32.getValue();
    }

    /* convert bytearray[] into string */
    public static String byteArrayToString(byte[] hexByte) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hexByte) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() == 1) {
                sb.append("0");
                sb.append(hexString);
            } else {
                sb.append(hexString);
            }
        }
        return sb.toString();
    }
}
