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
package org.openhab.binding.insteon.internal.transport.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.insteon.internal.utils.HexUtils;

/**
 * Implements IOStream for an Insteon Hub 2
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 *
 */
@NonNullByDefault
public class HubIOStream extends IOStream {
    private static final String BUFFER_TAG_START = "<BS>";
    private static final String BUFFER_TAG_END = "</BS>";
    private static final int RATE_LIMIT_TIME = 500; // in milliseconds
    private static final int REQUEST_TIMEOUT = 30; // in seconds

    private String host;
    private int port;
    private String auth;
    private int pollInterval;
    private HttpClient httpClient;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    // index of the last byte we have read in the buffer
    private volatile int bufferIdx = -1;

    /**
     * Constructor
     *
     * @param host host name of hub device
     * @param port port to connect to
     * @param username hub user name
     * @param password hub password
     * @param pollInterval hub poll interval (in milliseconds)
     * @param httpClient the http client
     * @param scheduler the scheduler
     */
    public HubIOStream(String host, int port, String username, String password, int pollInterval, HttpClient httpClient,
            ScheduledExecutorService scheduler) {
        super(RATE_LIMIT_TIME);
        this.host = host;
        this.port = port;
        this.auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        this.pollInterval = pollInterval;
        this.httpClient = httpClient;
        this.scheduler = scheduler;
    }

    @Override
    public boolean open() {
        if (job != null) {
            logger.warn("hub stream is already open");
            return false;
        }

        try {
            clearBuffer();
        } catch (IOException e) {
            logger.warn("open failed: {}", e.getMessage());
            return false;
        }

        in = new HubInputStream();
        out = new HubOutputStream();

        job = scheduler.scheduleWithFixedDelay(() -> {
            try {
                poll();
            } catch (IOException e) {
                logger.debug("failed to poll hub", e);
                close();
            }
        }, 0, pollInterval, TimeUnit.MILLISECONDS);

        return true;
    }

    @Override
    public void close() {
        super.close();

        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }
    }

    /**
     * Returns the latest buffer from the Hub
     *
     * @return the buffer string
     * @throws IOException
     */
    private String getBuffer() throws IOException {
        String result = getURL("/buffstatus.xml");

        int start = result.indexOf(BUFFER_TAG_START);
        int end = result.indexOf(BUFFER_TAG_END, start);
        if (start == -1 || end == -1) {
            throw new IOException("malformed buffstatus.xml");
        }
        start += BUFFER_TAG_START.length();

        return result.substring(start, end).trim();
    }

    /**
     * Clears the Hub buffer
     *
     * @throws IOException
     */
    private void clearBuffer() throws IOException {
        logger.trace("clearing buffer");
        getURL("/1?XB=M=1");
        bufferIdx = 0;
    }

    /**
     * Sends a message to the Hub
     *
     * @param b byte array representing the Insteon message
     * @throws IOException
     */
    private void sendMessage(byte[] b) throws IOException {
        poll(); // poll the status buffer before we send the message
        logger.trace("sending a message");
        getURL("/3?" + HexUtils.getHexString(b) + "=I=3");
        bufferIdx = 0;
    }

    /**
     * Polls the Hub buffer and add to input stream
     *
     * @throws IOException
     */
    private void poll() throws IOException {
        String buffer = getBuffer();
        logger.trace("poll: {}", buffer);
        // The Hub maintains a ring buffer where the last two digits (in hex!) represent
        // the position of the last byte read.
        String data = buffer.substring(0, buffer.length() - 2); // pure data w/o index pointer

        int nIdx = -1;
        try {
            nIdx = Integer.parseInt(buffer.substring(buffer.length() - 2, buffer.length()), 16);
        } catch (NumberFormatException e) {
            bufferIdx = -1;
            logger.debug("invalid buffer size received in line: {}", buffer);
            return;
        }

        if (bufferIdx == -1) {
            // this is the first call or first call after error, no need for buffer copying
            bufferIdx = nIdx;
            return;
        }

        if (isClearedBuffer(data)) {
            logger.trace("skip cleared buffer");
            bufferIdx = 0;
            return;
        }

        StringBuilder msg = new StringBuilder();
        if (nIdx < bufferIdx) {
            String msgStart = data.substring(bufferIdx, data.length());
            String msgEnd = data.substring(0, nIdx);
            if (isClearedBuffer(msgStart)) {
                logger.trace("discard cleared buffer wrap around msg start");
                msgStart = "";
            }

            msg.append(msgStart + msgEnd);
            logger.trace("wrap around: copying new data on: {}", msg);
        } else {
            msg.append(data.substring(bufferIdx, nIdx));
            logger.trace("no wrap:      appending new data: {}", msg);
        }
        if (msg.length() != 0) {
            byte[] b = HexUtils.toByteArray(msg.toString());
            if (in instanceof HubInputStream hubInput) {
                hubInput.add(b);
            } else {
                logger.debug("hub input stream is null");
            }
        }
        bufferIdx = nIdx;
    }

    /**
     * Returns if is cleared buffer
     *
     * @param data buffer data to check
     * @return true if all zeros in buffer
     */
    private boolean isClearedBuffer(String data) {
        return "0".repeat(data.length()).equals(data);
    }

    /**
     * Helper method to fetch url from http server
     *
     * @param path the url path
     * @return contents returned by http server
     * @throws IOException
     */
    private String getURL(String path) throws IOException {
        Request request = httpClient.newRequest(host, port).path(path).header(HttpHeader.AUTHORIZATION, "Basic " + auth)
                .timeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);
        logger.trace("getting {}", request.getURI());

        try {
            ContentResponse response = request.send();

            int statusCode = response.getStatus();
            switch (statusCode) {
                case HttpStatus.OK_200:
                    return response.getContentAsString();
                case HttpStatus.UNAUTHORIZED_401:
                    throw new IOException(
                            "Bad username or password. See the label on the bottom of the hub for the correct login information.");
                default:
                    throw new IOException("GET " + request.getURI() + " failed with status code: " + statusCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("GET " + request.getURI() + " interrupted");
        } catch (TimeoutException | ExecutionException e) {
            throw new IOException("GET " + request.getURI() + " failed with error: " + e.getMessage());
        }
    }

    /**
     * Implements an InputStream for an Insteon Hub 2
     */
    public class HubInputStream extends InputStream {
        // A buffer to keep bytes while we are waiting for the inputstream to read
        private ReadByteBuffer buffer = new ReadByteBuffer(1024);

        public void add(byte[] b) throws IOException {
            // Make sure we cleanup as much space as possible
            buffer.makeCompact();
            buffer.add(b);
        }

        @Override
        public int read() throws IOException {
            return buffer.get();
        }

        @Override
        public int read(byte @Nullable [] b, int off, int len) throws IOException {
            Objects.requireNonNull(b);
            return buffer.get(b, off, len);
        }

        @Override
        public void close() throws IOException {
            buffer.close();
        }
    }

    /**
     * Implements an OutputStream for an Insteon Hub 2
     */
    public class HubOutputStream extends OutputStream {
        private ByteArrayOutputStream out = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            flush();
        }

        @Override
        public void write(byte @Nullable [] b, int off, int len) throws IOException {
            out.write(b, off, len);
            flush();
        }

        @Override
        public void flush() throws IOException {
            sendMessage(out.toByteArray());
            out.reset();
        }
    }
}
