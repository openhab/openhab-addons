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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.roborock.internal.RoborockException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@NonNullByDefault({})
class ProtocolUtilsDirectJsonDecodeTest {

    @Test
    void decodeMessageHandlesProtocol4AsJsonPayload() {
        String localKey = "local-key";
        int timestamp = 1_762_101_111;
        String rpc = "{\"id\":73001,\"result\":[{\"enabled\":1}]}";
        String payload = "{\"dps\":{\"102\":" + quoteJsonString(rpc) + "}}";

        byte[] frame = buildEncryptedV10Frame(4, timestamp, localKey, payload.getBytes(StandardCharsets.UTF_8));
        ProtocolUtils.DecodedMessage decoded = ProtocolUtils.decodeMessage(frame, localKey, new byte[16], "ABCDEF==");

        ProtocolUtils.JsonPayloadResponse json = assertInstanceOf(ProtocolUtils.JsonPayloadResponse.class, decoded);
        assertRpcId(json.payload(), 73001);
    }

    @Test
    void decodeMessageHandlesProtocol5AsJsonPayload() {
        String localKey = "local-key";
        int timestamp = 1_762_101_112;
        String rpc = "{\"id\":73002,\"result\":[{\"enabled\":0}]}";
        String payload = "{\"dps\":{\"102\":" + quoteJsonString(rpc) + "}}";

        byte[] frame = buildEncryptedV10Frame(5, timestamp, localKey, payload.getBytes(StandardCharsets.UTF_8));
        ProtocolUtils.DecodedMessage decoded = ProtocolUtils.decodeMessage(frame, localKey, new byte[16], "ABCDEF==");

        ProtocolUtils.JsonPayloadResponse json = assertInstanceOf(ProtocolUtils.JsonPayloadResponse.class, decoded);
        assertRpcId(json.payload(), 73002);
    }

    @Test
    void decodeMessageKeepsUnknownProtocolIgnored() {
        String localKey = "local-key";
        int timestamp = 1_762_101_113;
        byte[] frame = buildEncryptedV10Frame(9, timestamp, localKey,
                "{\"dps\":{\"102\":\"{}\"}}".getBytes(StandardCharsets.UTF_8));

        ProtocolUtils.DecodedMessage decoded = ProtocolUtils.decodeMessage(frame, localKey, new byte[16], "ABCDEF==");
        assertEquals(ProtocolUtils.IgnoredResponse.class, decoded.getClass());
    }

    @Test
    void decodeL01NeedsHandshakeContext() throws Exception {
        String localKey = "abcdef1234567890";
        int connectNonce = 23456;
        int ackNonce = 65432;
        byte[] frame = buildL01Frame(localKey, connectNonce, ackNonce,
                "{\"id\":74001,\"result\":[{\"start_hour\":22,\"enabled\":1}]}");

        ProtocolUtils.DecodedMessage withoutHandshake = ProtocolUtils.decodeMessage(frame, localKey, new byte[16],
                "ABCDEF==");
        assertEquals(ProtocolUtils.IgnoredResponse.class, withoutHandshake.getClass());

        ProtocolUtils.DecodedMessage withHandshake = ProtocolUtils.decodeMessage(frame, localKey, new byte[16],
                "ABCDEF==", connectNonce, ackNonce);
        ProtocolUtils.JsonPayloadResponse json = assertInstanceOf(ProtocolUtils.JsonPayloadResponse.class,
                withHandshake);
        assertRpcId(json.payload(), 74001);
    }

    @Test
    void decodeL01AcceptsNegativeAckNonceWhenHandshakeContextIsAvailable() throws Exception {
        String localKey = "abcdef1234567890";
        int connectNonce = 24567;
        int ackNonce = 0x9ABCDEFF;
        byte[] frame = buildL01Frame(localKey, connectNonce, ackNonce, "{\"id\":74002,\"result\":[{\"enabled\":1}]}");

        ProtocolUtils.DecodedMessage decoded = ProtocolUtils.decodeMessage(frame, localKey, new byte[16], "ABCDEF==",
                connectNonce, ackNonce, true);
        ProtocolUtils.JsonPayloadResponse json = assertInstanceOf(ProtocolUtils.JsonPayloadResponse.class, decoded);
        assertRpcId(json.payload(), 74002);
        assertTrue(ackNonce < 0);
    }

