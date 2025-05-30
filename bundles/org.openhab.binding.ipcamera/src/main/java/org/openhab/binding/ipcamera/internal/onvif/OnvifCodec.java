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
package org.openhab.binding.ipcamera.internal.onvif;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.onvif.OnvifConnection.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * The {@link OnvifCodec} is used by Netty to decode Onvif traffic into message Strings.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class OnvifCodec extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String incomingMessage = "";
    private OnvifConnection onvifConnection;
    private RequestType requestType = RequestType.GetStatus;

    OnvifCodec(OnvifConnection onvifConnection) {
        this.onvifConnection = onvifConnection;
    }

    @Override
    public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
        if (msg == null || ctx == null) {
            return;
        }
        try {
            if (msg instanceof HttpResponse response) {
                switch (response.status().code()) {
                    case 200:
                        break;
                    case 400:
                        onvifConnection.processBadRequest(requestType);
                        ctx.close();
                        return;
                    case 401:
                        if (!response.headers().isEmpty()) {
                            for (CharSequence name : response.headers().names()) {
                                for (CharSequence value : response.headers().getAll(name)) {
                                    if ("WWW-Authenticate".equalsIgnoreCase(name.toString())) {
                                        logger.debug(
                                                "ONVIF {} replied with WWW-Authenticate header:{}, camera may require ONVIF Profile-T support.",
                                                requestType, value.toString());
                                    }
                                }
                            }
                        }
                    default:
                        logger.trace("ONVIF {} replied with code {}, the message is {}", requestType,
                                response.status().code(), msg);
                        ctx.close();
                        return;
                }
            }
            if (msg instanceof HttpContent content) {
                incomingMessage += content.content().toString(CharsetUtil.UTF_8);
            }
            if (msg instanceof LastHttpContent) {
                onvifConnection.processReply(requestType, incomingMessage);
                ctx.close();
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(@Nullable ChannelHandlerContext ctx, @Nullable Object evt) throws Exception {
        if (ctx == null) {
            return;
        }
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            logger.debug("IdleStateEvent received for {} : {}", requestType, e.state());
            onvifConnection.setIsConnected(false);
            ctx.close();
        } else {
            logger.debug("ONVIF {} netty channel event occurred: {}", requestType, evt);
        }
    }

    @Override
    public void exceptionCaught(@Nullable ChannelHandlerContext ctx, @Nullable Throwable cause) {
        if (ctx == null || cause == null) {
            return;
        }
        logger.debug("Exception on ONVIF {} connection: {}", requestType, cause.getMessage());
        ctx.close();
    }

    @Override
    public void handlerRemoved(@Nullable ChannelHandlerContext ctx) {
        if (requestType == RequestType.PullMessages) {
            onvifConnection.pullMessageRequests.decrementAndGet();
        }
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
        if (requestType == RequestType.PullMessages) {
            onvifConnection.pullMessageRequests.incrementAndGet();
        }
    }

    public RequestType getRequestType() {
        return requestType;
    }
}
