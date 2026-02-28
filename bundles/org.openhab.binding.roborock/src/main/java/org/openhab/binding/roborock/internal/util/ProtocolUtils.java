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
package org.openhab.binding.roborock.internal.util;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.zip.CRC32;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.roborock.internal.RoborockException;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public final class ProtocolUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolUtils.class);

    private static byte[] md5bin(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            return md.digest(key.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("MD5 algorithm not found, this should not happen.", e);
            return new byte[0];
        }
    }

    public static String md5Hex(String key) {
        return HexFormat.of().formatHex(md5bin(key));
    }

    public static byte[] decrypt(byte[] payload, String key) throws RoborockException {
        try {
            byte[] aesKeyBytes = md5bin(key);
            Cipher cipher = Cipher.getInstance(AES_ECB_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(payload);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new RoborockException("Failed to decrypt data using AES/ECB/PKCS5Padding.", e);
        }
    }

    public static byte[] encrypt(byte[] payload, String key) throws RoborockException {
        try {
            byte[] aesKeyBytes = md5bin(key);
            Cipher cipher = Cipher.getInstance(AES_ECB_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(payload);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new RoborockException("Failed to encrypt data using AES/ECB/PKCS5Padding.", e);
        }
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
        return !(crc32.getValue() != (expectedCrc32 & 0xFFFFFFFFL));
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
        LOGGER.debug("Protocol 301 (image) received, not handled yet.");
        return "";
    }

    /**
     * Handles messages with protocol 102 (data payload).
     * Decrypts the payload and returns the result as a UTF-8 string.
     *
     * @param message The full message byte array.
     * @param header The parsed message header.
     * @param localKey The local key for decryption.
     * @return The decrypted payload as a string, or an empty string on decryption failure.
     */
    private static String handleDataProtocol(byte[] message, MessageHeader header, String localKey) {
        int payloadStart = HEADER_LENGTH_WITHOUT_CRC;
        int payloadEnd = payloadStart + header.payloadLen;

        if (payloadEnd > message.length - CRC_LENGTH) {
            return "";
        }

        byte[] payload = Arrays.copyOfRange(message, payloadStart, payloadEnd);

        String encryptionKey = encodeTimestamp(header.timestamp) + localKey + SALT;
        try {
            byte[] decryptedResult = decrypt(payload, encryptionKey);
            return new String(decryptedResult, StandardCharsets.UTF_8);
        } catch (RoborockException e) {
            LOGGER.debug("Exception decrypting payload for protocol 102: {}", e.getMessage(), e);
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
            return "";
        }

        MessageHeader header = parseMessageHeader(message);
        if (!VERSION_1_0.equals(header.version)) {
            LOGGER.debug("Received message version is not 1.0: {}", header.version);
            return "";
        }

        int messageCrc32 = readInt32BE(message, message.length - CRC_LENGTH);
        if (!validateCrc32(message, messageCrc32)) {
            LOGGER.warn("Message CRC32 checksum mismatch. Message discarded.");
            return "";
        }

        switch (header.protocol) {
            case 301:
                return handleImageProtocol(message, header, nonce);
            case 102:
                return handleDataProtocol(message, header, localKey);
            default:
                LOGGER.debug("Unknown protocol received: {}", header.protocol);
                return "";
        }
    }

    /**
     * Helper record to encapsulate parsed message header fields.
     */
    private record MessageHeader(String version, int sequence, int random, int timestamp, int protocol,
            int payloadLen) {
    }

    public static String getEndpoint(Rriot rriot) {
        try {
            byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(rriot.k.getBytes());
            byte[] subArray = Arrays.copyOfRange(md5Bytes, 8, 14);
            return Base64.getEncoder().encodeToString(subArray);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
