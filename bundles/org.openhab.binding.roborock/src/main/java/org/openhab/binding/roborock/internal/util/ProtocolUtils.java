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
package org.openhab.binding.roborock.internal.util;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class ProtocolUtils {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolUtils.class);

    private ProtocolUtils() {
        // Prevent instantiation of util class
    }

    public static String md5Hex(String data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // should never occur
            return "";
        }
        byte[] array = md.digest(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] decrypt(byte[] payload, String key) throws Exception {
        byte[] aesKeyBytes = md5bin(key);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(payload);
    }

    public static byte[] encrypt(byte[] payload, String key) throws Exception {
        byte[] aesKeyBytes = md5bin(key);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(payload);
    }

    private static byte[] md5bin(String key) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(key.getBytes("UTF-8"));
    }

    private static String bytesToString(byte[] data, int start, int length) {
        return new String(data, start, length, StandardCharsets.UTF_8);
    }

    public static int readInt32BE(byte[] data, int start) {
        return (((data[start] & 0xFF) << 24) | ((data[start + 1] & 0xFF) << 16) | ((data[start + 2] & 0xFF) << 8)
                | (data[start + 3] & 0xFF));
    }

    public static int readInt16BE(byte[] data, int start) {
        return (((data[start] & 0xFF) << 8) | (data[start + 1] & 0xFF));
    }

    public static void writeInt32BE(byte[] msg, int value, int start) {
        msg[start + 0] = (byte) ((value >> 24) & 0xFF);
        msg[start + 1] = (byte) ((value >> 16) & 0xFF);
        msg[start + 2] = (byte) ((value >> 8) & 0xFF);
        msg[start + 3] = (byte) (value & 0xFF);
    }

    public static void writeInt16BE(byte[] msg, int value, int start) {
        msg[start + 0] = (byte) ((value >> 8) & 0xFF);
        msg[start + 1] = (byte) (value & 0xFF);
    }

    public static String encodeTimestamp(int timestamp) {
        // Convert the timestamp to a hexadecimal string and pad it to ensure it's at least 8 characters
        String hex = new BigInteger(Long.toString(timestamp)).toString(16);
        hex = String.format("%8s", hex).replace(' ', '0');
        List<String> hexChars = new ArrayList<>();
        for (char c : hex.toCharArray()) {
            hexChars.add(String.valueOf(c));
        }
        // Define the order in which to rearrange the hexadecimal characters
        int[] order = { 5, 6, 3, 7, 1, 2, 0, 4 };
        StringBuilder result = new StringBuilder();
        for (int index : order) {
            result.append(hexChars.get(index));
        }
        return result.toString();
    }

    public static byte[] decryptCbc(byte[] ciphertext, byte[] nonce) {
        // Equivalent of Python's `Utils.verify_token(token)`
        // verifyToken(token); // todo

        try {
            // "AES/CBC/PKCS5Padding" specifies AES algorithm, CBC mode, and PKCS5 padding.
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Key secretKey = new SecretKeySpec(nonce, "AES");

            // Python's `iv = bytes(AES.block_size)` creates an IV of all zeros.
            // In Java, this is represented by a `new byte[AES_BLOCK_SIZE]` and wrapped in `IvParameterSpec`.
            byte[] ivBytes = new byte[AES_BLOCK_SIZE]; // All zeros by default
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            // `doFinal` handles both decryption and unpadding automatically due to "PKCS5Padding".
            return cipher.doFinal(ciphertext);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            logger.error("Error initializing cipher. Check algorithm and padding.", e);
        } catch (InvalidKeyException e) {
            logger.error("Invalid decryption key (token).", e);
        } catch (InvalidAlgorithmParameterException e) {
            logger.error("Invalid algorithm parameter (IV).", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            // BadPaddingException often indicates incorrect key, IV, or corrupted ciphertext.
            logger.error("Error during decryption or invalid ciphertext. Check key, IV, and data integrity.", e);
        }
        return new byte[0];
    }

    private static byte[] decompress(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return new byte[0]; // Return an empty array for null or empty input
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(bis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } finally {
            // Close streams to release resources
            if (gis != null) {
                try {
                    gis.close();
                } catch (IOException e) {
                    // Log or handle the exception if closing fails
                }
            }
            try {
                bis.close();
            } catch (IOException e) {
                // Log or handle the exception if closing fails
            }
            try {
                bos.close();
            } catch (IOException e) {
                // Log or handle the exception if closing fails
            }
        }
    }

    public static String handleMessage(byte[] message, String localKey, byte[] nonce) {
        String version = bytesToString(message, 0, 3);
        // Do some checks
        if (!"1.0".equals(version)) {// && version!="A01") {
            logger.debug("Parse was not version 1.0 as expected:{}", version);
            return "";
        }
        byte[] buf = Arrays.copyOfRange(message, 0, message.length - 4);
        CRC32 crc32 = new CRC32();
        crc32.update(buf);

        int expectedCrc32 = readInt32BE(message, message.length - 4);
        if (crc32.getValue() != expectedCrc32) {
            logger.debug("message was not crc32 {} as expected {}", crc32.getValue(), expectedCrc32);
        }
        int sequence = readInt32BE(message, 3);
        int random = readInt32BE(message, 7);
        int timestamp = readInt32BE(message, 11);
        int protocol = readInt16BE(message, 15);
        if (protocol == 301) {
            logger.debug("we don't handle images yet");
            byte[] payload = Arrays.copyOfRange(message, 0, 24);
            String endpoint = new String(Arrays.copyOfRange(payload, 0, 8)).trim();
            String unusedString = new String(Arrays.copyOfRange(payload, 8, 16)).trim();
            int requestId = ByteBuffer.wrap(Arrays.copyOfRange(payload, 16, 22)).getShort();
            String anotherUnusedString = new String(Arrays.copyOfRange(payload, 22, 24)).trim();
            byte[] decrypted;
            /*
             * decrypted = decryptCbc(Arrays.copyOfRange(payload, 24, payload.length), nonce);
             * byte[] decompressed;
             * try {
             * decompressed = decompress(decrypted);
             * } catch (IOException e) {
             * logger.debug("Exception decompressing payload, {}", e.getMessage());
             * }
             */
            return "";
        } else if (protocol == 102) {
            int payloadLen = readInt16BE(message, 17);
            byte[] payload = Arrays.copyOfRange(message, 19, 19 + payloadLen);
            logger.trace(
                    "parsed message version: {}, sequence: {}, random: {}, timestamp: {}, protocol: {}, payloadLen: {}",
                    version, sequence, random, timestamp, protocol, payloadLen);
            String key = encodeTimestamp(timestamp) + localKey + SALT;
            try {
                byte[] result = decrypt(payload, key);
                String stringResult = new String(result, StandardCharsets.UTF_8);
                return stringResult;
            } catch (Exception e) {
                logger.debug("Exception decrypting payload, {}", e.getMessage());
                return "";
            }
        } else {
            logger.debug("Unknown protocol {}", protocol);
            return "";
        }
    }
}
