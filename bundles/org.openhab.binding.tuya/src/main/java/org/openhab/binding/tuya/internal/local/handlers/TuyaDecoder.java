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

import static org.openhab.binding.tuya.internal.local.CommandType.BROADCAST_LPV34;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_QUERY;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_QUERY_NOT_SUPPORTED;
import static org.openhab.binding.tuya.internal.local.CommandType.STATUS;
import static org.openhab.binding.tuya.internal.local.CommandType.UDP;
import static org.openhab.binding.tuya.internal.local.CommandType.UDP_NEW;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_3;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_4;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_5;
import static org.openhab.binding.tuya.internal.local.TuyaDevice.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tuya.internal.local.CommandType;
import org.openhab.binding.tuya.internal.local.MessageWrapper;
import org.openhab.binding.tuya.internal.local.ProtocolVersion;
import org.openhab.binding.tuya.internal.local.dto.DiscoveryMessage;
import org.openhab.binding.tuya.internal.local.dto.TcpStatusPayload;
import org.openhab.binding.tuya.internal.util.CryptoUtil;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * The {@link TuyaDecoder} is a Netty Decoder for encoding Tuya Local messages
 *
 * Parts of this code are inspired by the TuyAPI project (see notice file)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TuyaDecoder extends ByteToMessageDecoder {
    private final Logger logger = LoggerFactory.getLogger(TuyaDecoder.class);

    private final Gson gson;

    public TuyaDecoder(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void decode(@NonNullByDefault({}) ChannelHandlerContext ctx, @NonNullByDefault({}) ByteBuf in,
            @NonNullByDefault({}) List<Object> out) throws Exception {
        if (in.readableBytes() < 24) {
            // minimum packet size is 16 bytes header + 8 bytes suffix
            return;
        }

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

        // we need to take a copy first so the buffer stays intact if we exit early
        ByteBuf inCopy = in.copy();
        byte[] bytes = new byte[inCopy.readableBytes()];
        inCopy.readBytes(bytes);
        inCopy.release();

        if (logger.isTraceEnabled()) {
            logger.trace("{}{}: Received encoded '{}'", deviceId,
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""), HexUtils.bytesToHex(bytes));
            logger.trace("{}{}: Protocol version '{}'", deviceId,
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""), protocol.getString());
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int prefix = buffer.getInt();

        if (prefix == 0x006699 && protocol != V3_5) {
            protocol = V3_5;
            logger.debug("Set protocol version to {}", protocol.getString());
        }

        int headerLength = protocol == V3_5 ? 22 : 16;

        if (protocol == V3_5) {
            // skip 2 unknown bytes in header
            buffer.position(buffer.position() + 2);
        }

        // this method call is necessary to correctly move the pointer within the buffer.
        buffer.getInt();
        CommandType commandType = CommandType.fromCode(buffer.getInt());
        int payloadLength = buffer.getInt();

        byte[] header = new byte[14];
        if (protocol == V3_5) {
            // get header for GCM AAD
            System.arraycopy(buffer.array(), 4, header, 0, 14);
        }

        if (buffer.limit() < payloadLength + headerLength) {
            // there are less bytes than needed, exit early
            logger.trace("Did not receive enough bytes from '{}', exiting early", deviceId);
            return;
        } else {
            // we have enough bytes, skip them from the input buffer and proceed processing
            in.skipBytes(payloadLength + headerLength);
        }

        byte[] payload;

        if (protocol == V3_5) {
            payload = new byte[payloadLength];
        } else {
            int returnCode = buffer.getInt();

            if ((returnCode & 0xffffff00) != 0) {
                // rewind if no return code is present
                buffer.position(buffer.position() - 4);
                payload = protocol == V3_4 ? new byte[payloadLength - 32] : new byte[payloadLength - 8];
            } else {
                payload = protocol == V3_4 ? new byte[payloadLength - 32 - 8] : new byte[payloadLength - 8 - 4];
            }
        }

        buffer.get(payload);

        if (protocol == V3_4 && commandType != UDP && commandType != UDP_NEW) {
            byte[] fullMessage = new byte[buffer.position()];
            buffer.position(0);
            buffer.get(fullMessage);
            byte[] expectedHmac = new byte[32];
            buffer.get(expectedHmac);
            byte[] calculatedHmac = CryptoUtil.hmac(fullMessage, sessionKey);
            if (!Arrays.equals(expectedHmac, calculatedHmac)) {
                logger.warn("{}{}: Checksum failed for message: calculated {}, found {}", deviceId,
                        Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""),
                        calculatedHmac != null ? HexUtils.bytesToHex(calculatedHmac) : "<null>",
                        HexUtils.bytesToHex(expectedHmac));
                return;
            }
        } else if (protocol != V3_5) {
            int crc = buffer.getInt();
            // header + payload without suffix and checksum
            int calculatedCrc = CryptoUtil.calculateChecksum(bytes, 0, 16 + payloadLength - 8);
            if (calculatedCrc != crc) {
                logger.warn("{}{}: Checksum failed for message: calculated {}, found {}", deviceId,
                        Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""), calculatedCrc, crc);
                return;
            }
        }

        int suffix = buffer.getInt();
        if ((prefix != 0x000055aa || suffix != 0x0000aa55) && (prefix != 0x00006699 || suffix != 0x00009966)) {
            logger.warn("{}{}: Decoding failed: Prefix or suffix invalid.", deviceId,
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
            return;
        }

        if (Arrays.equals(Arrays.copyOfRange(payload, 0, protocol.getBytes().length), protocol.getBytes())) {
            if (protocol == V3_3) {
                // Remove 3.3 header
                payload = Arrays.copyOfRange(payload, 15, payload.length);
            } else {
                payload = Base64.getDecoder().decode(Arrays.copyOfRange(payload, 19, payload.length));
            }
        }

        MessageWrapper<?> m;
        if (commandType == UDP) {
            // UDP is unencrypted
            m = new MessageWrapper<>(commandType,
                    Objects.requireNonNull(gson.fromJson(new String(payload), DiscoveryMessage.class)));
        } else {
            byte[] decodedMessage = switch (protocol) {
                case V3_5 -> CryptoUtil.decryptAesGcm(payload, sessionKey, header, null);
                case V3_4 -> CryptoUtil.decryptAesEcb(payload, sessionKey, true);
                default -> CryptoUtil.decryptAesEcb(payload, sessionKey, false);
            };
            if (decodedMessage == null) {
                return;
            }

            if (protocol == V3_5) {
                // Remove return code
                decodedMessage = Arrays.copyOfRange(decodedMessage, 4, decodedMessage.length);
            }

            if (Arrays.equals(Arrays.copyOfRange(decodedMessage, 0, protocol.getBytes().length), protocol.getBytes())) {
                if (protocol == V3_4 || protocol == V3_5) {
                    // Remove 3.4 or 3.5 header
                    decodedMessage = Arrays.copyOfRange(decodedMessage, 15, decodedMessage.length);
                }
            }

            if (logger.isTraceEnabled()) {
                logger.trace("{}{}: Decoded raw payload: {}", deviceId,
                        Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""),
                        HexUtils.bytesToHex(decodedMessage));
            }

            try {
                String decodedString = new String(decodedMessage).trim();
                if (commandType == DP_QUERY && "json obj data unvalid".equals(decodedString)) {
                    // "json obj data unvalid" would also result in a JSONSyntaxException but is a known error when
                    // DP_QUERY is not supported by the device. Using a CONTROL message with null values is a known
                    // workaround, cf. https://github.com/codetheweb/tuyapi/blob/master/index.js#L156
                    logger.info("{}{}: DP_QUERY not supported. Trying to request with CONTROL.", deviceId,
                            Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
                    m = new MessageWrapper<>(DP_QUERY_NOT_SUPPORTED, Map.of());
                } else if (commandType == STATUS || commandType == DP_QUERY) {
                    m = new MessageWrapper<>(commandType,
                            Objects.requireNonNull(gson.fromJson(decodedString, TcpStatusPayload.class)));
                } else if (commandType == UDP_NEW || commandType == BROADCAST_LPV34) {
                    m = new MessageWrapper<>(commandType,
                            Objects.requireNonNull(gson.fromJson(decodedString, DiscoveryMessage.class)));
                } else {
                    m = new MessageWrapper<>(commandType, decodedMessage);
                }
            } catch (JsonSyntaxException e) {
                logger.warn("{}{} failed to parse JSON: {}", deviceId,
                        Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""), e.getMessage());
                return;
            }
        }

        logger.debug("{}{}: Received {}", deviceId, Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""), m);
        out.add(m);
    }
}
