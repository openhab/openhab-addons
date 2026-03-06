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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

class ProtocolUtilsMapDecodeTest {

    @Test
    void decodeMapResponseReturnsRequestIdAndPayload() throws Exception {
        byte[] nonce = new byte[AES_BLOCK_SIZE];
        Arrays.fill(nonce, (byte) 0x22);
        byte[] decompressedPayload = "rr-map-payload".getBytes(StandardCharsets.UTF_8);
        byte[] compressed = gzip(decompressedPayload);
        byte[] encryptedBody = encryptCbcWithZeroIv(compressed, nonce);

        byte[] transportHeader = new byte[MAP_TRANSPORT_HEADER_LENGTH];
        byte[] endpoint = "ABCDEF==".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(endpoint, 0, transportHeader, 0, endpoint.length);
        transportHeader[MAP_REQUEST_ID_OFFSET] = 0x2A;
        transportHeader[MAP_REQUEST_ID_OFFSET + 1] = 0x00;

        byte[] payload = new byte[transportHeader.length + encryptedBody.length];
        System.arraycopy(transportHeader, 0, payload, 0, transportHeader.length);
        System.arraycopy(encryptedBody, 0, payload, transportHeader.length, encryptedBody.length);

        byte[] message = buildMessage(PROTOCOL_MAP, 12345, payload, "unused");
        ProtocolUtils.DecodedMessage decoded = ProtocolUtils.decodeMessage(message, "unused", nonce, "ABCDEF==");

        ProtocolUtils.MapPayloadResponse map = assertInstanceOf(ProtocolUtils.MapPayloadResponse.class, decoded);
        assertEquals(42, map.requestId());
        assertArrayEquals(decompressedPayload, map.payload());
    }

    @Test
    void decodeMapResponseContinuesOnEndpointMismatch() throws Exception {
        byte[] nonce = new byte[AES_BLOCK_SIZE];
        Arrays.fill(nonce, (byte) 0x11);

        byte[] decompressedPayload = "x".getBytes(StandardCharsets.UTF_8);
        byte[] compressed = gzip(decompressedPayload);
        byte[] encryptedBody = encryptCbcWithZeroIv(compressed, nonce);

        byte[] transportHeader = new byte[MAP_TRANSPORT_HEADER_LENGTH];
        byte[] endpoint = "ZZZZZZ==".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(endpoint, 0, transportHeader, 0, endpoint.length);
        transportHeader[MAP_REQUEST_ID_OFFSET] = 0x2A;
        transportHeader[MAP_REQUEST_ID_OFFSET + 1] = 0x00;

        byte[] payload = new byte[transportHeader.length + encryptedBody.length];
        System.arraycopy(transportHeader, 0, payload, 0, transportHeader.length);
        System.arraycopy(encryptedBody, 0, payload, transportHeader.length, encryptedBody.length);

        byte[] message = buildMessage(PROTOCOL_MAP, 222, payload, "unused");
        ProtocolUtils.DecodedMessage decoded = ProtocolUtils.decodeMessage(message, "unused", nonce, "ABCDEF==");

        ProtocolUtils.MapPayloadResponse map = assertInstanceOf(ProtocolUtils.MapPayloadResponse.class, decoded);
        assertEquals(42, map.requestId());
        assertArrayEquals(decompressedPayload, map.payload());
    }

    @Test
    void decodeMapResponseIgnoresShortPayload() {
        byte[] nonce = new byte[AES_BLOCK_SIZE];
        byte[] message = buildMessage(PROTOCOL_MAP, 111, new byte[8], "unused");
        ProtocolUtils.DecodedMessage decoded = ProtocolUtils.decodeMessage(message, "unused", nonce, "ABCDEF==");
        assertTrue(decoded instanceof ProtocolUtils.IgnoredResponse);
    }

    @Test
    void decodeMapResponseRejectsBadCrc() {
        byte[] nonce = new byte[AES_BLOCK_SIZE];
        byte[] message = buildMessage(PROTOCOL_MAP, 111, new byte[MAP_TRANSPORT_HEADER_LENGTH], "unused");
        message[message.length - 1] ^= 0x7F;
        ProtocolUtils.DecodedMessage decoded = ProtocolUtils.decodeMessage(message, "unused", nonce, "ABCDEF==");
        assertTrue(decoded instanceof ProtocolUtils.IgnoredResponse);
    }

