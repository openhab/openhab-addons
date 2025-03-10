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

import java.io.IOException;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tuya.internal.local.TuyaDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * The {@link UserEventHandler} is a Netty handler for events (used for closing the connection)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class UserEventHandler extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(UserEventHandler.class);

    @Override
    public void userEventTriggered(@NonNullByDefault({}) ChannelHandlerContext ctx, @NonNullByDefault({}) Object evt) {
        if (!ctx.channel().hasAttr(TuyaDevice.DEVICE_ID_ATTR)) {
            logger.warn("Failed to retrieve deviceId from ChannelHandlerContext. This is a bug.");
            return;
        }
        String deviceId = ctx.channel().attr(TuyaDevice.DEVICE_ID_ATTR).get();

        if (evt instanceof DisposeEvent) {
            logger.debug("{}{}: Received DisposeEvent, closing channel", deviceId,
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(@NonNullByDefault({}) ChannelHandlerContext ctx, @NonNullByDefault({}) Throwable cause)
            throws Exception {
        if (!ctx.channel().hasAttr(TuyaDevice.DEVICE_ID_ATTR)) {
            logger.warn("{}: Failed to retrieve deviceId from ChannelHandlerContext. This is a bug.",
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""));
            ctx.close();
            return;
        }
        String deviceId = ctx.channel().attr(TuyaDevice.DEVICE_ID_ATTR).get();

        if (cause instanceof IOException) {
            logger.debug("{}{}: IOException caught, closing channel.", deviceId,
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""), cause);
            logger.debug("IOException caught: ", cause);
        } else {
            logger.warn("{}{}: {} caught, closing the channel", deviceId,
                    Objects.requireNonNullElse(ctx.channel().remoteAddress(), ""), cause.getClass(), cause);
        }
        ctx.close();
    }

    public static class DisposeEvent {
    }
}
