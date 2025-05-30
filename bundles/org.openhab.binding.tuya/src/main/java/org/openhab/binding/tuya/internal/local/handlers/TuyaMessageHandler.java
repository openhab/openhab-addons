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

import static org.openhab.binding.tuya.internal.local.TuyaDevice.PROTOCOL_ATTR;
import static org.openhab.binding.tuya.internal.local.TuyaDevice.SESSION_KEY_ATTR;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tuya.internal.local.CommandType;
import org.openhab.binding.tuya.internal.local.DeviceStatusListener;
import org.openhab.binding.tuya.internal.local.MessageWrapper;
import org.openhab.binding.tuya.internal.local.ProtocolVersion;
import org.openhab.binding.tuya.internal.local.TuyaDevice;
import org.openhab.binding.tuya.internal.local.dto.TcpStatusPayload;
import org.openhab.binding.tuya.internal.util.CryptoUtil;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * The {@link TuyaMessageHandler} is a Netty channel handler
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TuyaMessageHandler extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(TuyaMessageHandler.class);

    private final DeviceStatusListener deviceStatusListener;

    public TuyaMessageHandler(DeviceStatusListener deviceStatusListener) {
        this.deviceStatusListener = deviceStatusListener;
    }

    @Override
    public void channelActive(@NonNullByDefault({}) ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().hasAttr(TuyaDevice.DEVICE_ID_ATTR) || !ctx.channel().hasAttr(SESSION_KEY_ATTR)) {
            logger.warn("{}: Failed to retrieve deviceId or sessionKey from ChannelHandlerContext. This is a bug.",
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
            return;
        }
        String deviceId = ctx.channel().attr(TuyaDevice.DEVICE_ID_ATTR).get();

        logger.debug("{}{}: Connection established.", deviceId,
                Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
        deviceStatusListener.connectionStatus(true);
    }

    @Override
    public void channelInactive(@NonNullByDefault({}) ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().hasAttr(TuyaDevice.DEVICE_ID_ATTR) || !ctx.channel().hasAttr(SESSION_KEY_ATTR)) {
            logger.warn("{}: Failed to retrieve deviceId or sessionKey from ChannelHandlerContext. This is a bug.",
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
            return;
        }
        String deviceId = ctx.channel().attr(TuyaDevice.DEVICE_ID_ATTR).get();

        logger.debug("{}{}: Connection terminated.", deviceId,
                Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
        deviceStatusListener.connectionStatus(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(@NonNullByDefault({}) ChannelHandlerContext ctx, @NonNullByDefault({}) Object msg)
            throws Exception {
        if (!ctx.channel().hasAttr(TuyaDevice.DEVICE_ID_ATTR) || !ctx.channel().hasAttr(SESSION_KEY_ATTR)
                || !ctx.channel().hasAttr(PROTOCOL_ATTR)) {
            logger.warn(
                    "{}: Failed to retrieve deviceId, sessionKey or protocol from ChannelHandlerContext. This is a bug.",
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
            return;
        }
        String deviceId = ctx.channel().attr(TuyaDevice.DEVICE_ID_ATTR).get();
        ProtocolVersion protocol = ctx.channel().attr(TuyaDevice.PROTOCOL_ATTR).get();

        if (msg instanceof MessageWrapper<?> m) {
            if (m.commandType == CommandType.DP_QUERY || m.commandType == CommandType.STATUS) {
                Map<Integer, Object> stateMap = null;
                if (m.content instanceof TcpStatusPayload payload) {
                    stateMap = payload.protocol == 4 ? payload.data.dps : payload.dps;
                }

                if (stateMap != null && !stateMap.isEmpty()) {
                    deviceStatusListener.processDeviceStatus(stateMap);
                }
            } else if (m.commandType == CommandType.DP_QUERY_NOT_SUPPORTED) {
                deviceStatusListener.processDeviceStatus(Map.of());
            } else if (m.commandType == CommandType.SESS_KEY_NEG_RESPONSE) {
                if (!ctx.channel().hasAttr(TuyaDevice.SESSION_KEY_ATTR)
                        || !ctx.channel().hasAttr(TuyaDevice.SESSION_RANDOM_ATTR)) {
                    logger.warn("{}{}: Session key negotiation failed because device key or session random is not set.",
                            deviceId, Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
                    return;
                }
                byte[] sessionKey = ctx.channel().attr(TuyaDevice.SESSION_KEY_ATTR).get();
                byte[] sessionRandom = ctx.channel().attr(TuyaDevice.SESSION_RANDOM_ATTR).get();
                byte[] localKeyHmac = CryptoUtil.hmac(sessionRandom, sessionKey);
                byte[] localKeyExpectedHmac = Arrays.copyOfRange((byte[]) m.content, 16, 16 + 32);

                if (!Arrays.equals(localKeyHmac, localKeyExpectedHmac)) {
                    logger.warn(
                            "{}{}: Session key negotiation failed during Hmac validation: calculated {}, expected {}",
                            deviceId, Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""),
                            localKeyHmac != null ? HexUtils.bytesToHex(localKeyHmac) : "<null>",
                            HexUtils.bytesToHex(localKeyExpectedHmac));
                    return;
                }

                byte[] remoteKey = Arrays.copyOf((byte[]) m.content, 16);
                byte[] remoteKeyHmac = CryptoUtil.hmac(remoteKey, sessionKey);
                MessageWrapper<?> response = new MessageWrapper<>(CommandType.SESS_KEY_NEG_FINISH, remoteKeyHmac);

                ctx.channel().writeAndFlush(response);

                byte[] newSessionKey = CryptoUtil.generateSessionKey(sessionRandom, remoteKey, sessionKey, protocol);
                if (newSessionKey == null) {
                    logger.warn("{}{}: Session key negotiation failed because session key is null.", deviceId,
                            Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
                    return;
                }
                ctx.channel().attr(TuyaDevice.SESSION_KEY_ATTR).set(newSessionKey);
            }
        }
    }
}
