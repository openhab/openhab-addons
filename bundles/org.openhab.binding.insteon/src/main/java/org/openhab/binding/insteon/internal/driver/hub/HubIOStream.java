/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.driver.hub;

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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.driver.IOStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements IOStream for a Hub 2014 device
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 *
 */
@NonNullByDefault
public class HubIOStream extends IOStream implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(HubIOStream.class);

    private static final String BS_START = "<BS>";
    private static final String BS_END = "</BS>";

    /** time between polls (in milliseconds */
    private int pollTime = 1000;

    private String baseUrl;
    private @Nullable String auth = null;

    private @Nullable Thread pollThread = null;

    // index of the last byte we have read in the buffer
    private int bufferIdx = -1;

    private boolean polling;

    /**
     * Constructor for HubIOStream
     *
     * @param host host name of hub device
     * @param port port to connect to
     * @param pollTime time between polls (in milliseconds)
     * @param user hub user name
     * @param pass hub password
     */
    public HubIOStream(String host, int port, int pollTime, @Nullable String user, @Nullable String pass) {
        this.pollTime = pollTime;

        StringBuilder s = new StringBuilder();
        s.append("http://");
        s.append(host);
        if (port != -1) {
            s.append(":").append(port);
        }
        baseUrl = s.toString();

        if (user != null && pass != null) {
            auth = "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public boolean open() {
        try {
            clearBuffer();
        } catch (IOException e) {
            logger.warn("open failed: {}", e.getMessage());
            return false;
        }

        in = new HubInputStream();
        out = new HubOutputStream();

        polling = true;
        pollThread = new Thread(this);
        setParamsAndStart(pollThread);

        return true;
    }

    private void setParamsAndStart(@Nullable Thread thread) {
        if (thread != null) {
            thread.setName("OH-binding-" + InsteonBindingConstants.BINDING_ID + "-hubPoller");
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void close() {
        polling = false;

        if (pollThread != null) {
            pollThread = null;
        }

        InputStream in = this.in;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.warn("failed to close input stream", e);
            }
            this.in = null;
        }

        OutputStream out = this.out;
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                logger.warn("failed to close output stream", e);
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
        String hexMSG = b.toString();
        logger.trace("writing a message");
        getURL("/3?" + hexMSG + "=I=3");
        bufferIdx = 0;
    }

    /**
     * Polls the Hub web interface to fetch the status buffer
     *
     * @throws IOException if something goes wrong with I/O
     */
    public synchronized void poll() throws IOException {
        String buffer = bufferStatus(); // fetch via http call
        logger.trace("poll: {}", buffer);
        //
        // The Hub maintains a ring buffer where the last two digits (in hex!) represent
        // the position of the last byte read.
        //
        String data = buffer.substring(0, buffer.length() - 2); // pure data w/o index pointer

        int nIdx = -1;
        try {
            nIdx = Integer.parseInt(buffer.substring(buffer.length() - 2, buffer.length()), 16);
        } catch (NumberFormatException e) {
            bufferIdx = -1;
            logger.warn("invalid buffer size received in line: {}", buffer);
            return;
        }

        if (bufferIdx == -1) {
            // this is the first call or first call after error, no need for buffer copying
            bufferIdx = nIdx;
            return; // XXX why return here????
        }

        if (allZeros(data)) {
            logger.trace("skip cleared buffer");
            bufferIdx = 0;
            return;
        }

        StringBuilder msg = new StringBuilder();
        if (nIdx < bufferIdx) {
            String msgStart = data.substring(bufferIdx, data.length());
            String msgEnd = data.substring(0, nIdx);
            if (allZeros(msgStart)) {
                logger.trace("discard cleared buffer wrap around msg start");
                msgStart = "";
            }

            msg.append(msgStart + msgEnd);
            logger.trace("wrap around: copying new data on: {}", msg.toString());
        } else {
            msg.append(data.substring(bufferIdx, nIdx));
            logger.trace("no wrap:      appending new data: {}", msg.toString());
        }
        if (msg.length() != 0) {
            ByteBuffer buf = ByteBuffer.wrap(hexStringToByteArray(msg.toString()));
            InputStream in = this.in;
            if (in != null) {
                ((HubInputStream) in).handle(buf);
            } else {
                logger.warn("in is null");
            }
        }
        bufferIdx = nIdx;
    }

    private boolean allZeros(String s) {
        return "0".repeat(s.length()).equals(s);
    }

    /**
     * Helper method to fetch url from http server
     *
     * @param resource the url
     * @return contents returned by http server
     * @throws IOException
     */
    private String getURL(String resource) throws IOException {
        String url = baseUrl + resource;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(10000);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            if (auth != null) {
                connection.setRequestProperty("Authorization", auth);
            }

            logger.debug("getting {}", url);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                if (responseCode == 401) {
                    logger.warn(
                            "Bad username or password. See the label on the bottom of the hub for the correct login information.");
                    throw new IOException("login credentials are incorrect");
                } else {
                    String message = url + " failed with the response code: " + responseCode;
                    logger.warn(message);
                    throw new IOException(message);
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
     * Entry point for thread
     */
    @Override
    public void run() {
        while (polling) {
            try {
                poll();
            } catch (IOException e) {
                logger.warn("got exception while polling: {}", e.toString());
            }
            try {
                Thread.sleep(pollTime);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Helper function to convert an ascii hex string (received from hub)
     * into a byte array
     *
     * @param s string received from hub
     * @return simple byte array
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return bytes;
    }

    /**
     * Implements an InputStream for the Hub 2014
     *
     * @author Daniel Pfrommer - Initial contribution
     *
     */
    public class HubInputStream extends InputStream {

        // A buffer to keep bytes while we are waiting for the inputstream to read
        private ReadByteBuffer buffer = new ReadByteBuffer(1024);

        public HubInputStream() {
        }

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
            return buffer.get(b, off, len);
        }

        @Override
        public void close() throws IOException {
            buffer.done();
        }
    }

    /**
     * Implements an OutputStream for the Hub 2014
     *
     * @author Daniel Pfrommer - Initial contribution
     *
     */
    public class HubOutputStream extends OutputStream {
        private ByteArrayOutputStream out = new ByteArrayOutputStream();

        @Override
        public void write(int b) {
            out.write(b);
            flushBuffer();
        }

        @Override
        public void write(byte @Nullable [] b, int off, int len) {
            out.write(b, off, len);
            flushBuffer();
        }

        private void flushBuffer() {
            ByteBuffer buffer = ByteBuffer.wrap(out.toByteArray());
            try {
                HubIOStream.this.write(buffer);
            } catch (IOException e) {
                logger.warn("failed to write to hub: {}", e.toString());
            }
            out.reset();
        }
    }
}