    private static byte[] buildEncryptedV10Frame(int protocol, int timestamp, String localKey,
            byte[] plaintextPayload) {
        try {
            byte[] encryptedPayload = ProtocolUtils.encrypt(plaintextPayload,
                    ProtocolUtils.encodeTimestamp(timestamp) + localKey + SALT);
            byte[] frame = new byte[HEADER_LENGTH_WITHOUT_CRC + encryptedPayload.length + CRC_LENGTH];
            frame[0] = '1';
            frame[1] = '.';
            frame[2] = '0';
            ProtocolUtils.writeInt32BE(frame, 500001, SEQ_OFFSET);
            ProtocolUtils.writeInt32BE(frame, 61111, RANDOM_OFFSET);
            ProtocolUtils.writeInt32BE(frame, timestamp, TIMESTAMP_OFFSET);
            ProtocolUtils.writeInt16BE(frame, protocol, PROTOCOL_OFFSET);
            ProtocolUtils.writeInt16BE(frame, encryptedPayload.length, PAYLOAD_OFFSET);
            System.arraycopy(encryptedPayload, 0, frame, HEADER_LENGTH_WITHOUT_CRC, encryptedPayload.length);

            CRC32 crc32 = new CRC32();
            crc32.update(frame, 0, frame.length - CRC_LENGTH);
            ProtocolUtils.writeInt32BE(frame, (int) crc32.getValue(), frame.length - CRC_LENGTH);
            return frame;
        } catch (RoborockException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] buildL01Frame(String localKey, int connectNonce, int ackNonce, String rpcPayload)
            throws RoborockException {
        int timestamp = (int) (System.currentTimeMillis() / 1000L);
        int sequence = 600101;
        int random = 70101;
        String payloadJson = "{\"dps\":{\"102\":" + quoteJsonString(rpcPayload) + "}}";
        byte[] encrypted = ProtocolUtils.encryptL01(payloadJson.getBytes(StandardCharsets.UTF_8), localKey, timestamp,
                sequence, random, connectNonce, ackNonce);

        byte[] frame = new byte[HEADER_LENGTH_WITHOUT_CRC + encrypted.length + CRC_LENGTH];
        frame[0] = 'L';
        frame[1] = '0';
        frame[2] = '1';
        ProtocolUtils.writeInt32BE(frame, sequence, SEQ_OFFSET);
        ProtocolUtils.writeInt32BE(frame, random, RANDOM_OFFSET);
        ProtocolUtils.writeInt32BE(frame, timestamp, TIMESTAMP_OFFSET);
        ProtocolUtils.writeInt16BE(frame, 5, PROTOCOL_OFFSET);
        ProtocolUtils.writeInt16BE(frame, encrypted.length, PAYLOAD_OFFSET);
        System.arraycopy(encrypted, 0, frame, HEADER_LENGTH_WITHOUT_CRC, encrypted.length);

        CRC32 crc32 = new CRC32();
        crc32.update(frame, 0, frame.length - CRC_LENGTH);
        ProtocolUtils.writeInt32BE(frame, (int) crc32.getValue(), frame.length - CRC_LENGTH);
        return frame;
    }

    private static String quoteJsonString(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 8);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                    break;
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static void assertRpcId(String outerJson, int expectedId) {
        JsonObject outer = JsonParser.parseString(outerJson).getAsJsonObject();
        JsonObject dps = outer.getAsJsonObject("dps");
        String rpc = dps.get("102").getAsString();
        JsonObject rpcJson = JsonParser.parseString(rpc).getAsJsonObject();
        assertEquals(expectedId, rpcJson.get("id").getAsInt(), outerJson);
    }
}
