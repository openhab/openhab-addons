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
package org.openhab.binding.tuya.internal.local;

import static org.openhab.binding.tuya.internal.local.CommandType.REQ_DEVINFO;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tuya.internal.local.handlers.TuyaEncoder;
import org.openhab.binding.tuya.internal.local.handlers.UdpBroadcastHandler;
import org.openhab.binding.tuya.internal.util.CryptoUtil;
import org.openhab.binding.tuya.internal.util.NetworkUtil;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * The {@link UdpDiscoverySender} sends device v3.5 discovery UDP broadcast message
 *
 * @author Andriy Yemets - Initial contribution
 */
@NonNullByDefault
public class UdpDiscoverySender {
    private static final byte[] TUYA_UDP_KEY = HexUtils.hexToBytes(CryptoUtil.md5("yGAdlopoPVldABfn"));

    private final Logger logger = LoggerFactory.getLogger(UdpDiscoverySender.class);

    private final Gson gson = new Gson();

    private final String broadcastAddress = "255.255.255.255";
    private final int broadcastPort = 7000;

    public UdpDiscoverySender() {
        //
    }

    public void sendMessage() {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("broadcastHandler",
                                    new UdpBroadcastHandler(broadcastAddress, broadcastPort));
                            pipeline.addLast("messageEncoder", new TuyaEncoder(gson));
                        }
                    });

            ChannelFuture futureChannel = b.bind(0).sync();
            Channel broadcastChannel = futureChannel.channel();
            broadcastChannel.attr(TuyaDevice.DEVICE_ID_ATTR).set("udpDiscoverySender");
            broadcastChannel.attr(TuyaDevice.PROTOCOL_ATTR).set(ProtocolVersion.V3_5);
            broadcastChannel.attr(TuyaDevice.SESSION_KEY_ATTR).set(TUYA_UDP_KEY);

            MessageWrapper<?> m = new MessageWrapper<>(REQ_DEVINFO,
                    Map.of("from", "app", "ip", NetworkUtil.getLocalIPAddress()));
            broadcastChannel.writeAndFlush(m).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            logger.error("Error during sending UDP Discovery message. {}", e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
    }
}
