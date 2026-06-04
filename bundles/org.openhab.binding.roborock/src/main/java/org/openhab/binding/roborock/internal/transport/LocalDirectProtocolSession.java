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
package org.openhab.binding.roborock.internal.transport;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.roborock.internal.RoborockException;
import org.openhab.binding.roborock.internal.util.ProtocolUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Foundation implementation for direct-session message framing.
 *
 * This class builds local direct protocol frames and tracks minimal handshake state required
 * for request/response command exchange.
 *
 * @author Maciej Pham - Initial contribution
 */
@NonNullByDefault
final class LocalDirectProtocolSession {

    private static final int PROTOCOL_HELLO_REQUEST = 0;
    private static final int PROTOCOL_HELLO_RESPONSE = 1;
    private static final int PROTOCOL_PING_REQUEST = 2;
    private static final int PROTOCOL_PING_RESPONSE = 3;
    private static final int PROTOCOL_GENERAL_REQUEST = 4;
    private static final byte[] VERSION_V1 = new byte[] { '1', '.', '0' };
    private static final byte[] VERSION_L01 = new byte[] { 'L', '0', '1' };

    enum LocalProtocolVersion {
        V1,
        L01
    }

    private final Gson gson = new Gson();
    private final SecureRandom secureRandom = new SecureRandom();
    private final AtomicInteger sequenceCounter;
    private final int connectNonce;
    private int ackNonce = -1;
    private boolean handshakeContextAvailable;
    private LocalProtocolVersion negotiatedProtocol = LocalProtocolVersion.V1;

    LocalDirectProtocolSession() {
        this.sequenceCounter = new AtomicInteger(1);
        this.connectNonce = secureRandom.nextInt(22767 + 1) + 10000;
    }

    int getConnectNonce() {
        return connectNonce;
    }

    int getAckNonce() {
        return ackNonce;
    }

    boolean hasHandshakeContext() {
        return handshakeContextAvailable;
    }

    LocalProtocolVersion getNegotiatedProtocol() {
        return negotiatedProtocol;
    }

    byte[] buildHelloRequestFrame() {
        return buildHelloRequestFrame(false, true);
    }

    byte[] buildHelloRequestFrame(boolean l01Version) {
        return buildHelloRequestFrame(l01Version, true);
    }

    byte[] buildHelloRequestFrame(boolean l01Version, boolean prefixed) {
        int timestamp = (int) (System.currentTimeMillis() / 1000L);
        int seq = 1;

        byte[] message = new byte[17];
        byte[] version = l01Version ? VERSION_L01 : VERSION_V1;
        System.arraycopy(version, 0, message, 0, 3);
        ProtocolUtils.writeInt32BE(message, seq, SEQ_OFFSET);
        ProtocolUtils.writeInt32BE(message, connectNonce, RANDOM_OFFSET);
        ProtocolUtils.writeInt32BE(message, timestamp, TIMESTAMP_OFFSET);
        ProtocolUtils.writeInt16BE(message, PROTOCOL_HELLO_REQUEST, PROTOCOL_OFFSET);

        CRC32 crc32 = new CRC32();
        crc32.update(message, 0, message.length);

        byte[] helloFrame = new byte[message.length + CRC_LENGTH];
        System.arraycopy(message, 0, helloFrame, 0, message.length);
        ProtocolUtils.writeInt32BE(helloFrame, (int) crc32.getValue(), message.length);

        return prefixed ? withPrefix(helloFrame) : helloFrame;
    }

    boolean applyHelloResponse(byte[] responseFrame) {
        if (responseFrame.length < 17) {
            return false;
        }
        byte[] versionBytes = Arrays.copyOfRange(responseFrame, 0, 3);
        LocalProtocolVersion detectedVersion;
        if (Arrays.equals(VERSION_V1, versionBytes)) {
            detectedVersion = LocalProtocolVersion.V1;
        } else if (Arrays.equals(VERSION_L01, versionBytes)) {
            detectedVersion = LocalProtocolVersion.L01;
        } else {
            return false;
        }
        int protocol = ProtocolUtils.readInt16BE(responseFrame, PROTOCOL_OFFSET);
        if (protocol != PROTOCOL_HELLO_RESPONSE) {
            return false;
        }
        ackNonce = ProtocolUtils.readInt32BE(responseFrame, RANDOM_OFFSET);
        handshakeContextAvailable = true;
        negotiatedProtocol = detectedVersion;
        return true;
    }

