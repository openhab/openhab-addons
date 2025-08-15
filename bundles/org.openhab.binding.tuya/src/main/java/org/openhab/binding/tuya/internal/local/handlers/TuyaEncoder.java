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
package org.openhab.binding.tuya.internal.local.handlers;

import static org.openhab.binding.tuya.internal.local.CommandType.DP_QUERY;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_QUERY_NEW;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_REFRESH;
import static org.openhab.binding.tuya.internal.local.CommandType.HEART_BEAT;
import static org.openhab.binding.tuya.internal.local.CommandType.REQ_DEVINFO;
import static org.openhab.binding.tuya.internal.local.CommandType.SESS_KEY_NEG_FINISH;
import static org.openhab.binding.tuya.internal.local.CommandType.SESS_KEY_NEG_START;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_3;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_4;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_5;
import static org.openhab.binding.tuya.internal.local.TuyaDevice.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tuya.internal.local.CommandType;
import org.openhab.binding.tuya.internal.local.MessageWrapper;
import org.openhab.binding.tuya.internal.local.ProtocolVersion;
import org.openhab.binding.tuya.internal.util.CryptoUtil;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * The {@link TuyaEncoder} is a Netty Encoder for encoding Tuya Local messages
 *
 * Parts of this code are inspired by the TuyAPI project (see notice file)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TuyaEncoder extends MessageToByteEncoder<MessageWrapper<?>> {
    private final Logger logger = LoggerFactory.getLogger(TuyaEncoder.class);

    private final Gson gson;

    private int sequenceNo = 0;

    public TuyaEncoder(Gson gson) {
        this.gson = gson;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void encode(@NonNullByDefault({}) ChannelHandlerContext ctx, MessageWrapper<?> msg,
            @NonNullByDefault({}) ByteBuf out) throws Exception {
        if (!ctx.channel().hasAttr(DEVICE_ID_ATTR) || !ctx.channel().hasAttr(PROTOCOL_ATTR)
                || !ctx.channel().hasAttr(SESSION_KEY_ATTR)) {
            logger.warn(
                    "{}: Failed to retrieve deviceId, protocol or sessionKey from ChannelHandlerContext. This is a bug.",
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
            return;
        }
        String deviceId = ctx.channel().attr(DEVICE_ID_ATTR).get();
        ProtocolVersion protocol = ctx.channel().attr(PROTOCOL_ATTR).get();
        byte[] sessionKey = ctx.channel().attr(SESSION_KEY_ATTR).get();

        byte[] payloadBytes;

        // prepare payload
        if (msg.content == null || msg.content instanceof Map<?, ?>) {
            Map<String, Object> content = (Map<String, Object>) msg.content;
            Map<String, Object> payload = new HashMap<>();
            if (msg.commandType == REQ_DEVINFO) {
                if (content != null) {
                    payload.putAll(content);
                }
            } else if (protocol == V3_4 || protocol == V3_5) {
                payload.put("protocol", 5);
                payload.put("t", System.currentTimeMillis() / 1000);
                Map<String, Object> data = new HashMap<>();
                data.put("cid", deviceId);
                data.put("ctype", 0);
                if (content != null) {
                    data.putAll(content);
                }
                payload.put("data", data);
            } else {
                payload.put("devId", deviceId);
                payload.put("gwId", deviceId);
                payload.put("uid", deviceId);
                payload.put("t", System.currentTimeMillis() / 1000);
                if (content != null) {
                    payload.putAll(content);
                }
            }

            logger.debug("{}{}: Sending {}, payload {}", deviceId,
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""), msg.commandType, payload);

            String json = gson.toJson(payload);
            payloadBytes = json.getBytes(StandardCharsets.UTF_8);
        } else if (msg.content instanceof byte[] contentBytes) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}{}: Sending payload {}", deviceId,
                        Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""),
                        HexUtils.bytesToHex(contentBytes));
            }
            payloadBytes = contentBytes.clone();
        } else {
            logger.warn("Can't determine payload type for '{}', discarding.", msg.content);
            return;
        }

        Optional<byte[]> bufferOptional = switch (protocol) {
            case V3_5 -> encode35(msg.commandType, payloadBytes, sessionKey);
            case V3_4 -> encode34(msg.commandType, payloadBytes, sessionKey);
            default -> encodePre34(msg.commandType, payloadBytes, sessionKey, protocol);
        };

        bufferOptional.ifPresentOrElse(buffer -> {
            if (logger.isTraceEnabled()) {
                logger.trace("{}{}: Sending encoded '{}'", deviceId, ctx.channel().remoteAddress(),
                        HexUtils.bytesToHex(buffer));
            }

            out.writeBytes(buffer);
        }, () -> logger.debug("{}{}: Encoding returned an empty buffer", deviceId, ctx.channel().remoteAddress()));
    }

    private Optional<byte[]> encodePre34(CommandType commandType, byte[] payload, byte[] deviceKey,
            ProtocolVersion protocol) {
        byte[] payloadBytes = payload;
        if (protocol == V3_3) {
            // Always encrypted
            payloadBytes = CryptoUtil.encryptAesEcb(payloadBytes, deviceKey, true);
            if (payloadBytes == null) {
                return Optional.empty();
            }

            if (commandType != DP_QUERY && commandType != CommandType.DP_REFRESH) {
                // Add 3.3 header
                ByteBuffer buffer = ByteBuffer.allocate(payloadBytes.length + 15);
                buffer.put("3.3".getBytes(StandardCharsets.UTF_8));
                buffer.position(15);
                buffer.put(payloadBytes);
                payloadBytes = buffer.array();
            }
        } else if (CommandType.CONTROL.equals(commandType)) {
            // Protocol 3.1 and below, only encrypt data if necessary
            byte[] encryptedPayload = CryptoUtil.encryptAesEcb(payloadBytes, deviceKey, true);
            if (encryptedPayload == null) {
                return Optional.empty();
            }
            String payloadStr = Base64.getEncoder().encodeToString(encryptedPayload);
            String hash = CryptoUtil
                    .md5("data=" + payloadStr + "||lpv=" + protocol.getString() + "||" + new String(deviceKey));

            // Create byte buffer from hex data
            payloadBytes = (protocol + hash.substring(8, 24) + payloadStr).getBytes(StandardCharsets.UTF_8);
        }

        // Allocate buffer with room for payload + 24 bytes for
        // prefix, sequence, command, length, crc, and suffix
        ByteBuffer buffer = ByteBuffer.allocate(payloadBytes.length + 24);

        // Add prefix, command, and length
        buffer.putInt(0x000055AA);
        buffer.putInt(++sequenceNo);
        buffer.putInt(commandType.getCode());
        buffer.putInt(payloadBytes.length + 8);

        // Add payload
        buffer.put(payloadBytes);

        // Calculate and add checksum
        int calculatedCrc = CryptoUtil.calculateChecksum(buffer.array(), 0, payloadBytes.length + 16);
        buffer.putInt(calculatedCrc);

        // Add postfix
        buffer.putInt(0x0000AA55);

        return Optional.of(buffer.array());
    }

    private Optional<byte[]> encode34(CommandType commandType, byte[] payloadBytes, byte[] sessionKey) {
        byte[] rawPayload = payloadBytes;

        if (commandType != DP_QUERY && commandType != HEART_BEAT && commandType != DP_QUERY_NEW
                && commandType != SESS_KEY_NEG_START && commandType != SESS_KEY_NEG_FINISH
                && commandType != DP_REFRESH) {
            rawPayload = new byte[payloadBytes.length + 15];
            System.arraycopy("3.4".getBytes(StandardCharsets.UTF_8), 0, rawPayload, 0, 3);
            System.arraycopy(payloadBytes, 0, rawPayload, 15, payloadBytes.length);
        }

        byte padding = (byte) (0x10 - (rawPayload.length & 0xf));
        byte[] padded = new byte[rawPayload.length + padding];
        Arrays.fill(padded, padding);
        System.arraycopy(rawPayload, 0, padded, 0, rawPayload.length);

        byte[] encryptedPayload = CryptoUtil.encryptAesEcb(padded, sessionKey, false);
        if (encryptedPayload == null) {
            return Optional.empty();
        }

        ByteBuffer buffer = ByteBuffer.allocate(encryptedPayload.length + 52);

        // Add prefix, command, and length
        buffer.putInt(0x000055AA);
        buffer.putInt(++sequenceNo);
        buffer.putInt(commandType.getCode());
        buffer.putInt(encryptedPayload.length + 0x24);

        // Add payload
        buffer.put(encryptedPayload);

        // Calculate and add checksum
        byte[] checksumContent = new byte[encryptedPayload.length + 16];
        System.arraycopy(buffer.array(), 0, checksumContent, 0, encryptedPayload.length + 16);
        byte[] checksum = CryptoUtil.hmac(checksumContent, sessionKey);
        if (checksum == null) {
            return Optional.empty();
        }
        buffer.put(checksum);

        // Add postfix
        buffer.putInt(0x0000AA55);

        return Optional.of(buffer.array());
    }

    private Optional<byte[]> encode35(CommandType commandType, byte[] payloadBytes, byte[] sessionKey) {
        byte[] rawPayload = payloadBytes;

        if (commandType != DP_QUERY && commandType != HEART_BEAT && commandType != DP_QUERY_NEW
                && commandType != SESS_KEY_NEG_START && commandType != SESS_KEY_NEG_FINISH && commandType != DP_REFRESH
                && commandType != REQ_DEVINFO) {
            rawPayload = new byte[payloadBytes.length + 15];
            System.arraycopy("3.5".getBytes(StandardCharsets.UTF_8), 0, rawPayload, 0, 3);
            System.arraycopy(payloadBytes, 0, rawPayload, 15, payloadBytes.length);
        }

        ByteBuffer buffer = ByteBuffer.allocate(rawPayload.length + 22 + 12 + 16);

        // Add prefix
        buffer.putInt(0x00006699);
        // Add unknown 2 bytes
        buffer.putShort((short) 0x0000);
        // Add sequence number and command
        buffer.putInt(++sequenceNo);
        buffer.putInt(commandType.getCode());
        // Add length: 12 byte IV/nonce + payload length + 16 byte GCM Tag
        buffer.putInt(rawPayload.length + 12 + 16);
        // Get header data for GCM AAD
        byte[] header = new byte[14];
        System.arraycopy(buffer.array(), 4, header, 0, 14);

        byte[] encryptedPayload = CryptoUtil.encryptAesGcm(rawPayload, sessionKey, header, null);
        if (encryptedPayload == null) {
            return Optional.empty();
        }

        // Add encrypted payload
        buffer.put(encryptedPayload);

        // Add postfix
        buffer.putInt(0x00009966);

        return Optional.of(buffer.array());
    }
}
