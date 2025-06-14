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

import java.net.InetSocketAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

/**
 * The {@link UdpBroadcastHandler} is a Netty handler for create UDP broadcast message
 *
 * @author Andriy Yemets - Initial contribution
 */
@NonNullByDefault
public class UdpBroadcastHandler extends ChannelOutboundHandlerAdapter {

    private final String broadcastAddress;
    private final int broadcastPort;

    public UdpBroadcastHandler(String broadcastAddress, int broadcastPort) {
        this.broadcastAddress = broadcastAddress;
        this.broadcastPort = broadcastPort;
    }

    @Override
    public void write(@NonNullByDefault({}) ChannelHandlerContext ctx, @NonNullByDefault({}) Object msg,
            @NonNullByDefault({}) ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            DatagramPacket packet = new DatagramPacket(buf, new InetSocketAddress(broadcastAddress, broadcastPort));
            ctx.write(packet, promise);
        } else {
            super.write(ctx, msg, promise);
        }
    }
}