    byte[] buildCommandFrame(String method, String params, int requestId, String localKey) throws RoborockException {
        int timestamp = (int) (System.currentTimeMillis() / 1000L);
        int protocol = PROTOCOL_GENERAL_REQUEST;

        JsonElement paramsElement = JsonParser.parseString(params);
        Map<String, Object> inner = new HashMap<>();
        inner.put("id", requestId);
        inner.put("method", method);
        inner.put("params", paramsElement);

        Map<String, Object> dps = new HashMap<>();
        dps.put("101", gson.toJson(inner));

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("t", timestamp);
        payloadMap.put("dps", dps);

        String payload = gson.toJson(payloadMap);
        return withPrefix(buildFrame(localKey, protocol, timestamp, payload.getBytes(StandardCharsets.UTF_8),
                negotiatedProtocol));
    }

    byte[] buildPingRequestFrame() {
        int timestamp = (int) (System.currentTimeMillis() / 1000L);
        int seq = nextSequence();
        int randomInt = secureRandom.nextInt(90000) + 10000;

        byte[] message = new byte[17];
        byte[] version = negotiatedProtocol == LocalProtocolVersion.L01 ? VERSION_L01 : VERSION_V1;
        System.arraycopy(version, 0, message, 0, 3);
        ProtocolUtils.writeInt32BE(message, seq, SEQ_OFFSET);
        ProtocolUtils.writeInt32BE(message, randomInt, RANDOM_OFFSET);
        ProtocolUtils.writeInt32BE(message, timestamp, TIMESTAMP_OFFSET);
        ProtocolUtils.writeInt16BE(message, PROTOCOL_PING_REQUEST, PROTOCOL_OFFSET);

        CRC32 crc32 = new CRC32();
        crc32.update(message, 0, message.length);

        byte[] pingFrame = new byte[message.length + CRC_LENGTH];
        System.arraycopy(message, 0, pingFrame, 0, message.length);
        ProtocolUtils.writeInt32BE(pingFrame, (int) crc32.getValue(), message.length);
        return withPrefix(pingFrame);
    }

    boolean isPingResponseFrame(byte[] responseFrame) {
        if (responseFrame.length < 17) {
            return false;
        }
        byte[] versionBytes = Arrays.copyOfRange(responseFrame, 0, 3);
        if (!Arrays.equals(VERSION_V1, versionBytes) && !Arrays.equals(VERSION_L01, versionBytes)) {
            return false;
        }
        int protocol = ProtocolUtils.readInt16BE(responseFrame, PROTOCOL_OFFSET);
        return protocol == PROTOCOL_PING_RESPONSE;
    }

    private byte[] buildFrame(String localKey, int protocol, int timestamp, byte[] payload,
            LocalProtocolVersion protocolVersion) throws RoborockException {
        int randomInt = secureRandom.nextInt(90000) + 10000;
        int seq = nextSequence();

        byte[] encryptedPayload;
        if (protocolVersion == LocalProtocolVersion.L01) {
            encryptedPayload = ProtocolUtils.encryptL01(payload, localKey, timestamp, seq, randomInt, connectNonce,
                    ackNonce);
        } else {
            String key = ProtocolUtils.encodeTimestamp(timestamp) + localKey + SALT;
            encryptedPayload = ProtocolUtils.encrypt(payload, key);
        }

        int totalLength = HEADER_LENGTH_WITHOUT_CRC + encryptedPayload.length + CRC_LENGTH;
        byte[] message = new byte[totalLength];

        byte[] versionBytes = protocolVersion == LocalProtocolVersion.L01 ? VERSION_L01 : VERSION_V1;
        message[0] = versionBytes[0];
        message[1] = versionBytes[1];
        message[2] = versionBytes[2];

        ProtocolUtils.writeInt32BE(message, seq, SEQ_OFFSET);
        ProtocolUtils.writeInt32BE(message, randomInt, RANDOM_OFFSET);
        ProtocolUtils.writeInt32BE(message, timestamp, TIMESTAMP_OFFSET);
        ProtocolUtils.writeInt16BE(message, protocol, PROTOCOL_OFFSET);
        ProtocolUtils.writeInt16BE(message, encryptedPayload.length, PAYLOAD_OFFSET);

        System.arraycopy(encryptedPayload, 0, message, HEADER_LENGTH_WITHOUT_CRC, encryptedPayload.length);

        CRC32 crc32 = new CRC32();
        crc32.update(message, 0, message.length - CRC_LENGTH);
        ProtocolUtils.writeInt32BE(message, (int) crc32.getValue(), message.length - CRC_LENGTH);
        return message;
    }

    private int nextSequence() {
        return sequenceCounter.updateAndGet(current -> current >= Integer.MAX_VALUE ? 2 : current + 1);
    }

    private byte[] withPrefix(byte[] frameWithoutPrefix) {
        byte[] prefixed = new byte[4 + frameWithoutPrefix.length];
        ProtocolUtils.writeInt32BE(prefixed, frameWithoutPrefix.length, 0);
        System.arraycopy(frameWithoutPrefix, 0, prefixed, 4, frameWithoutPrefix.length);
        return prefixed;
    }
}
