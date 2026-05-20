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
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_MAX_LIFETIME;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECTION_MESSAGE_RESPONSE;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECT_INITIAL_DELAY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECT_INITIAL_INTERVAL;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECT_RETRY_INTERVAL;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECT_TIMEOUT;
import static org.openhab.binding.tuya.internal.local.CommandType.CONTROL;
import static org.openhab.binding.tuya.internal.local.CommandType.CONTROL_NEW;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_QUERY;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_QUERY_NEW;
import static org.openhab.binding.tuya.internal.local.CommandType.DP_REFRESH;
import static org.openhab.binding.tuya.internal.local.CommandType.HEART_BEAT;
import static org.openhab.binding.tuya.internal.local.CommandType.SESS_KEY_NEG_FINISH;
import static org.openhab.binding.tuya.internal.local.CommandType.SESS_KEY_NEG_START;
import static org.openhab.binding.tuya.internal.local.CommandType.STATUS;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_4;
import static org.openhab.binding.tuya.internal.local.ProtocolVersion.V3_5;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import io.netty.channel.ChannelDuplexHandler;
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
import io.netty.util.concurrent.Future;

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
    private final int port;
    private final ProtocolVersion protocolVersion;

    private @Nullable ChannelFuture channelFuture;

    private final MessageWrapper<?> msgRequestAllControl;
    private final MessageWrapper<?> msgRequestAllDpQuery;
    private static final MessageWrapper<?> msgRefreshAll = new MessageWrapper<>(DP_REFRESH, Map.of("dpId", List.of()));

    private boolean firstRefusal = true;

    private class HeartbeatSender extends IdleStateHandler {
        private static final MessageWrapper<?> msgHeartbeat = new MessageWrapper<>(HEART_BEAT, Map.of());

        // Battery operated sensors remain online for about 15 seconds after the last STATUS. They accept
        // and acknowledge commands during that time but if they do not see another event that needs
        // reporting they will then go offline without closing or resetting connections. Therefore if we
        // see a STATUS we need a HEART_BEAT in 15 seconds time regardless of whether we have sent anything
        // else in the meantime.
        private boolean statusSeen = false;

        public HeartbeatSender() {
            super(0, TCP_CONNECTION_HEARTBEAT_INTERVAL, 0);
        }

        @Override
        public void write(@Nullable ChannelHandlerContext ctx, @Nullable Object msg, @Nullable ChannelPromise promise)
                throws Exception {
            if (ctx != null) {
                // Some devices that do not have any refreshable DPs do not implement DP_REFRESH and
                // simply ignore it. Some battery devices ignore anything sent too soon after they wake
                // up or erroneously claim not to support DP_QUERY and do not respond to CONTROL at all
                // if they have no function DPs and don't use CONTROL in place of DP_QUERY.
                // Therefore these messages do not push back the heartbeat timeout.
                if (!statusSeen && msg != null && msg instanceof MessageWrapper<?> m //
                        && m.commandType != DP_REFRESH //
                        && m.commandType != DP_QUERY && m.commandType != DP_QUERY_NEW //
                        && m.commandType != CONTROL && m.commandType != CONTROL_NEW) {
                    // Does reset the write timeout.
                    // The next heartbeat will be one heartbeat interval from now.
                    super.write(ctx, msg, promise);
                } else {
                    // Does NOT reset the write timeout.
                    // The next heartbeat will be one heartbeat interval from when the status message was seen.
                    ctx.write(msg, promise);
                }
            }
        }

        @Override
        public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
            if (ctx != null) {
                if (msg != null && msg instanceof MessageWrapper<?> m && m.commandType == STATUS) {
                    // The next heartbeat will be one heartbeat interval from now.
                    resetWriteTimeout();
                    statusSeen = true;
                } else if (statusSeen && msg != null && msg instanceof MessageWrapper<?> m
                        && m.commandType == HEART_BEAT) {
                    // A heartbeat acknowledgement after a status message is proof-of-life
                    // and we can revert to the normal heartbeat only if idle.
                    statusSeen = false;
                }

                super.channelRead(ctx, msg);
            }
        }

        @Override
        protected void channelIdle(@Nullable ChannelHandlerContext ctx, @Nullable IdleStateEvent evt) throws Exception {
            if (ctx != null) {
                ctx.channel().writeAndFlush(msgHeartbeat);
            }
        }
    }

    private class ResponseTimeoutHandler extends ChannelDuplexHandler {
        private @Nullable Future<?> responseTimeout = null;
        private @Nullable ChannelHandlerContext context = null;
        private TimeoutTask timeoutTask = new TimeoutTask();

        @Override
        public void handlerAdded(@Nullable ChannelHandlerContext ctx) throws Exception {
            this.context = ctx;

            super.handlerAdded(ctx);
        }

        @Override
        public void handlerRemoved(@Nullable ChannelHandlerContext ctx) throws Exception {
            var future = responseTimeout;
            if (future != null) {
                future.cancel(false);
            }

            super.handlerRemoved(ctx);
        }

        @Override
        public void write(@Nullable ChannelHandlerContext ctx, @Nullable Object msg, @Nullable ChannelPromise promise)
                throws Exception {
            if (ctx != null) {
                ctx.write(msg, promise);

                // Messages sent to the device should trigger a timely response. However, some devices
                // that do not have any refreshable DPs do not implement DP_REFRESH and simply ignore it.
                // Some battery devices ignore anything sent too soon after they wake up or erroneously
                // claim not to support DP_QUERY and do not respond to CONTROL at all however at least
                // one of a DP_QUERY/CONTROL pair should elicit a response. If we do not get a response
                // the connection does not recover so we do need to reconnect. And finally, since we
                // always send DP_QUERY and CONTROL in pairs (because some older devices require CONTROL
                // rather than DP_QUERY) we do not need to set a timeout for DP_QUERY.
                if (msg != null && msg instanceof MessageWrapper<?> m //
                        && m.commandType != SESS_KEY_NEG_FINISH //
                        && m.commandType != DP_REFRESH //
                        && m.commandType != DP_QUERY && m.commandType != DP_QUERY_NEW //
                ) {
                    var future = responseTimeout;
                    if (future != null) {
                        future.cancel(false);
                    }

                    responseTimeout = ctx.executor().schedule(timeoutTask, //
                            TCP_CONNECTION_MESSAGE_RESPONSE, TimeUnit.MILLISECONDS);
                }
            }
        }

        @Override
        public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
            if (ctx != null) {
                if (msg != null && msg instanceof MessageWrapper<?> m) {
                    // Almost anything can count as a response - it does not have to be specifically
                    // a response to what we sent. The exceptions are CONTROL/CONTROL_NEW which are
                    // ignored because there is normally some other response (DP_QUERY or STATUS)
                    // as well. If there isn't either the device or API is not active. (Sometimes
                    // devices seem to accept TCP connections before the API is fully initialized.)
                    if (m.commandType != CONTROL_NEW && m.commandType != CONTROL //
                    ) {
                        var future = responseTimeout;
                        if (future != null) {
                            future.cancel(false);
                            responseTimeout = null;
                        }
                    }
                }

                super.channelRead(ctx, msg);
            }
        }

        private final class TimeoutTask implements Runnable {
            @Override
            public void run() {
                var ctx = context;
                if (ctx != null && ctx.channel().isOpen()) {
                    logger.debug("{}/{}: Connection seems to be dead.", deviceId, address);
                    ctx.close();
                }
            }
        }
    }

    private class MaxLifetimeHandler extends IdleStateHandler {

        public MaxLifetimeHandler() {
            super(TCP_CONNECTION_MAX_LIFETIME, 0, 0);
        }

        // Override so that we never reset the read timeout.
        @Override
        public void channelReadComplete(@Nullable ChannelHandlerContext ctx) throws Exception {
            if (ctx != null) {
                ctx.fireChannelReadComplete();
            }
        }

        @Override
        protected void channelIdle(@Nullable ChannelHandlerContext ctx, @Nullable IdleStateEvent evt) throws Exception {
            if (ctx != null) {
                logger.debug("{}/{}: Maximum connection lifetime reached.", deviceId, address);
                ctx.close();
            }
        }
    }

    public TuyaDevice(Gson gson, DeviceStatusListener deviceStatusListener, EventLoopGroup eventLoopGroup,
            String deviceId, byte[] deviceKey, String address, int port, String protocolVersion,
            List<Integer> allDpIds) {
        this.deviceStatusListener = deviceStatusListener;
        this.eventLoopGroup = eventLoopGroup;
        this.deviceId = deviceId;
        this.deviceKey = deviceKey;
        this.address = address;
        this.port = port;
        this.protocolVersion = ProtocolVersion.fromString(protocolVersion);

        // CommandType commandType = (protocolVersion == V3_4 || protocolVersion == V3_5) ? DP_QUERY_NEW : DP_QUERY;
        CommandType commandType = DP_QUERY;
        msgRequestAllDpQuery = new MessageWrapper<>(commandType, Map.of("dps", allDpIds));

        Map<Integer, @Nullable Object> allDpIdsMap = new HashMap<>();
        allDpIds.forEach(dp -> allDpIdsMap.put(dp, null));
        msgRequestAllControl = new MessageWrapper<>(CONTROL, Map.of("dps", allDpIdsMap));

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
                pipeline.addLast("responseTimeoutHandler", new ResponseTimeoutHandler());
                pipeline.addLast("maxLifetimeHandler", new MaxLifetimeHandler());
                pipeline.addLast("deviceHandler", new TuyaMessageHandler(deviceStatusListener));
                pipeline.addLast("userEventHandler", new UserEventHandler());
            }
        });

        connect();
    }

    private void connect() {
        logger.trace("{}: connecting", deviceId);

        channelFuture = bootstrap.connect(address, port).addListener(this);
    }

    public void set(Map<Integer, @Nullable Object> command) {
        CommandType commandType = (protocolVersion == V3_4 || protocolVersion == V3_5) ? CONTROL_NEW : CONTROL;
        ChannelFuture channelFuture = this.channelFuture;
        if (channelFuture != null) {
            channelFuture.channel().writeAndFlush(new MessageWrapper<>(commandType, Map.of("dps", command)));
        } else {
            logger.warn("{}: Setting {} failed. Device is not connected.", deviceId, command);
        }
    }

    public void requestStatus() {
        ChannelFuture channelFuture = this.channelFuture;
        if (channelFuture == null) {
            logger.warn("{}: Querying status failed. Device is not connected.", deviceId);
            return;
        }

        channelFuture.channel().writeAndFlush(msgRequestAllDpQuery);
        channelFuture.channel().writeAndFlush(msgRequestAllControl);
    }

    public void refreshStatus() {
        ChannelFuture channelFuture = this.channelFuture;
        if (channelFuture != null) {
            channelFuture.channel().writeAndFlush(msgRefreshAll);
        } else {
            logger.warn("{}: Refreshing status failed. Device is not connected.", deviceId);
        }
    }

    public void dispose() {
        logger.debug("{}: disposed", deviceId);

        synchronized (bootstrap) {
            ScheduledFuture<?> future = reconnectFuture;
            reconnectFuture = null;

            if (future != null) {
                logger.trace("{}/{}: cancel reconnectFuture", deviceId, address);
                future.cancel(true);
            }
        }

        ChannelFuture channelFuture = this.channelFuture;
        this.channelFuture = null;

        if (channelFuture != null) {
            logger.trace("{}/{}: cancel closeFuture", deviceId, address);
            channelFuture.cancel(true);
            channelFuture.channel().closeFuture().cancel(true);

            logger.trace("{}/{}: close channel", deviceId, address);
            channelFuture.channel().close();
        }
    }

    @Override
    public void operationComplete(@NonNullByDefault({}) ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            logger.debug("{}/{}: channel connected", deviceId, address);

            firstRefusal = true;

            Channel channel = channelFuture.channel();

            if (channel != null) {
                channel.closeFuture().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(@NonNullByDefault({}) ChannelFuture channelFuture) {
                        logger.debug("{}/{}: channel closed", deviceId, address);

                        channelFuture.channel().closeFuture().removeListener(this);
                        deviceStatusListener.connectionStatus(false, 0);

                        synchronized (bootstrap) {
                            if (!channelFuture.isCancelled()) {
                                connect();
                            } else {
                                logger.debug("{}/{}: reconnect cancelled", deviceId, address);
                            }
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
                    // No handshake for 3.1/3.3
                    // Some devices seem to initialize their stacks in the wrong order and
                    // requests that come too soon can be either ignored completely or responded
                    // to with a, "not supported" so we suggest that the handler hold off initially.
                    deviceStatusListener.connectionStatus(true, TCP_CONNECT_INITIAL_DELAY);
                }
            }
        } else {
            String cause = channelFuture.cause().getMessage();

            logger.trace("{}/{}: Failed to connect: {}", deviceId, address, cause);

            channelFuture.channel().close();

            synchronized (bootstrap) {
                if (!channelFuture.isCancelled()) {
                    if (cause != null && cause.startsWith("connection timed out")) {
                        connect();
                    } else if (firstRefusal && cause != null && cause.startsWith("Connection refused")) {
                        // Once a battery device is powering up we need to give it time to get its
                        // stack together. Hammering it with connection attempts is not helpful.
                        firstRefusal = false;
                        logger.debug("{}/{}: scheduling initial reconnect", deviceId, address);
                        reconnectFuture = eventLoopGroup.schedule(this::connect, //
                                TCP_CONNECT_INITIAL_INTERVAL, TimeUnit.MILLISECONDS);
                    } else {
                        logger.trace("{}/{}: scheduling reconnect", deviceId, address);
                        reconnectFuture = eventLoopGroup.schedule(this::connect, //
                                TCP_CONNECT_RETRY_INTERVAL, TimeUnit.MILLISECONDS);
                    }
                } else {
                    logger.trace("{}/{}: reconnect cancelled", deviceId, address);
                }
            }
        }
    }
}
