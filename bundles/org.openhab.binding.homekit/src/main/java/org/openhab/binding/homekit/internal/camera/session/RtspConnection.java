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
package org.openhab.binding.homekit.internal.camera.session;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An RTSP client connection to go2rtc, supporting request/response exchanges and TCP interleaved media frames.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class RtspConnection implements AutoCloseable {

    private static final int CONNECT_TIMEOUT_MS = 2000;
    private static final int RESPONSE_TIMEOUT_MS = 5000;
    private static final int MAX_LINE_LENGTH = 4096;
    private static final int MAX_BODY_LENGTH = 65536;

    /**
     * The body of a successful response.
     */
    public record Response(String body) {
    }

    /**
     * The header of an interleaved media frame. Its payload is in the buffer passed to {@link #readFrame}.
     */
    public record Frame(int channel, int length) {
    }

    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private int cseq = 0;
    private @Nullable String session = null;

    public RtspConnection(String host, int port) throws IOException {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(RESPONSE_TIMEOUT_MS);
            socket.setTcpNoDelay(true);
            in = new BufferedInputStream(socket.getInputStream());
            out = socket.getOutputStream();
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }

    /**
     * Sends a request and waits for its response.
     *
     * @param method the RTSP method, for example ANNOUNCE
     * @param url the request URL
     * @param headers additional header lines, each terminated by CRLF
     * @param body the request body, or null for none
     * @return the response
     * @throws IOException if the connection fails or the response status is not 200
     */
    public synchronized Response request(String method, String url, String headers, @Nullable String body)
            throws IOException {
        cseq++;
        StringBuilder message = new StringBuilder();
        message.append(method).append(' ').append(url).append(" RTSP/1.0\r\n");
        message.append("CSeq: ").append(cseq).append("\r\n");
        String currentSession = session;
        if (currentSession != null) {
            message.append("Session: ").append(currentSession).append("\r\n");
        }
        message.append(headers);
        byte[] payload = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        if (payload.length > 0) {
            message.append("Content-Length: ").append(payload.length).append("\r\n");
        }
        message.append("\r\n");
        out.write(message.toString().getBytes(StandardCharsets.UTF_8));
        out.write(payload);
        out.flush();

        String statusLine = readLine();
        Map<String, String> responseHeaders = readHeaders();
        String responseBody = readBody(responseHeaders);
        String responseSession = responseHeaders.get("session");
        if (responseSession != null) {
            session = responseSession.split(";")[0].trim();
        }
        if (!statusLine.startsWith("RTSP/1.0 200 ")) {
            throw new IOException(method + " failed: " + statusLine);
        }
        return new Response(responseBody);
    }

    /**
     * Reads the next interleaved media frame into the buffer. RTSP messages that arrive between frames are skipped.
     *
     * @param buffer receives the frame payload
     * @return the frame channel and payload length
     * @throws IOException if the connection fails or the frame does not fit in the buffer
     */
    public Frame readFrame(byte[] buffer) throws IOException {
        while (true) {
            int first = in.read();
            if (first < 0) {
                throw new EOFException("RTSP connection closed");
            }
            if (first == '$') {
                int channel = in.read();
                int high = in.read();
                int low = in.read();
                if (channel < 0 || high < 0 || low < 0) {
                    throw new EOFException("RTSP connection closed");
                }
                int length = (high << 8) | low;
                if (length > buffer.length) {
                    throw new IOException("RTP packet too large: " + length);
                }
                readFully(buffer, length);
                return new Frame(channel, length);
            }
            readLine();
            readBody(readHeaders());
        }
    }

    /**
     * Sends an interleaved media frame.
     */
    public synchronized void sendFrame(int channel, byte[] data, int length) throws IOException {
        out.write(new byte[] { '$', (byte) channel, (byte) (length >> 8), (byte) length });
        out.write(data, 0, length);
        out.flush();
    }

    /**
     * Removes the read timeout, so that a media stream may stay silent indefinitely. Closing the connection still
     * unblocks a pending read.
     */
    public void disableReadTimeout() throws SocketException {
        socket.setSoTimeout(0);
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            // closing a socket that is already closed has no effect
        }
    }

    private void readFully(byte[] buffer, int length) throws IOException {
        int offset = 0;
        while (offset < length) {
            int count = in.read(buffer, offset, length - offset);
            if (count < 0) {
                throw new EOFException("RTSP connection closed");
            }
            offset += count;
        }
    }

    private String readLine() throws IOException {
        StringBuilder line = new StringBuilder();
        int c = in.read();
        while (c != '\n') {
            if (c < 0) {
                throw new EOFException("RTSP connection closed");
            }
            if (line.length() >= MAX_LINE_LENGTH) {
                throw new IOException("RTSP line too long");
            }
            line.append((char) c);
            c = in.read();
        }
        if (line.length() > 0 && line.charAt(line.length() - 1) == '\r') {
            line.setLength(line.length() - 1);
        }
        return line.toString();
    }

    private Map<String, String> readHeaders() throws IOException {
        Map<String, String> headers = new HashMap<>();
        for (String line = readLine(); !line.isEmpty(); line = readLine()) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                headers.put(line.substring(0, colon).trim().toLowerCase(Locale.ROOT), line.substring(colon + 1).trim());
            }
        }
        return headers;
    }

    private String readBody(Map<String, String> headers) throws IOException {
        String value = headers.get("content-length");
        if (value == null) {
            return "";
        }
        int length;
        try {
            length = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IOException("invalid Content-Length: " + value, e);
        }
        if (length < 0 || length > MAX_BODY_LENGTH) {
            throw new IOException("invalid Content-Length: " + value);
        }
        byte[] body = new byte[length];
        readFully(body, length);
        return new String(body, StandardCharsets.UTF_8);
    }
}
