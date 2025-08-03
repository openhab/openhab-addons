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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.zip.CRC32;

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
public final class ProtocolUtils {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolUtils.class);

    private static final String MD5_ALGORITHM = "MD5";
    private static final String AES_ECB_PADDING = "AES/ECB/PKCS5Padding";
    private static final String AES_CBC_NO_PADDING = "AES/CBC/NoPadding";
    private static final String VERSION_1_0 = "1.0";
    private static final int HEADER_LENGTH_WITHOUT_CRC = 19; // 3 (version) + 4 (seq) + 4 (random) + 4 (timestamp) + 2
                                                             // (protocol) + 2 (payloadLen)
    private static final int CRC_LENGTH = 4;

    private ProtocolUtils() {
        // Prevent instantiation of util class
    }

    public static String md5Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            byte[] hashBytes = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error("MD5 algorithm not found, this should not happen.", e);
            return "";
        }
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     *
     * @param bytes The byte array to convert.
     * @return The hexadecimal string.
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] decrypt(byte[] payload, String key) throws RoborockCryptoException {
        try {
            byte[] aesKeyBytes = md5bin(key);
            Cipher cipher = Cipher.getInstance(AES_ECB_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(payload);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new RoborockCryptoException("Failed to decrypt data using AES/ECB/PKCS5Padding.", e);
        }
    }

    public static byte[] encrypt(byte[] payload, String key) throws RoborockCryptoException {
        try {
            byte[] aesKeyBytes = md5bin(key);
            Cipher cipher = Cipher.getInstance(AES_ECB_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(payload);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new RoborockCryptoException("Failed to encrypt data using AES/ECB/PKCS5Padding.", e);
        }
    }

    private static byte[] md5bin(String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
        return md.digest(key.getBytes(StandardCharsets.UTF_8));
    }

    private static String bytesToString(byte[] data, int start, int length) {
        return new String(data, start, length, StandardCharsets.UTF_8);
    }

    /**
     * Reads a 32-bit big-endian integer from a byte array at a given starting position.
     *
     * @param data The byte array.
     * @param start The starting index.
     * @return The integer value.
     */
    public static int readInt32BE(byte[] data, int start) {
        return ByteBuffer.wrap(data, start, 4).getInt();
    }

    /**
     * Reads a 16-bit big-endian integer from a byte array at a given starting position.
     *
     * @param data The byte array.
     * @param start The starting index.
     * @return The short integer value.
     */
    public static int readInt16BE(byte[] data, int start) {
        return ByteBuffer.wrap(data, start, 2).getShort() & 0xFFFF;
    }

    /**
     * Writes a 32-bit big-endian integer into a byte array at a given starting position.
     *
     * @param msg The byte array to write into.
     * @param value The integer value to write.
     * @param start The starting index.
     */
    public static void writeInt32BE(byte[] msg, int value, int start) {
        ByteBuffer.wrap(msg, start, 4).putInt(value);
    }

    /**
     * Writes a 16-bit big-endian integer into a byte array at a given starting position.
     *
     * @param msg The byte array to write into.
     * @param value The integer value to write.
     * @param start The starting index.
     */
    public static void writeInt16BE(byte[] msg, int value, int start) {
        ByteBuffer.wrap(msg, start, 2).putShort((short) value);
    }

    /**
     * Encodes a timestamp into a specific hexadecimal string format by rearranging its characters.
     * This is a very specific encoding unique to Roborock's protocol.
     *
     * @param timestamp The timestamp to encode.
     * @return The encoded hexadecimal string.
     */
    public static String encodeTimestamp(int timestamp) {
        // Convert the timestamp to a hexadecimal string and pad it to ensure it's at least 8 characters
        String hex = String.format("%08x", timestamp);

        // Define the order in which to rearrange the hexadecimal characters
        int[] order = { 5, 6, 3, 7, 1, 2, 0, 4 };
        StringBuilder result = new StringBuilder();
        for (int index : order) {
            result.append(hex.charAt(index));
        }
        return result.toString();
    }

    /**
     * Decrypts a ciphertext using AES/CBC/NoPadding.
     * The IV is assumed to be an array of zeros with a length equal to AES_BLOCK_SIZE.
     *
     * @param ciphertext The data to decrypt.
     * @param nonce The key for decryption.
     * @return The decrypted byte array, or an empty array on failure.
     */
    public static byte[] decryptCbc(byte[] ciphertext, byte[] nonce) {
        if (ciphertext.length == 0 || nonce.length == 0) {
            logger.warn("Attempted to decrypt CBC with null or empty ciphertext/nonce.");
            return new byte[0];
        }

        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_NO_PADDING);
            Key secretKey = new SecretKeySpec(nonce, "AES");

            byte[] ivBytes = new byte[AES_BLOCK_SIZE];
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            // This assumes the incoming ciphertext is padded to AES_BLOCK_SIZE
            byte[] decryptedBytes = cipher.doFinal(ciphertext);
            return decryptedBytes;
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

    /**
     * Parses the common header fields from a Roborock message.
     *
     * @param message The full message byte array.
     * @return A {@link MessageHeader} object containing parsed header details.
     */
    private static MessageHeader parseMessageHeader(byte[] message) {
        String version = bytesToString(message, 0, 3);
        int sequence = readInt32BE(message, 3);
        int random = readInt32BE(message, 7);
        int timestamp = readInt32BE(message, 11);
        int protocol = readInt16BE(message, 15);
        int payloadLen = readInt16BE(message, 17);

        return new MessageHeader(version, sequence, random, timestamp, protocol, payloadLen);
    }

    /**
     * Validates the CRC32 checksum of the message.
     *
     * @param message The full message byte array.
     * @param expectedCrc32 The expected CRC32 value from the message.
     * @return True if the CRC32 matches, false otherwise.
     */
    private static boolean validateCrc32(byte[] message, int expectedCrc32) {
        CRC32 crc32 = new CRC32();
        crc32.update(message, 0, message.length - CRC_LENGTH);
        if (crc32.getValue() != expectedCrc32) {
            logger.debug("CRC32 mismatch. Calculated: {}, Expected: {}", crc32.getValue(), expectedCrc32);
            return false;
        }
        return true;
    }

    /**
     * Handles messages with protocol 301 (images).
     * This method is currently a placeholder as image handling is not fully implemented.
     *
     * @param message The full message byte array.
     * @param header The parsed message header.
     * @param nonce The nonce for decryption.
     * @return An empty string, indicating no string result for image protocol.
     */
    private static String handleImageProtocol(byte[] message, MessageHeader header, byte[] nonce) {
        logger.debug("Protocol 301 (image) received, not handled yet.");
        return "";
    }

    /**
     * Handles messages with protocol 102 (data payload).
     * Decrypts the payload and returns the result as a UTF-8 string.
     *
     * @param message The full message byte array.e
     * @param header The parsed message header.
     * @param localKey The local key for decryption.
     * @return The decrypted payload as a string, or an empty string on decryption failure.
     */
    private static String handleDataProtocol(byte[] message, MessageHeader header, String localKey) {
        int payloadStart = HEADER_LENGTH_WITHOUT_CRC;
        int payloadEnd = payloadStart + header.payloadLen;

        if (payloadEnd > message.length - CRC_LENGTH) { // Payload should not extend into CRC area
            logger.warn("Payload length ({}) exceeds message bounds for protocol 102. Message length: {}",
                    header.payloadLen, message.length);
            return "";
        }

        byte[] payload = Arrays.copyOfRange(message, payloadStart, payloadEnd);

        logger.trace(
                "Parsed message version: {}, sequence: {}, random: {}, timestamp: {}, protocol: {}, payloadLen: {}",
                header.version, header.sequence, header.random, header.timestamp, header.protocol, header.payloadLen);

        String encryptionKey = encodeTimestamp(header.timestamp) + localKey + SALT;
        try {
            byte[] decryptedResult = decrypt(payload, encryptionKey);
            return new String(decryptedResult, StandardCharsets.UTF_8);
        } catch (RoborockCryptoException e) {
            logger.debug("Exception decrypting payload for protocol 102: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Handles an incoming Roborock message, parses its header, validates CRC32,
     * and processes the payload based on the protocol.
     *
     * @param message The full message byte array received from the device.
     * @param localKey The local key associated with the device for decryption.
     * @param nonce The nonce used for certain decryption operations (e.g., CBC).
     * @return The decoded string content of the message, or an empty string if parsing/decryption fails
     *         or the protocol is not handled.
     */
    public static String handleMessage(byte[] message, String localKey, byte[] nonce) {
        if (message.length < HEADER_LENGTH_WITHOUT_CRC + CRC_LENGTH) {
            logger.warn("Invalid message length received. Minimum required: {}",
                    HEADER_LENGTH_WITHOUT_CRC + CRC_LENGTH);
            return "";
        }

        MessageHeader header = parseMessageHeader(message);
        if (!VERSION_1_0.equals(header.version)) {
            logger.debug("Received message version is not 1.0: {}", header.version);
            // Depending on protocol evolution, this might require a different handling path
            return "";
        }

        int messageCrc32 = readInt32BE(message, message.length - CRC_LENGTH);
        if (!validateCrc32(message, messageCrc32)) {
            // logger.warn("Message CRC32 checksum mismatch. Message discarded.");
            // return "";
        }

        switch (header.protocol) {
            case 301:
                return handleImageProtocol(message, header, nonce);
            case 102:
                return handleDataProtocol(message, header, localKey);
            default:
                logger.debug("Unknown protocol received: {}", header.protocol);
                return "";
        }
    }

    /**
     * Helper record to encapsulate parsed message header fields.
     */
    private record MessageHeader(String version, int sequence, int random, int timestamp, int protocol,
            int payloadLen) {
    }

    /**
     * Custom exception for cryptographic errors to provide more specific error handling.
     */
    public static class RoborockCryptoException extends Exception {
        private static final long serialVersionUID = 529232811860854017L;

        public RoborockCryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
