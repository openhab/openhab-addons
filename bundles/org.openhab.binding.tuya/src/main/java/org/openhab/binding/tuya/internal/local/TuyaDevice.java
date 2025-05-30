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

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_HEARTBEAT_INTERVAL;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_TIMEOUT;
import static org.openhab.binding.tuya.internal.local.CommandType.CONTROL;
import static org.openhab.binding.tuya.internal.local.CommandType.CONTROL_NEW;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_QUERY;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_REFRESH;
import static org.openhab.binding.tuya.internal.local.CommandType.SESS_KEY_NEG_START;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_4;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_5;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.local.handlers.HeartbeatHandler;
import org.openhab.binding.tuya.internal.local.handlers.TuyaDecoder;
import org.openhab.binding.tuya.internal.local.handlers.TuyaEncoder;
import org.openhab.binding.tuya.internal.local.handlers.TuyaMessageHandler;
import org.openhab.binding.tuya.internal.local.handlers.UserEventHandler;
import org.openhab.binding.tuya.internal.util.CryptoUtil;
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
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;

/**
 * The {@link TuyaDevice} handles the device connection
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TuyaDevice implements ChannelFutureListener {
    public static final AttributeKey<String> DEVICE_ID_ATTR = AttributeKey.valueOf("deviceId");
    public static final AttributeKey<ProtocolVersion> PROTOCOL_ATTR = AttributeKey.valueOf("protocol");
    public static final AttributeKey<byte[]> SESSION_RANDOM_ATTR = AttributeKey.valueOf("sessionRandom");
    public static final AttributeKey<byte[]> SESSION_KEY_ATTR = AttributeKey.valueOf("sessionKey");

    private final Logger logger = LoggerFactory.getLogger(TuyaDevice.class);

    private final Bootstrap bootstrap = new Bootstrap();
    private final DeviceStatusListener deviceStatusListener;
    private final String deviceId;
    private final byte[] deviceKey;

    private final String address;
    private final ProtocolVersion protocolVersion;
    private @Nullable Channel channel;

    public TuyaDevice(Gson gson, DeviceStatusListener deviceStatusListener, EventLoopGroup eventLoopGroup,
            String deviceId, byte[] deviceKey, String address, String protocolVersion) {
        this.address = address;
        this.deviceId = deviceId;
        this.deviceKey = deviceKey;
        this.deviceStatusListener = deviceStatusListener;
        this.protocolVersion = ProtocolVersion.fromString(protocolVersion);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("idleStateHandler",
                        new IdleStateHandler(TCP_CONNECTION_TIMEOUT, TCP_CONNECTION_HEARTBEAT_INTERVAL, 0));
                pipeline.addLast("messageEncoder", new TuyaEncoder(gson));
                pipeline.addLast("messageDecoder", new TuyaDecoder(gson));
                pipeline.addLast("heartbeatHandler", new HeartbeatHandler());
                pipeline.addLast("deviceHandler", new TuyaMessageHandler(deviceStatusListener));
                pipeline.addLast("userEventHandler", new UserEventHandler());
            }
        });
        connect();
    }

    public void connect() {
        bootstrap.connect(address, 6668).addListener(this);
    }

    private void disconnect() {
        Channel channel = this.channel;
        if (channel != null) { // if channel == null we are not connected anyway
            channel.pipeline().fireUserEventTriggered(new UserEventHandler.DisposeEvent());
            this.channel = null;
        }
    }

    public void set(Map<Integer, @Nullable Object> command) {
        CommandType commandType = (protocolVersion == V3_4 || protocolVersion == V3_5) ? CONTROL_NEW : CONTROL;
        MessageWrapper<?> m = new MessageWrapper<>(commandType, Map.of("dps", command));
        Channel channel = this.channel;
        if (channel != null) {
            channel.writeAndFlush(m);
        } else {
            logger.warn("{}: Setting {} failed. Device is not connected.", deviceId, command);
        }
    }

    public void requestStatus() {
        MessageWrapper<?> m = new MessageWrapper<>(DP_QUERY, Map.of("dps", Map.of()));
        Channel channel = this.channel;
        if (channel != null) {
            channel.writeAndFlush(m);
        } else {
            logger.warn("{}: Querying status failed. Device is not connected.", deviceId);
        }
    }

    public void refreshStatus(List<Integer> dps) {
        MessageWrapper<?> m = new MessageWrapper<>(DP_REFRESH, Map.of("dpId", dps));
        Channel channel = this.channel;
        if (channel != null) {
            channel.writeAndFlush(m);
            requestStatus();
        } else {
            logger.warn("{}: Refreshing status failed. Device is not connected.", deviceId);
        }
    }

    public void dispose() {
        disconnect();
    }

    @Override
    public void operationComplete(@NonNullByDefault({}) ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            Channel channel = channelFuture.channel();
            channel.attr(DEVICE_ID_ATTR).set(deviceId);
            channel.attr(PROTOCOL_ATTR).set(protocolVersion);
            // session key is device key before negotiation
            channel.attr(SESSION_KEY_ATTR).set(deviceKey);

            if (protocolVersion == V3_4 || protocolVersion == V3_5) {
                byte[] sessionRandom = CryptoUtil.generateRandom(16);
                channel.attr(SESSION_RANDOM_ATTR).set(sessionRandom);
                this.channel = channel;

                // handshake for session key required
                MessageWrapper<?> m = new MessageWrapper<>(SESS_KEY_NEG_START, sessionRandom);
                channel.writeAndFlush(m);
            } else {
                this.channel = channel;

                // no handshake for 3.1/3.3
                requestStatus();
            }
        } else {
            logger.debug("{}{}: Failed to connect: {}", deviceId,
                    Objects.requireNonNullElse(channelFuture.channel().remoteAddress(), ""),
                    channelFuture.cause().getMessage());
            this.channel = null;
            deviceStatusListener.connectionStatus(false);
        }
    }
}
