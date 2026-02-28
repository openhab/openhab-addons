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

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_HEARTBEAT_INTERVAL;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_MESSAGE_RESPONSE;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECT_RETRY_INTERVAL;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECT_TIMEOUT;
import static org.openhab.binding.tuya.internal.local.CommandType.CONTROL;
import static org.openhab.binding.tuya.internal.local.CommandType.CONTROL_NEW;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_QUERY;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_REFRESH;
import static org.openhab.binding.tuya.internal.local.CommandType.HEART_BEAT;
import static org.openhab.binding.tuya.internal.local.CommandType.SESS_KEY_NEG_START;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_4;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_5;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
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
    private @Nullable ScheduledFuture<?> reconnectFuture;
    private final DeviceStatusListener deviceStatusListener;
    private final EventLoopGroup eventLoopGroup;
    private final String deviceId;
    private final byte[] deviceKey;

    private final String address;
    private final ProtocolVersion protocolVersion;
    private @Nullable Channel channel;

    private boolean queryUsesControl = false;

    private class HeartbeatSender extends IdleStateHandler {
        public HeartbeatSender() {
            super(0, TCP_CONNECTION_HEARTBEAT_INTERVAL, 0);
        }

        @Override
        public void write(@Nullable ChannelHandlerContext ctx, @Nullable Object msg, @Nullable ChannelPromise promise)
                throws Exception {
            if (ctx != null) {
                // All messages sent to the device should trigger a timely response.
                ctx.pipeline().replace(this, "idleHandler", new IdleHandler());
                ctx.write(msg, promise);
            }
        }

        @Override
        protected void channelIdle(@Nullable ChannelHandlerContext ctx, @Nullable IdleStateEvent evt) throws Exception {
            if (ctx != null) {
                logger.trace("{}{}: Sending heart beat", deviceId, address);
                ctx.channel().writeAndFlush(new MessageWrapper<>(HEART_BEAT, Map.of("dps", "")));
            }
        }
    }

    private class IdleHandler extends IdleStateHandler {
        public IdleHandler() {
            super(TCP_CONNECTION_MESSAGE_RESPONSE, 0, 0);
        }

        @Override
        public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
            if (ctx != null) {
                ctx.pipeline().replace(this, "heartbeatSender", new HeartbeatSender());
                ctx.fireChannelRead(msg);
            }
        }

        @Override
        protected void channelIdle(@Nullable ChannelHandlerContext ctx, @Nullable IdleStateEvent evt) throws Exception {
            if (ctx != null) {
                logger.debug("{}{}: Connection seems to be dead.", deviceId, address);
                ctx.close();
            }
        }
    }

    public TuyaDevice(Gson gson, DeviceStatusListener deviceStatusListener, EventLoopGroup eventLoopGroup,
            String deviceId, byte[] deviceKey, String address, String protocolVersion) {
        this.deviceStatusListener = deviceStatusListener;
        this.eventLoopGroup = eventLoopGroup;
        this.deviceId = deviceId;
        this.deviceKey = deviceKey;
        this.address = address;
        this.protocolVersion = ProtocolVersion.fromString(protocolVersion);

        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                TCP_CONNECT_TIMEOUT);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("messageEncoder", new TuyaEncoder(gson));
                pipeline.addLast("messageDecoder", new TuyaDecoder(gson));
                pipeline.addLast("heartbeatSender", new HeartbeatSender());
                pipeline.addLast("deviceHandler", new TuyaMessageHandler(deviceStatusListener));
                pipeline.addLast("userEventHandler", new UserEventHandler());
            }
        });

        connect();
    }

    public void setQueryUsesControl() {
        queryUsesControl = true;
    }

    private void connect() {
        logger.trace("{}: connecting", deviceId);

        bootstrap.connect(address, 6668).addListener(this);
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

    public void requestStatus(Collection<Integer> dps) {
        if (!queryUsesControl) {
            // CommandType commandType = (protocolVersion == V3_4 || protocolVersion == V3_5) ? DP_QUERY_NEW : DP_QUERY;
            CommandType commandType = DP_QUERY;
            MessageWrapper<?> m = new MessageWrapper<>(commandType, Map.of("dps", dps));
            Channel channel = this.channel;
            if (channel != null) {
                channel.writeAndFlush(m);
            } else {
                logger.warn("{}: Querying status failed. Device is not connected.", deviceId);
            }
        } else {
            Map<Integer, @Nullable Object> dpMap = new HashMap<>();
            dps.forEach(dp -> dpMap.put(dp, null));
            set(dpMap);
        }
    }

    public void refreshStatus(Collection<Integer> dps) {
        MessageWrapper<?> m = new MessageWrapper<>(DP_REFRESH, Map.of("dpId", dps));
        Channel channel = this.channel;
        if (channel != null) {
            channel.writeAndFlush(m);
            // We could try a requestStatus(dps) here however it shouldn't be necessary as
            // once new values for the DPs have been sampled the device should send an update
            // (but not necessarily if the new values are the same as the old).
        } else {
            logger.warn("{}: Refreshing status failed. Device is not connected.", deviceId);
        }
    }

    public void dispose() {
        logger.debug("{}: disposed", deviceId);

        Channel channel = this.channel;
        this.channel = null;

        if (channel != null) {
            channel.closeFuture().cancel(true);
        }

        ScheduledFuture<?> future = reconnectFuture;
        if (future != null) {
            future.cancel(true);
            reconnectFuture = null;
        }

        if (channel != null) {
            channel.close();
        }
    }

    @Override
    public void operationComplete(@NonNullByDefault({}) ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            logger.debug("{}{}: channel connected", deviceId,
                    Objects.requireNonNullElse(channelFuture.channel().remoteAddress(), ""));

            Channel channel = channelFuture.channel();

            if (channel != null) {
                this.channel = channel;

                channel.closeFuture().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(@NonNullByDefault({}) ChannelFuture channelFuture) {
                        logger.debug("{}{}: channel closed", deviceId,
                                Objects.requireNonNullElse(channelFuture.channel().remoteAddress(), ""));

                        deviceStatusListener.connectionStatus(false);

                        if (!channelFuture.isCancelled()) {
                            reconnectFuture = eventLoopGroup.schedule(() -> {
                                logger.debug("{}{}: reconnect", deviceId,
                                        Objects.requireNonNullElse(channelFuture.channel().remoteAddress(), ""));
                                connect();
                            }, TCP_CONNECT_RETRY_INTERVAL, TimeUnit.MILLISECONDS);
                        }
                    }
                });

                channel.attr(DEVICE_ID_ATTR).set(deviceId);
                channel.attr(PROTOCOL_ATTR).set(protocolVersion);
                // session key is device key before negotiation
                channel.attr(SESSION_KEY_ATTR).set(deviceKey);

                if (protocolVersion == V3_4 || protocolVersion == V3_5) {
                    byte[] sessionRandom = CryptoUtil.generateRandom(16);
                    channel.attr(SESSION_RANDOM_ATTR).set(sessionRandom);

                    // handshake for session key required
                    MessageWrapper<?> m = new MessageWrapper<>(SESS_KEY_NEG_START, sessionRandom);
                    channel.writeAndFlush(m);
                } else {
                    // no handshake for 3.1/3.3
                    deviceStatusListener.connectionStatus(true);
                }
            }
        } else {
            logger.trace("{}{}: Failed to connect: {}", deviceId,
                    Objects.requireNonNullElse(channelFuture.channel().remoteAddress(), ""),
                    channelFuture.cause().getMessage());

            channelFuture.channel().close();

            reconnectFuture = eventLoopGroup.schedule(this::connect, TCP_CONNECT_RETRY_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }
}
