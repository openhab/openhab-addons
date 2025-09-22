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
package org.openhab.binding.homekit.internal.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.session.AsymmetricSessionKeys;
import org.openhab.binding.homekit.internal.session.SecureSession;

/**
 * This provides the IP transport layer for HomeKit communication.
 * It provides methods for sending GET, POST, and PUT requests with appropriate headers and content types.
 * It supports both plain and secure (encrypted) communication based on whether session keys have been set.
 * It handles building HTTP requests, sending them over a socket, and parsing HTTP responses.
 * It throws exceptions for various error conditions, including IO issues, timeouts, and non-200 HTTP responses.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class IpTransport implements AutoCloseable {

    private static final int TIMEOUT = Duration.ofSeconds(5).toMillisPart();

    private final String host; // ip address with optional port e.g. "192.168.1.42:9123"
    private final Socket socket;

    private @Nullable SecureSession secureSession = null;

    /**
     * Creates a new IpTransport instance with the given socket and session keys.
     */
    public IpTransport(String host) throws Exception {
        this.host = host;
        String[] parts = host.split(":");
        int port = (parts.length > 1) ? Integer.parseInt(parts[1]) : 80; // default to port 80
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT);
        socket.setKeepAlive(false); // HAP spec forbids TCP keepalive
    }

    public void setSessionKeys(AsymmetricSessionKeys keys) throws Exception {
        secureSession = new SecureSession(socket, keys);
    }

    public byte[] get(String endpoint, String contentType)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        return execute("GET", endpoint, contentType, new byte[0]);
    }

    public byte[] post(String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        return execute("POST", endpoint, contentType, content);
    }

    public byte[] put(String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        return execute("PUT", endpoint, contentType, content);
    }

    private byte[] execute(String method, String endpoint, String contentType, byte[] body)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        try {
            byte[] request = buildRequest(method, endpoint, contentType, body);
            byte[] response;

            SecureSession secureSession = this.secureSession;
            if (secureSession != null) {
                secureSession.send(request);
                response = secureSession.receive();
            } else {
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                out.write(request);
                out.flush();
                response = readPlainResponse(in);
            }

            return parseResponse(response);
        } catch (IOException | InterruptedException | TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    private byte[] buildRequest(String method, String endpoint, String contentType, byte[] body) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(endpoint).append(" HTTP/1.1\r\n");
        sb.append("Host: ").append(host).append("\r\n");
        sb.append("Accept: ").append(contentType).append("\r\n");
        if (!bodyIsEmpty(method)) {
            sb.append("Content-Type: ").append(contentType).append("\r\n");
            sb.append("Content-Length: ").append(body.length).append("\r\n");
        } else {
            sb.append("Content-Length: 0\r\n");
        }
        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        if (bodyIsEmpty(method)) {
            return headerBytes;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(headerBytes);
        out.write(body);
        return out.toByteArray();
    }

    private boolean bodyIsEmpty(String method) {
        return "GET".equals(method) || "DELETE".equals(method);
    }

    private byte[] readPlainResponse(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int read;
        while ((read = in.read(buf)) != -1) {
            out.write(buf, 0, read);
            if (read < buf.length) {
                break; // crude EOF detection
            }
        }
        return out.toByteArray();
    }

    private byte[] parseResponse(byte[] raw) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(raw);
        String statusLine = readLine(in);
        String[] parts = statusLine.split(" ", 3);
        int status = Integer.parseInt(parts[1]);

        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = readLine(in)).isEmpty()) {
            int idx = line.indexOf(':');
            String name = line.substring(0, idx).trim().toLowerCase();
            String value = line.substring(idx + 1).trim();
            headers.put(name, value);
        }

        if (status != 200) {
            throw new IOException("HTTP " + status);
        }

        int len = Integer.parseInt(headers.getOrDefault("content-length", "0"));
        return in.readNBytes(len);
    }

    private static String readLine(ByteArrayInputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = in.read()) >= 0) {
            if (ch == '\r') {
                continue;
            }
            if (ch == '\n') {
                break;
            }
            sb.append((char) ch);
        }
        return sb.toString();
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }
}
