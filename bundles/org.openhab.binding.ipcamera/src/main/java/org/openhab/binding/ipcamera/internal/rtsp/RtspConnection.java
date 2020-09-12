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
package org.openhab.binding.ipcamera.internal.rtsp;

import java.net.InetSocketAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.rtsp.RtspDecoder;
import io.netty.handler.codec.rtsp.RtspEncoder;
import io.netty.handler.codec.rtsp.RtspHeaderNames;
import io.netty.handler.codec.rtsp.RtspMethods;
import io.netty.handler.codec.rtsp.RtspVersions;

/**
 * The {@link RtspConnection} is a WIP and not currently used, but will talk directly to RTSP and collect information
 * about the camera and streams.
 *
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class RtspConnection {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private @Nullable Bootstrap rtspBootstrap;
    private EventLoopGroup mainEventLoopGroup = new NioEventLoopGroup();
    private IpCameraHandler ipCameraHandler;

    RtspConnection(IpCameraHandler ipCameraHandler) {
        this.ipCameraHandler = ipCameraHandler;
    }

    public void connect() {
        sendRtspRequest(getRTSPoptions());
    }

    public void processMessage(String string) {
        // TODO Auto-generated method stub
        // Handle following replies

        // (getRTSPoptions());
        // returns this:
        // RTSP/1.0 200 OK
        // CSeq: 1
        // Server: Rtsp Server/3.0
        // Public: OPTIONS, DESCRIBE, ANNOUNCE, SETUP, PLAY, RECORD, PAUSE, TEARDOWN,
        // SET_PARAMETER, GET_PARAMETER

        // (getRTSPdescribe());
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
    }

    HttpRequest getRTSPoptions() {
        HttpRequest request = new DefaultHttpRequest(RtspVersions.RTSP_1_0, RtspMethods.OPTIONS,
                ipCameraHandler.rtspUri);
        request.headers().add(RtspHeaderNames.CSEQ, "1");
        return request;
    }

    HttpRequest getRTSPdescribe() {
        HttpRequest request = new DefaultHttpRequest(RtspVersions.RTSP_1_0, RtspMethods.DESCRIBE,
                ipCameraHandler.rtspUri);
        request.headers().add(RtspHeaderNames.CSEQ, "2");
        return request;
    }

    HttpRequest getRTSPsetup() {
        HttpRequest request = new DefaultHttpRequest(RtspVersions.RTSP_1_0, RtspMethods.SETUP, ipCameraHandler.rtspUri);
        request.headers().add(RtspHeaderNames.CSEQ, "3");
        request.headers().add(RtspHeaderNames.TRANSPORT, "RTP/AVP;unicast;client_port=5000-5001");
        return request;
    }

    HttpRequest getRTSPplay() {
        HttpRequest request = new DefaultHttpRequest(RtspVersions.RTSP_1_0, RtspMethods.PLAY, ipCameraHandler.rtspUri);
        request.headers().add(RtspHeaderNames.CSEQ, "4");
        request.headers().add(RtspHeaderNames.SESSION, "12345678"); // need session number to match that of setup
        return request;
    }

    private RtspConnection getHandle() {
        return this;
    }

    @SuppressWarnings("null")
    public void sendRtspRequest(HttpRequest request) {
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
                    socketChannel.pipeline().addLast(new RtspDecoder());
                    socketChannel.pipeline().addLast(new RtspEncoder());
                    socketChannel.pipeline().addLast(new NettyRtspHandler(getHandle()));
                }
            });
        }

        rtspBootstrap.connect(new InetSocketAddress(ipCameraHandler.ipAddress, 554))
                .addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(@Nullable ChannelFuture future) {
                        if (future == null) {
                            return;
                        }
                        if (future.isDone() && future.isSuccess()) {
                            Channel ch = future.channel();
                            ch.writeAndFlush(request);
                        } else { // an error occured
                            logger.debug("Could not reach cameras rtsp on port 554.");
                        }
                    }
                });
    }
}
