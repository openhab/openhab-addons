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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
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

    public sealed interface DecodedMessage permits JsonPayloadResponse, MapPayloadResponse, IgnoredResponse {
    }

    public record JsonPayloadResponse(String payload) implements DecodedMessage {
    }

    public record MapPayloadResponse(int requestId, byte[] payload) implements DecodedMessage {
    }

    public record IgnoredResponse() implements DecodedMessage {
    }

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
     * Handles messages with protocol 301 (map/image payload).
     * Decrypts the transport payload, validates and parses the map transport header,
     * then decrypts, unpads, and decompresses the map body.
     *
     * @param message The full message byte array.
     * @param header The parsed message header.
     * @param localKey The local key for transport payload decryption.
     * @param nonce The nonce used for AES/CBC decryption of the map body.
     * @param endpointPrefix Optional endpoint prefix for validation/logging.
     * @return A {@link MapPayloadResponse} when decoding succeeds, otherwise {@link IgnoredResponse}.
     */
    private static DecodedMessage handleImageProtocol(byte[] message, MessageHeader header, String localKey,
            byte[] nonce, String endpointPrefix) {
        int payloadStart = HEADER_LENGTH_WITHOUT_CRC;
        int payloadEnd = payloadStart + header.payloadLen;
        if (payloadEnd > message.length - CRC_LENGTH) {
            LOGGER.debug("Protocol {} payload boundaries are invalid. payloadLen={}", PROTOCOL_MAP, header.payloadLen);
            return new IgnoredResponse();
        }

        byte[] payload = Arrays.copyOfRange(message, payloadStart, payloadEnd);
        byte[] decryptedTransportPayload;
        String encryptionKey = encodeTimestamp(header.timestamp) + localKey + SALT;
        try {
            decryptedTransportPayload = decrypt(payload, encryptionKey);
        } catch (RoborockException e) {
            LOGGER.debug("Failed to decrypt protocol {} transport payload: {}", PROTOCOL_MAP, e.getMessage());
            return new IgnoredResponse();
        }

        payload = decryptedTransportPayload;
        if (payload.length < MAP_TRANSPORT_HEADER_LENGTH) {
            LOGGER.debug("Protocol {} payload too short: {}", PROTOCOL_MAP, payload.length);
            return new IgnoredResponse();
        }

        byte[] endpointBytes = Arrays.copyOfRange(payload, 0, MAP_ENDPOINT_LENGTH);
        byte[] reservedBytes = Arrays.copyOfRange(payload, MAP_ENDPOINT_LENGTH,
                MAP_ENDPOINT_LENGTH + MAP_RESERVED_LENGTH);
        int requestId = readInt16LE(payload, MAP_REQUEST_ID_OFFSET);
        byte[] tailBytes = Arrays.copyOfRange(payload, MAP_REQUEST_ID_OFFSET + 2,
                MAP_REQUEST_ID_OFFSET + 2 + MAP_TAIL_LENGTH);
        String endpoint = new String(endpointBytes, StandardCharsets.UTF_8).replace("\0", "");
        if (!endpointPrefix.isEmpty() && !endpoint.startsWith(endpointPrefix)) {
            LOGGER.debug(
                    "Protocol {} endpoint mismatch for requestId={}. expectedPrefix='{}', actualEndpoint='{}'. Continuing decode.",
                    PROTOCOL_MAP, requestId, endpointPrefix, endpoint);
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Decoded map transport header. endpoint='{}' reserved={} tail={}", endpoint,
                    reservedBytes.length, tailBytes.length);
        }

        byte[] encryptedBody = Arrays.copyOfRange(payload, MAP_TRANSPORT_HEADER_LENGTH, payload.length);

        try {
            byte[] decryptedBody = decryptCbcNoPadding(encryptedBody, nonce);
            byte[] unpaddedBody = unpadPkcs7(decryptedBody);
            byte[] decompressed = decompressGzip(unpaddedBody);
            return new MapPayloadResponse(requestId, decompressed);
        } catch (RoborockException e) {
            LOGGER.debug("Failed to decode protocol {} map payload: {}", PROTOCOL_MAP, e.getMessage());
            return new IgnoredResponse();
        }
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
        DecodedMessage decoded = decodeMessage(message, localKey, nonce, "");
        if (decoded instanceof JsonPayloadResponse jsonPayloadResponse) {
            return jsonPayloadResponse.payload();
        }
        return "";
    }

    public static DecodedMessage decodeMessage(byte[] message, String localKey, byte[] nonce, String endpointPrefix) {
        if (message.length < HEADER_LENGTH_WITHOUT_CRC + CRC_LENGTH) {
            return new IgnoredResponse();
        }

        MessageHeader header = parseMessageHeader(message);
        if (!VERSION_1_0.equals(header.version)) {
            LOGGER.debug("Received message version is not 1.0: {}", header.version);
            return new IgnoredResponse();
        }

        int messageCrc32 = readInt32BE(message, message.length - CRC_LENGTH);
        if (!validateCrc32(message, messageCrc32)) {
            LOGGER.warn("Message CRC32 checksum mismatch. Message discarded.");
            return new IgnoredResponse();
        }

        switch (header.protocol) {
            case PROTOCOL_MAP:
                return handleImageProtocol(message, header, localKey, nonce, endpointPrefix);
            case PROTOCOL_JSON:
                String payload = handleDataProtocol(message, header, localKey);
                if (!payload.isEmpty()) {
                    return new JsonPayloadResponse(payload);
                }
                return new IgnoredResponse();
            default:
                LOGGER.debug("Unknown protocol received: {}", header.protocol);
                return new IgnoredResponse();
        }
    }

    /**
     * Helper record to encapsulate parsed message header fields.
     */
    private record MessageHeader(String version, int sequence, int random, int timestamp, int protocol,
            int payloadLen) {
    }

    private static int readInt16LE(byte[] data, int start) {
        return (data[start] & 0xFF) | ((data[start + 1] & 0xFF) << 8);
    }

    private static byte[] decryptCbcNoPadding(byte[] payload, byte[] key) throws RoborockException {
        if (payload.length == 0 || payload.length % AES_BLOCK_SIZE != 0 || key.length != AES_BLOCK_SIZE) {
            throw new RoborockException("Invalid map payload/key size for AES-CBC decryption.");
        }

        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_NO_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(new byte[AES_BLOCK_SIZE]));
            return cipher.doFinal(payload);
        } catch (GeneralSecurityException e) {
            throw new RoborockException("Failed to decrypt map payload with AES-CBC.", e);
        }
    }

    private static byte[] unpadPkcs7(byte[] bytes) throws RoborockException {
        if (bytes.length == 0) {
            throw new RoborockException("Cannot unpad empty payload.");
        }

        int padLength = bytes[bytes.length - 1] & 0xFF;
        if (padLength <= 0 || padLength > AES_BLOCK_SIZE || padLength > bytes.length) {
            throw new RoborockException("Invalid PKCS7 padding in map payload.");
        }

        for (int i = bytes.length - padLength; i < bytes.length; i++) {
            if ((bytes[i] & 0xFF) != padLength) {
                throw new RoborockException("Invalid PKCS7 padding bytes in map payload.");
            }
        }

        return Arrays.copyOf(bytes, bytes.length - padLength);
    }

    private static byte[] decompressGzip(byte[] payload) throws RoborockException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(payload);
                GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = gzipInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RoborockException("Failed to decompress map payload.", e);
        }
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
