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
package org.openhab.binding.amazonechocontrol.internal.push;

import static org.eclipse.jetty.http.HttpHeader.CONTENT_TYPE;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PingFrame;
import org.eclipse.jetty.util.Callback;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushMessageTO;
import org.openhab.binding.amazonechocontrol.internal.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link PushStreamAdapter} handles the HTTP/2 push stream
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class PushStreamAdapter extends Stream.Listener.Adapter {
    private final Logger logger = LoggerFactory.getLogger(PushStreamAdapter.class);
    private final Gson gson;
    private final Session session;
    private final Listener listener;
    private String boundary = "";

    public PushStreamAdapter(Gson gson, Session session, Listener listener) {
        this.gson = gson;
        this.session = session;
        this.listener = listener;
    }

    @Override
    public void onHeaders(@NonNullByDefault({}) Stream stream, @NonNullByDefault({}) HeadersFrame frame) {
        HttpFields headers = frame.getMetaData().getFields();
        if (logger.isTraceEnabled()) {
            logger.trace("Received headers: {}", HttpUtil.logToString(headers));
        }
        String contentType = headers.get(CONTENT_TYPE);
        if (contentType == null || contentType.isBlank()) {
            logger.warn("Headers of HTTP/2 stream don't contain content-type");
            return;
        }
        int boundaryStart = contentType.indexOf("boundary=");
        int boundaryEnd = contentType.indexOf(";", boundaryStart);
        boundary = contentType.substring(boundaryStart + 9, boundaryEnd);
    }

    @Override
    public void onData(@NonNullByDefault({}) Stream stream, @NonNullByDefault({}) DataFrame frame,
            @NonNullByDefault({}) Callback callback) {
        byte[] contentBuffer = new byte[frame.remaining()];
        frame.getData().get(contentBuffer);
        String contentString = new String(contentBuffer);
        logger.trace("Received raw data {}", contentString);

        // process
        try {
            if (boundary.isBlank()) {
                logger.debug("Discarding message because boundary is not set");
                return;
            }
            BufferedReader contentReader = new BufferedReader(new StringReader(contentString));
            List<String> content = contentReader.lines().filter(line -> !line.isBlank()).toList();

            if (content.isEmpty()) {
                return;
            }

            if (!content.get(content.size() - 1).endsWith(boundary)) {
                logger.debug("Discarding incomplete message, boundary not found");
            }

            if (content.size() == 1) {
                // only boundary requires a PING response
                logger.debug("Sending ping");
                session.ping(new PingFrame(false), Callback.NOOP);
            } else if (content.get(0).equals("Content-Type: application/json")) {
                // parse the message
                PushMessageTO parsedMessage = Objects
                        .requireNonNullElse(gson.fromJson(content.get(1), PushMessageTO.class), new PushMessageTO());
                parsedMessage.directive.payload.renderingUpdates.forEach(listener::onPushMessageReceived);
            } else {
                logger.warn("Don't know how to handle frame starting with {}", content.get(0));
            }
        } catch (RuntimeException e) {
            logger.warn("Exception while processing message", e);
        }
    }

    public interface Listener {
        void onPushMessageReceived(PushMessageTO.RenderingUpdateTO renderingUpdate);
    }
}
