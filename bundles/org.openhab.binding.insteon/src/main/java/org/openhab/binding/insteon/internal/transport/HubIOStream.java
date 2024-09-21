/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.transport;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger logger = LoggerFactory.getLogger(HubIOStream.class);

    private static final String BS_START = "<BS>";
    private static final String BS_END = "</BS>";

    private String host;
    private int port;
    private String auth;
    private int pollInterval;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    // index of the last byte we have read in the buffer
    private int bufferIdx = -1;

    /**
     * Constructor
     *
     * @param host host name of hub device
     * @param port port to connect to
     * @param username hub user name
     * @param password hub password
     * @param pollInterval hub poll interval (in milliseconds)
     * @param scheduler the scheduler
     */
    public HubIOStream(String host, int port, String username, String password, int pollInterval,
            ScheduledExecutorService scheduler) {
        this.host = host;
        this.port = port;
        this.auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        this.pollInterval = pollInterval;
        this.scheduler = scheduler;
    }

    @Override
    public boolean isOpen() {
        return job != null;
    }

    @Override
    public boolean open() {
        if (isOpen()) {
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
        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }

        InputStream in = this.in;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.debug("failed to close input stream", e);
            }
            this.in = null;
        }

        OutputStream out = this.out;
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                logger.debug("failed to close output stream", e);
            }
            this.out = null;
        }
    }

    /**
     * Fetches the latest status buffer from the Hub
     *
     * @return string with status buffer
     * @throws IOException
     */
    private synchronized String bufferStatus() throws IOException {
        String result = getURL("/buffstatus.xml");

        int start = result.indexOf(BS_START);
        if (start == -1) {
            throw new IOException("malformed bufferstatus.xml");
        }
        start += BS_START.length();

        int end = result.indexOf(BS_END, start);
        if (end == -1) {
            throw new IOException("malformed bufferstatus.xml");
        }

        return result.substring(start, end).trim();
    }

    /**
     * Sends command to Hub to clear the status buffer
     *
     * @throws IOException
     */
    private synchronized void clearBuffer() throws IOException {
        logger.trace("clearing buffer");
        getURL("/1?XB=M=1");
        bufferIdx = 0;
    }

    /**
     * Sends Insteon message (byte array) as a readable ascii string to the Hub
     *
     * @param msg byte array representing the Insteon message
     * @throws IOException in case of I/O error
     */
    public synchronized void write(ByteBuffer msg) throws IOException {
        poll(); // fetch the status buffer before we send out commands

        StringBuilder b = new StringBuilder();
        while (msg.remaining() > 0) {
            b.append(String.format("%02x", msg.get()));
        }
        String hexMsg = b.toString();
        logger.trace("writing a message");
        getURL("/3?" + hexMsg + "=I=3");
        bufferIdx = 0;
    }

    /**
     * Polls the Hub web interface to fetch the status buffer
     *
     * @throws IOException if something goes wrong with I/O
     */
    private synchronized void poll() throws IOException {
        String buffer = bufferStatus(); // fetch via http call
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
            byte[] array = HexUtils.toByteArray(msg.toString());
            ByteBuffer buf = ByteBuffer.wrap(array);
            if (in instanceof HubInputStream hubInput) {
                hubInput.handle(buf);
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
     * @param resource the url
     * @return contents returned by http server
     * @throws IOException
     */
    private String getURL(String resource) throws IOException {
        String url = "http://" + host + ":" + port + resource;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(10000);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setRequestProperty("Authorization", "Basic " + auth);

            logger.trace("getting {}", url);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                if (responseCode == 401) {
                    throw new IOException(
                            "Bad username or password. See the label on the bottom of the hub for the correct login information.");
                } else {
                    throw new IOException(url + " failed with the response code: " + responseCode);
                }
            }

            return getData(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
    }

    private String getData(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }

            String s = baos.toString();
            return s;
        } finally {
            bis.close();
        }
    }

    /**
     * Implements an InputStream for an Insteon Hub 2
     */
    public class HubInputStream extends InputStream {
        // A buffer to keep bytes while we are waiting for the inputstream to read
        private ReadByteBuffer buffer = new ReadByteBuffer(1024);

        public void handle(ByteBuffer b) throws IOException {
            // Make sure we cleanup as much space as possible
            buffer.makeCompact();
            buffer.add(b.array());
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
            flushBuffer();
        }

        @Override
        public void write(byte @Nullable [] b, int off, int len) throws IOException {
            out.write(b, off, len);
            flushBuffer();
        }

        private void flushBuffer() throws IOException {
            ByteBuffer buffer = ByteBuffer.wrap(out.toByteArray());
            HubIOStream.this.write(buffer);
            out.reset();
        }
    }
}
