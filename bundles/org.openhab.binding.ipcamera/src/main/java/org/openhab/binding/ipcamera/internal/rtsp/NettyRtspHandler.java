/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ipcamera.internal.rtsp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * The {@link NettyRtspHandler} is used to decode RTSP traffic into message Strings.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class NettyRtspHandler extends ChannelDuplexHandler {
    RtspConnection rtspConnection;

    NettyRtspHandler(RtspConnection rtspConnection) {
        this.rtspConnection = rtspConnection;
    }

    @Override
    public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
        if (msg == null || ctx == null) {
            return;
        }
        if (!(msg instanceof LastHttpContent)) {
            rtspConnection.processMessage(msg);
        } else {
            ctx.close();
        }
    }

    @Override
    public void channelReadComplete(@Nullable ChannelHandlerContext ctx) {
    }

    @Override
    public void handlerAdded(@Nullable ChannelHandlerContext ctx) {
    }

    @Override
    public void handlerRemoved(@Nullable ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(@Nullable ChannelHandlerContext ctx, @Nullable Throwable cause) {
    }

    @Override
    public void userEventTriggered(@Nullable ChannelHandlerContext ctx, @Nullable Object evt) throws Exception {
    }
}
