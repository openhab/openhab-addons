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
package org.openhab.binding.tuya.internal.local;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tuya.internal.local.dto.DeviceInfo;
import org.openhab.binding.tuya.internal.local.handlers.DatagramToByteBufDecoder;
import org.openhab.binding.tuya.internal.local.handlers.DiscoveryMessageHandler;
import org.openhab.binding.tuya.internal.local.handlers.TuyaDecoder;
import org.openhab.binding.tuya.internal.local.handlers.UserEventHandler;
import org.openhab.binding.tuya.internal.util.CryptoUtil;
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
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * The {@link UdpDiscoveryListener} handles UDP device discovery message
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class UdpDiscoveryListener implements ChannelFutureListener {
    private static final byte[] TUYA_UDP_KEY = HexUtils.hexToBytes(CryptoUtil.md5("yGAdlopoPVldABfn"));

    private final Logger logger = LoggerFactory.getLogger(UdpDiscoveryListener.class);

    private final Gson gson = new Gson();

    private final Map<String, DeviceInfo> deviceInfos = new ConcurrentHashMap<>();
    private final Map<String, DeviceInfoSubscriber> deviceListeners = new ConcurrentHashMap<>();

    private List<Channel> channels = List.of();

    private static final List<PortVersion> PORTS = List.of( //
            new PortVersion(6666, ProtocolVersion.V3_1), // Raw channel
            new PortVersion(6667, ProtocolVersion.V3_1), // Encrypted channel
            new PortVersion(7000, ProtocolVersion.V3_5) // Encrypted channel V3.5
    );

    private final EventLoopGroup group;
    private boolean deactivate = false;

    public UdpDiscoveryListener(EventLoopGroup group) throws InterruptedException {
        this.group = group;
        activate();
    }

    private void activate() throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("udpDecoder", new DatagramToByteBufDecoder());
                        pipeline.addLast("messageDecoder", new TuyaDecoder(gson));
                        pipeline.addLast("discoveryHandler", new DiscoveryMessageHandler(deviceInfos, deviceListeners));
                        pipeline.addLast("userEventHandler", new UserEventHandler());
                    }
                });

        channels = PORTS.stream().map(portVersion -> {
            try {
                ChannelFuture future = b.bind(portVersion.port()).addListener(this).sync();
                Channel channel = future.channel();
                channel.attr(TuyaDevice.DEVICE_ID_ATTR).set("udpListener");
                channel.attr(TuyaDevice.PROTOCOL_ATTR).set(portVersion.version());
                channel.attr(TuyaDevice.SESSION_KEY_ATTR).set(TUYA_UDP_KEY);
                return channel;
            } catch (InterruptedException e) {
                logger.warn("Interrupted while starting Tuya UDP listener on port {}: {}", portVersion.port(),
                        e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.warn("Error starting Tuya UDP listener on port {}: {}", portVersion.port(), e.getMessage());
            }
            return null;
        }).filter(Objects::nonNull).toList();
    }

    public void deactivate() {
        deactivate = true;
        channels.forEach(Channel::close);
    }

    public void registerListener(String deviceId, DeviceInfoSubscriber subscriber) {
        if (deviceListeners.put(deviceId, subscriber) != null) {
            logger.warn("Registered a second listener for '{}'.", deviceId);
        }
        DeviceInfo deviceInfo = deviceInfos.get(deviceId);
        if (deviceInfo != null) {
            subscriber.deviceInfoChanged(deviceInfo);
        }
    }

    public void unregisterListener(DeviceInfoSubscriber deviceInfoSubscriber) {
        deviceListeners.entrySet().removeIf(e -> deviceInfoSubscriber.equals(e.getValue()));
    }

    @Override
    public void operationComplete(@NonNullByDefault({}) ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess() && !deactivate) {
            // if we are not disposing, restart listener after an error
            deactivate();
            activate();
        }
    }

    private static record PortVersion(int port, ProtocolVersion version) {
    }
}
