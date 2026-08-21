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
package org.openhab.binding.amazonechocontrol.internal.push;

import static org.eclipse.jetty.http.HttpHeader.CONTENT_TYPE;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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
 * @author Martin Littkovsky - Buffer stream data until a message is complete
 */
@NonNullByDefault
public class PushStreamAdapter extends Stream.Listener.Adapter {
    // real messages are a few KiB, the limit is only reached if the boundary never arrives
    static final int MAX_BUFFER_SIZE = 512 * 1024;
    private static final Pattern DASHES_ONLY = Pattern.compile("-+");

    private final Logger logger = LoggerFactory.getLogger(PushStreamAdapter.class);
    private final Gson gson;
    private final Session session;
    private final Listener listener;
    // all mutable state is confined to the stream's serialized listener invocations
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private String boundary = "";
    private byte[] boundaryBytes = new byte[0];
    private boolean failureLogged = false;

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
        if (boundaryStart == -1) {
            logger.warn("Content-type of HTTP/2 stream doesn't contain a boundary: {}", contentType);
            return;
        }
        int boundaryEnd = contentType.indexOf(";", boundaryStart);
        boundary = contentType.substring(boundaryStart + 9, boundaryEnd == -1 ? contentType.length() : boundaryEnd);
        boundaryBytes = boundary.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void onData(@NonNullByDefault({}) Stream stream, @NonNullByDefault({}) DataFrame frame,
            @NonNullByDefault({}) Callback callback) {
        try {
            byte[] contentBuffer = new byte[frame.remaining()];
            frame.getData().get(contentBuffer);
            if (logger.isTraceEnabled()) {
                logger.trace("Received raw data {}", new String(contentBuffer, StandardCharsets.UTF_8));
            }
            if (boundary.isBlank()) {
                logger.debug("Discarding data because boundary is not set");
                return;
            }
            // a message can be split over several DATA frames and a frame can contain several messages,
            // so bytes are collected until the trailing boundary marker completes a part
            buffer.write(contentBuffer, 0, contentBuffer.length);
            processBuffer();
        } catch (RuntimeException e) {
            logger.warn("Exception while processing message", e);
        } finally {
            // completing the callback replenishes the HTTP/2 flow control window,
            // without that the server can't send further data on this long-lived stream
            callback.succeeded();
        }
    }

    private void processBuffer() {
        byte[] data = buffer.toByteArray();
        int consumed = 0;
        int boundaryPos;
        while ((boundaryPos = indexOf(data, boundaryBytes, consumed)) != -1) {
            String part = new String(data, consumed, boundaryPos - consumed, StandardCharsets.UTF_8);
            // the part counts as consumed even if it fails, a bad message must not wedge the stream
            consumed = boundaryPos + boundaryBytes.length;
            try {
                handlePart(part);
            } catch (RuntimeException e) {
                // the content was already logged at TRACE when the frame arrived
                logFailure("Failed to process a message part of {} characters: {}", part.length(), e.toString());
                logger.debug("Processing failure", e);
            }
        }
        if (consumed > 0) {
            buffer.reset();
            buffer.write(data, consumed, data.length - consumed);
        }
        if (buffer.size() > MAX_BUFFER_SIZE) {
            logger.warn("Discarding {} bytes of buffered data that don't contain a message boundary", buffer.size());
            buffer.reset();
        }
    }

    private void handlePart(String part) {
        // the delimiter on the wire may be the boundary parameter itself or "--" + parameter (RFC 2046),
        // splitting at the parameter leaves the extra dashes behind as a line of their own
        List<String> content = part.lines()
                .filter(line -> !line.isBlank() && !DASHES_ONLY.matcher(line.strip()).matches()).toList();
        if (content.isEmpty()) {
            // a bare boundary is a keep-alive that requires a PING response
            logger.debug("Sending ping");
            session.ping(new PingFrame(false), Callback.NOOP);
            return;
        }
        if (content.get(0).equals("Content-Type: application/json")) {
            String json = String.join("", content.subList(1, content.size()));
            PushMessageTO parsedMessage = Objects.requireNonNullElse(gson.fromJson(json, PushMessageTO.class),
                    new PushMessageTO());
            parsedMessage.directive.payload.renderingUpdates.forEach(listener::onPushMessageReceived);
            failureLogged = false;
        } else {
            logFailure("Don't know how to handle a message part of {} characters", part.length());
        }
    }

    // the first failure of a streak is a WARN, repetitions only DEBUG to keep a format change from flooding the log
    private void logFailure(String message, Object... arguments) {
        if (failureLogged) {
            logger.debug(message, arguments);
        } else {
            logger.warn(message, arguments);
            failureLogged = true;
        }
    }

    private static int indexOf(byte[] data, byte[] pattern, int fromIndex) {
        for (int i = fromIndex; i <= data.length - pattern.length; i++) {
            int j = 0;
            while (j < pattern.length && data[i + j] == pattern[j]) {
                j++;
            }
            if (j == pattern.length) {
                return i;
            }
        }
        return -1;
    }

    public interface Listener {
        void onPushMessageReceived(PushMessageTO.RenderingUpdateTO renderingUpdate);
    }
}