    @Test
    void decodeMapResponseIgnoresDecryptFailure() {
        byte[] nonce = new byte[AES_BLOCK_SIZE];
        Arrays.fill(nonce, (byte) 0x33);

        byte[] transportHeader = new byte[MAP_TRANSPORT_HEADER_LENGTH];
        byte[] endpoint = "ABCDEF==".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(endpoint, 0, transportHeader, 0, endpoint.length);
        transportHeader[MAP_REQUEST_ID_OFFSET] = 0x10;
        transportHeader[MAP_REQUEST_ID_OFFSET + 1] = 0x00;

        byte[] badEncryptedBody = new byte[17];
        byte[] payload = new byte[transportHeader.length + badEncryptedBody.length];
        System.arraycopy(transportHeader, 0, payload, 0, transportHeader.length);
        System.arraycopy(badEncryptedBody, 0, payload, transportHeader.length, badEncryptedBody.length);

        byte[] message = buildMessage(PROTOCOL_MAP, 333, payload, "unused");
        ProtocolUtils.DecodedMessage decoded = ProtocolUtils.decodeMessage(message, "unused", nonce, "ABCDEF==");
        assertTrue(decoded instanceof ProtocolUtils.IgnoredResponse);
    }

    @Test
    void decodeMapResponseIgnoresDecompressFailure() throws Exception {
        byte[] nonce = new byte[AES_BLOCK_SIZE];
        Arrays.fill(nonce, (byte) 0x44);

        byte[] notGzipPayload = "not-gzip-data".getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBody = encryptCbcWithZeroIv(notGzipPayload, nonce);

        byte[] transportHeader = new byte[MAP_TRANSPORT_HEADER_LENGTH];
        byte[] endpoint = "ABCDEF==".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(endpoint, 0, transportHeader, 0, endpoint.length);
        transportHeader[MAP_REQUEST_ID_OFFSET] = 0x34;
        transportHeader[MAP_REQUEST_ID_OFFSET + 1] = 0x12;

        byte[] payload = new byte[transportHeader.length + encryptedBody.length];
        System.arraycopy(transportHeader, 0, payload, 0, transportHeader.length);
        System.arraycopy(encryptedBody, 0, payload, transportHeader.length, encryptedBody.length);

        byte[] message = buildMessage(PROTOCOL_MAP, 444, payload, "unused");
        ProtocolUtils.DecodedMessage decoded = ProtocolUtils.decodeMessage(message, "unused", nonce, "ABCDEF==");
        assertTrue(decoded instanceof ProtocolUtils.IgnoredResponse);
    }

    private static byte[] gzip(byte[] data) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(data);
            gzip.finish();
            return output.toByteArray();
        }
    }

    private static byte[] encryptCbcWithZeroIv(byte[] plaintext, byte[] key) throws Exception {
        int padLen = AES_BLOCK_SIZE - (plaintext.length % AES_BLOCK_SIZE);
        if (padLen == 0) {
            padLen = AES_BLOCK_SIZE;
        }

        byte[] padded = Arrays.copyOf(plaintext, plaintext.length + padLen);
        Arrays.fill(padded, plaintext.length, padded.length, (byte) padLen);

        Cipher cipher = Cipher.getInstance(AES_CBC_NO_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(new byte[AES_BLOCK_SIZE]));
        return cipher.doFinal(padded);
    }

    private static byte[] buildMessage(int protocol, int timestamp, byte[] payload, String localKey) {
        byte[] transportPayload = payload;
        if (protocol == PROTOCOL_MAP) {
            try {
                String key = ProtocolUtils.encodeTimestamp(timestamp) + localKey + SALT;
                transportPayload = ProtocolUtils.encrypt(payload, key);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to build encrypted protocol 301 payload", e);
            }
        }

        int totalLength = HEADER_LENGTH_WITHOUT_CRC + transportPayload.length + CRC_LENGTH;
        byte[] message = new byte[totalLength];
        message[0] = '1';
        message[1] = '.';
        message[2] = '0';

        ProtocolUtils.writeInt32BE(message, 100001, SEQ_OFFSET);
        ProtocolUtils.writeInt32BE(message, 54321, RANDOM_OFFSET);
        ProtocolUtils.writeInt32BE(message, timestamp, TIMESTAMP_OFFSET);
        ProtocolUtils.writeInt16BE(message, protocol, PROTOCOL_OFFSET);
        ProtocolUtils.writeInt16BE(message, transportPayload.length, PAYLOAD_OFFSET);
        System.arraycopy(transportPayload, 0, message, HEADER_LENGTH_WITHOUT_CRC, transportPayload.length);

        CRC32 crc32 = new CRC32();
        crc32.update(message, 0, message.length - CRC_LENGTH);
        ProtocolUtils.writeInt32BE(message, (int) crc32.getValue(), message.length - CRC_LENGTH);
        return message;
    }
}
