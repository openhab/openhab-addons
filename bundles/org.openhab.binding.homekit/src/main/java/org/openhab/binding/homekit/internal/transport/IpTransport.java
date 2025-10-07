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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.session.AsymmetricSessionKeys;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser;
import org.openhab.binding.homekit.internal.session.SecureSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final int SOCKET_TIMEOUT = Duration.ofSeconds(5).toMillisPart(); // milliseconds

    private final Logger logger = LoggerFactory.getLogger(IpTransport.class);

    private final String host; // ip address with optional port e.g. "192.168.1.42:9123"
    private final Socket socket;

    private @Nullable SecureSession secureSession = null;

    /**
     * Creates a new IpTransport instance with the given socket and session keys.
     *
     * @param host the IP address and port of the HomeKit accessory
     */
    public IpTransport(String host) throws Exception {
        logger.debug("Connecting to {}", host);
        this.host = host;
        String[] parts = host.split(":");
        if (parts.length < 1) {
            throw new IllegalArgumentException("Missing host: " + host);
        }
        if (parts.length < 2) {
            throw new IllegalArgumentException("Missing port: " + host);
        }
        String ipAddress = parts[0];
        int port = Integer.parseInt(parts[1]);
        socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddress, port), SOCKET_TIMEOUT); // connect timeout
        socket.setSoTimeout(SOCKET_TIMEOUT); // read timeout
        socket.setKeepAlive(false); // HAP spec forbids TCP keepalive
        logger.debug("Connected to {}", host);
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

    private synchronized byte[] execute(String method, String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        try {
            byte[] request = buildRequest(method, endpoint, contentType, content);

            boolean trace = logger.isTraceEnabled();
            if (trace) {
                logger.trace("Request:\n{}", new String(request, StandardCharsets.ISO_8859_1));
            }

            byte[][] response; // 0 = headers, 1 = content, 2 = raw trace (if enabled)
            SecureSession secureSession = this.secureSession;
            if (secureSession != null) {
                secureSession.send(request);
                response = secureSession.receive(trace);
            } else {
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                out.write(request);
                out.flush();
                response = readPlainResponse(in, trace);
            }

            if (response.length != 3) {
                throw new IOException("Response must contain 3 arrays");
            }

            if (trace) {
                logger.trace("Response:\n{}", new String(response[2], StandardCharsets.ISO_8859_1));
            }

            checkHeaders(response[0]);
            return response[1];
        } catch (IOException | InterruptedException | TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * Builds an HTTP request with the given method, endpoint, content type, and content.
     *
     * @param method the HTTP method (e.g., "GET", "POST", "PUT")
     * @param endpoint the endpoint to which the request is sent
     * @param contentType the content type of the request
     * @param content the content of the request
     * @return the complete HTTP request as a byte array
     * @throws IOException if an I/O error occurs
     */
    private byte[] buildRequest(String method, String endpoint, String contentType, byte[] content) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(endpoint).append(" HTTP/1.1\r\n");
        sb.append("Host: ").append(host).append("\r\n");
        sb.append("Accept: ").append(contentType).append("\r\n");
        if (!contentIsEmpty(method)) {
            sb.append("Content-Type: ").append(contentType).append("\r\n");
            sb.append("Content-Length: ").append(content.length).append("\r\n");
        } else {
            sb.append("Content-Length: 0\r\n");
        }
        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        if (contentIsEmpty(method)) {
            return headerBytes;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(headerBytes);
        out.write(content);
        return out.toByteArray();
    }

    private boolean contentIsEmpty(String method) {
        return "GET".equals(method) || "DELETE".equals(method);
    }

    /*
     * Reads a plain (non-secure) HTTP response from the input stream.
     *
     * @param trace if true, captures the raw data for debugging purposes.
     *
     * @return a 3D byte array where the first element is the HTTP headers, the second element is the content,
     * and the third is the raw trace (if enabled).
     *
     * @throws IOException if an I/O error occurs or if the response is invalid.
     */
    private byte[][] readPlainResponse(InputStream in, boolean trace) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int read;
        while ((read = in.read(buf)) != -1) {
            out.write(buf, 0, read);
            if (read < buf.length) {
                break; // crude EOF detection
            }
        }
        byte[] data = out.toByteArray();
        int headersEnd = HttpPayloadParser.indexOfDoubleCRLF(data, 0);
        if (headersEnd < 0) {
            throw new IOException("Invalid HTTP response");
        }
        headersEnd += 4; // move past the \r\n\r\n
        byte[] headers = new byte[headersEnd];
        byte[] content = new byte[data.length - headersEnd];
        System.arraycopy(data, 0, headers, 0, headers.length);
        System.arraycopy(data, headersEnd, content, 0, content.length);
        return new byte[][] { headers, content, trace ? data : new byte[0] };
    }

    /**
     * Checks the HTTP headers for a successful response (status code < 300).
     *
     * @throws IOException if the response indicates an error.
     */
    private void checkHeaders(byte[] headers) throws IOException {
        int httpStatusCode = HttpPayloadParser.getHttpStatusCode(headers);
        if (httpStatusCode >= 300) {
            throw new IOException("HTTP " + httpStatusCode);
        }
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }
}
