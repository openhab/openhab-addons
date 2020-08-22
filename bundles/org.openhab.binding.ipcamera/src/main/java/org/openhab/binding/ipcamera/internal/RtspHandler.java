/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.ipcamera.internal;

import java.net.InetSocketAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.rtsp.RtspDecoder;
import io.netty.handler.codec.rtsp.RtspEncoder;
import io.netty.handler.codec.rtsp.RtspHeaderNames;
import io.netty.handler.codec.rtsp.RtspMethods;
import io.netty.handler.codec.rtsp.RtspVersions;
import io.netty.util.CharsetUtil;

/**
 * The {@link RtspHandler} is a WIP and is currently not used. Will be able to report what format the stream is in mjpeg
 * or h264/5.
 *
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class RtspHandler extends ChannelDuplexHandler {
    @Nullable
    private Bootstrap rtspBootstrap;
    private EventLoopGroup mainEventLoopGroup = new NioEventLoopGroup();
    private String ipAddress = "todo";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    RtspHandler() {
        // todo may need to pass in IP of camera plus a few others to make this code
        // work again.
    }

    public HttpRequest getRTSPoptions(String rtspURL) {
        HttpRequest request = new DefaultHttpRequest(RtspVersions.RTSP_1_0, RtspMethods.OPTIONS, rtspURL);
        request.headers().add(RtspHeaderNames.CSEQ, "1");
        return request;
    }

    public HttpRequest getRTSPdescribe(String rtspURL) {
        HttpRequest request = new DefaultHttpRequest(RtspVersions.RTSP_1_0, RtspMethods.DESCRIBE, rtspURL);
        request.headers().add(RtspHeaderNames.CSEQ, "2");
        return request;
    }

    public HttpRequest getRTSPsetup(String rtspURL) {
        HttpRequest request = new DefaultHttpRequest(RtspVersions.RTSP_1_0, RtspMethods.SETUP, rtspURL);
        request.headers().add(RtspHeaderNames.CSEQ, "3");
        request.headers().add(RtspHeaderNames.TRANSPORT, "RTP/AVP;unicast;client_port=5000-5001");
        return request;
    }

    public HttpRequest getRTSPplay(String rtspURL) {
        HttpRequest request = new DefaultHttpRequest(RtspVersions.RTSP_1_0, RtspMethods.PLAY, rtspURL);
        request.headers().add(RtspHeaderNames.CSEQ, "4");
        request.headers().add(RtspHeaderNames.SESSION, "12345678"); // need session number to match that of setup
        return request;
    }

    @SuppressWarnings("null")
    public void setupRTSP() {
        if (rtspBootstrap == null) {
            rtspBootstrap = new Bootstrap();
            rtspBootstrap.group(mainEventLoopGroup);
            rtspBootstrap.channel(NioSocketChannel.class);
            rtspBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            rtspBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4500);
            rtspBootstrap.option(ChannelOption.SO_SNDBUF, 1024 * 8);
            rtspBootstrap.option(ChannelOption.SO_RCVBUF, 1024 * 1024);
            rtspBootstrap.option(ChannelOption.TCP_NODELAY, true);
            rtspBootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {
                    // socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(18,
                    // 0, 0));
                    socketChannel.pipeline().addLast("RtspDecoder", new RtspDecoder());
                    socketChannel.pipeline().addLast("RtspEncoder", new RtspEncoder());
                    socketChannel.pipeline().addLast("myRTSPHandler", new RtspHandler());
                }
            });
        }

        ChannelFuture chFuture = rtspBootstrap.connect(new InetSocketAddress(ipAddress, 554));
        chFuture.awaitUninterruptibly(); // ChannelOption.CONNECT_TIMEOUT_MILLIS means this will not hang here
        if (!chFuture.isSuccess()) {
            logger.debug("!!!! RTSP could not open channel.");
        }
        Channel ch = chFuture.channel();

        ch.writeAndFlush(getRTSPoptions(
                "rtsp://192.168.xx.xx:554/cam/realmonitor?channel=1&subtype=1&unicast=true&proto=Onvif/"));
        // returns this:
        // RTSP/1.0 200 OK
        // CSeq: 1
        // Server: Rtsp Server/3.0
        // Public: OPTIONS, DESCRIBE, ANNOUNCE, SETUP, PLAY, RECORD, PAUSE, TEARDOWN,
        // SET_PARAMETER, GET_PARAMETER

        // ch.writeAndFlush(getRTSPdescribe(rtspUri));
        // returns this:
        // RTSP/1.0 200 OK
        // CSeq: 2
        // x-Accept-Dynamic-Rate: 1
        // Content-Base:
        // rtsp://192.168.xx.xx:554/cam/realmonitor?channel=1&subtype=1&unicast=true&proto=Onvif/
        // Cache-Control: must-revalidate
        // Content-Length: 582
        // Content-Type: application/sdp

        // ch.writeAndFlush(getRTSPsetup(rtspUri));
        // ch.writeAndFlush(getRTSPplay(rtspUri));

        // Cleanup
        chFuture = null;
    }

    @Override
    public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
        if (msg == null) {
            return;
        }

        logger.info("{}", msg.toString());

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            String detail = content.content().toString(CharsetUtil.UTF_8);
            logger.info("detail is {}", detail);
        }
    }

    @Override
    public void channelReadComplete(@Nullable ChannelHandlerContext ctx) {
    }

    @Override
    public void handlerAdded(@Nullable ChannelHandlerContext ctx) {
        logger.debug("RTSP handler just created now");
    }

    @Override
    public void handlerRemoved(@Nullable ChannelHandlerContext ctx) {
        logger.debug("RTSP handler removed just now");
    }

    @Override
    public void exceptionCaught(@Nullable ChannelHandlerContext ctx, @Nullable Throwable cause) {
    }

    @Override
    public void userEventTriggered(@Nullable ChannelHandlerContext ctx, @Nullable Object evt) throws Exception {

    }
}
