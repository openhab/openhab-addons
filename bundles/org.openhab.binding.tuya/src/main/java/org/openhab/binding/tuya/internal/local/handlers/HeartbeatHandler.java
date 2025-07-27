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

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_MAXIMUM_MISSED_HEARTBEATS;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_TIMEOUT;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tuya.internal.local.CommandType;
import org.openhab.binding.tuya.internal.local.MessageWrapper;
import org.openhab.binding.tuya.internal.local.TuyaDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * The {@link HeartbeatHandler} is responsible for sending and receiving heartbeat messages
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HeartbeatHandler extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);
    private int heartBeatMissed = 0;

    @Override
    public void userEventTriggered(@NonNullByDefault({}) ChannelHandlerContext ctx, @NonNullByDefault({}) Object evt)
            throws Exception {
        if (!ctx.channel().hasAttr(TuyaDevice.DEVICE_ID_ATTR)) {
            logger.warn("{}: Failed to retrieve deviceId from ChannelHandlerContext. This is a bug.",
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
            return;
        }
        String deviceId = ctx.channel().attr(TuyaDevice.DEVICE_ID_ATTR).get();

        if (evt instanceof IdleStateEvent e) {
            if (IdleState.READER_IDLE.equals(e.state())) {
                logger.warn("{}{}: Did not receive a message from for {} seconds. Connection seems to be dead.",
                        deviceId, Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""),
                        TCP_CONNECTION_TIMEOUT);
                ctx.close();
            } else if (IdleState.WRITER_IDLE.equals(e.state())) {
                heartBeatMissed++;
                if (heartBeatMissed > TCP_CONNECTION_MAXIMUM_MISSED_HEARTBEATS) {
                    logger.warn("{}{}: Missed more than {} heartbeat responses. Connection seems to be dead.", deviceId,
                            Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""),
                            TCP_CONNECTION_MAXIMUM_MISSED_HEARTBEATS);
                    ctx.close();
                } else {
                    logger.trace("{}{}: Sending ping", deviceId,
                            Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
                    ctx.channel().writeAndFlush(new MessageWrapper<>(CommandType.HEART_BEAT, Map.of("dps", "")));
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(@NonNullByDefault({}) ChannelHandlerContext ctx, @NonNullByDefault({}) Object msg)
            throws Exception {
        if (!ctx.channel().hasAttr(TuyaDevice.DEVICE_ID_ATTR)) {
            logger.warn("{}: Failed to retrieve deviceId from ChannelHandlerContext. This is a bug.",
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
            return;
        }
        String deviceId = ctx.channel().attr(TuyaDevice.DEVICE_ID_ATTR).get();

        if (msg instanceof MessageWrapper<?> m) {
            if (CommandType.HEART_BEAT.equals(m.commandType)) {
                logger.trace("{}{}: Received pong", deviceId,
                        Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
                heartBeatMissed = 0;
                // do not forward HEART_BEAT messages
                ctx.fireChannelReadComplete();
                return;
            }
        }
        // forward to next handler
        ctx.fireChannelRead(msg);
    }
}
