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
package org.openhab.binding.ipcamera.internal.servlet;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The {@link StreamOutput} Streams mjpeg out to a client
 *
 * @author Matthew Skinner - Initial contribution
 * @author Kai Kreuzer - Bound MJPEG stream lifetimes
 */
@NonNullByDefault
public class StreamOutput {
    // Servlet APIs cannot detect a client disconnect while waiting for the next camera frame.
    private static final long FRAME_TIMEOUT_MILLIS = Duration.ofSeconds(30).toMillis();
    private static final byte[] CLOSED = new byte[0];

    public final Logger logger = LoggerFactory.getLogger(getClass());
    private final HttpServletResponse response;
    private final String boundary;
    private String contentType;
    private final ServletOutputStream output;
    private final BlockingQueue<byte[]> fifo = new ArrayBlockingQueue<>(50);
    private final long frameTimeoutMillis;
    private volatile boolean closed;
    private boolean connected = false;
    public boolean isSnapshotBased = false;

    public StreamOutput(HttpServletResponse response) throws IOException {
        this(response, Duration.ofMillis(FRAME_TIMEOUT_MILLIS));
    }

    StreamOutput(HttpServletResponse response, Duration frameTimeout) throws IOException {
        boundary = "thisMjpegStream";
        contentType = "multipart/x-mixed-replace; boundary=" + boundary;
        this.response = response;
        output = response.getOutputStream();
        frameTimeoutMillis = frameTimeout.toMillis();
        isSnapshotBased = true;
    }

    public StreamOutput(HttpServletResponse response, String contentType) throws IOException {
        boundary = "";
        this.contentType = contentType;
        this.response = response;
        output = response.getOutputStream();
        frameTimeoutMillis = FRAME_TIMEOUT_MILLIS;
        if (!contentType.isEmpty()) {
            sendInitialHeaders();
            connected = true;
        }
    }

    public void sendSnapshotBasedFrame(byte[] currentSnapshot) throws IOException {
        String header = "--" + boundary + "\r\n" + "Content-Type: image/jpeg" + "\r\n" + "Content-Length: "
                + currentSnapshot.length + "\r\n\r\n";
        if (!connected) {
            sendInitialHeaders();
            // iOS needs to have two jpgs sent for the picture to appear instantly.
            output.write(header.getBytes());
            output.write(currentSnapshot);
            output.write("\r\n".getBytes());
            connected = true;
        }
        output.write(header.getBytes());
        output.write(currentSnapshot);
        output.write("\r\n".getBytes());
    }

    public void queueFrame(byte[] frame) {
        if (closed) {
            return;
        }
        try {
            fifo.add(frame);
        } catch (IllegalStateException e) {
            logger.debug("FIFO buffer has run out of space: {}", e.getMessage());
            fifo.remove();
            fifo.add(frame);
        }
    }

    public void updateContentType(String contentType) {
        this.contentType = contentType;
        if (!connected) {
            sendInitialHeaders();
            connected = true;
        }
    }

    public void sendFrame() throws IOException, InterruptedException {
        byte[] frame = fifo.poll(frameTimeoutMillis, TimeUnit.MILLISECONDS);
        if (frame == null) {
            throw new SocketTimeoutException("No camera frame received before timeout");
        }
        if (frame == CLOSED) {
            throw new IOException("Stream output was closed");
        }
        if (isSnapshotBased) {
            sendSnapshotBasedFrame(frame);
        } else {
            output.write(frame);
        }
    }

    private void sendInitialHeaders() {
        response.setContentType(contentType);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
    }

    public void close() {
        closed = true;
        fifo.clear();
        fifo.offer(CLOSED);
        try {
            output.close();
        } catch (IOException e) {
        }
    }
}
