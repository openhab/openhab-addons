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
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.session.AsymmetricSessionKeys;
import org.openhab.binding.homekit.internal.session.EventListener;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser;
import org.openhab.binding.homekit.internal.session.SecureSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provides the IP transport layer for HomeKit communication.
 * It provides methods for sending GET, POST, and PUT requests with appropriate headers and content types.
 * It supports both plain and secure (encrypted) communication based on whether session keys have been set.
 * It handles building HTTP requests, sending them over a socket, and parsing HTTP responses.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class IpTransport implements AutoCloseable {

    private static final int TIMEOUT_MILLI_SECONDS = 10000;
    private static final Duration MINIMUM_REQUEST_INTERVAL = Duration.ofMillis(200);

    private final Logger logger = LoggerFactory.getLogger(IpTransport.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "homekit-io"));

    private final Socket socket;
    private final String hostName;
    private final EventListener eventListener;

    private @Nullable SecureSession secureSession = null;
    private @Nullable Thread readThread = null;
    private @Nullable CompletableFuture<byte[][]> readHttpResponseFuture = null;

    private boolean closing = false;
    private Instant earliestNextRequestTime = Instant.MIN;

    /**
     * Creates a new IpTransport instance on the given host.
     *
     * @param ipAddress the IP address and port of the HomeKit accessory
     * @param hostName the fully qualified host name (e.g. 'foobar.local') of the HomeKit accessory
     * @throws IOException
     */
    public IpTransport(String ipAddress, String hostName, EventListener eventListener) throws IOException {
        logger.debug("Connecting to {} alias {}", ipAddress, hostName);
        this.hostName = hostName;
        this.eventListener = eventListener;
        String[] parts = ipAddress.split(":");
        socket = new Socket();
        socket.setKeepAlive(true); // keep-alive forbiddden for accessories but client should use it
        socket.setTcpNoDelay(true); // disable Nagle algorithm to force immediate flushing of packets
        socket.connect(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])), TIMEOUT_MILLI_SECONDS);
        logger.debug("Connected to {} alias {}", ipAddress, hostName);
    }

    public void setSessionKeys(AsymmetricSessionKeys keys) throws IOException {
        logger.trace("setSessionKeys()");
        secureSession = new SecureSession(socket, keys);
        Thread thread = new Thread(this::readTask, "homekit-read");
        readThread = thread;
        thread.start();
        logger.trace("setSessionKeys() {}", secureSession);
    }

    /**
     * Sends a GET request to the specified endpoint with the given content type.
     *
     * @param endpoint the endpoint to which the request is sent
     * @param contentType the content type of the request
     * @return the response content as a byte array
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws TimeoutException if the operation times out
     * @throws ExecutionException if an error occurs during execution
     * @throws IllegalStateException if the state is invalid
     */
    public byte[] get(String endpoint, String contentType)
            throws IOException, InterruptedException, ExecutionException, IllegalStateException, TimeoutException {
        return execute("GET", endpoint, contentType, new byte[0]);
    }

    /**
     * Sends a POST request to the specified endpoint with the given content type and content.
     *
     * @param endpoint the endpoint to which the request is sent
     * @param contentType the content type of the request
     * @param content the content of the request
     * @return the response content as a byte array
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws TimeoutException if the operation times out
     * @throws ExecutionException if an error occurs during execution
     * @throws IllegalStateException if the state is invalid
     */
    public byte[] post(String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, ExecutionException, IllegalStateException, TimeoutException {
        return execute("POST", endpoint, contentType, content);
    }

    /**
     * Sends a PUT request to the specified endpoint with the given content type and content.
     *
     * @param endpoint the endpoint to which the request is sent
     * @param contentType the content type of the request
     * @param content the content of the request
     * @return the response content as a byte array
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws TimeoutException if the operation times out
     * @throws ExecutionException if an error occurs during execution
     * @throws IllegalStateException if the state is invalid
     */
    public byte[] put(String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, ExecutionException, IllegalStateException, TimeoutException {
        return execute("PUT", endpoint, contentType, content);
    }

    /**
     * Executes an HTTP request with the specified method, endpoint, content type, and content.
     *
     * @param method the HTTP method (e.g., "GET", "POST", "PUT")
     * @param endpoint the endpoint to which the request is sent
     * @param contentType the content type of the request
     * @param content the content of the request
     * @return the response content as a byte array
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws TimeoutException if the operation times out
     * @throws ExecutionException if an error occurs during execution
     * @throws IllegalStateException if the state is invalid
     */
    private synchronized byte[] execute(String method, String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, ExecutionException, IllegalStateException, TimeoutException {
        byte[] request = buildRequest(method, endpoint, contentType, content);

        Duration delay = Duration.between(Instant.now(), earliestNextRequestTime);
        if (delay.isPositive()) {
            Thread.sleep(delay.toMillis()); // rate limit the HTTP requests
        }

        boolean trace = logger.isTraceEnabled();
        if (trace) {
            logger.trace("HTTP request:\n{}", new String(request, StandardCharsets.ISO_8859_1));
        }

        byte[][] response; // 0 = headers, 1 = content, 2 = raw trace (if enabled)
        earliestNextRequestTime = Instant.now().plus(MINIMUM_REQUEST_INTERVAL); // assume zero processing time
        if (secureSession instanceof SecureSession secureSession) {
            // before we write request, create CompletableFuture to read response (with a timeout)
            CompletableFuture<byte[][]> readHttpResponseFuture = new CompletableFuture<>();
            this.readHttpResponseFuture = readHttpResponseFuture;
            // create Future to write the request (with a timeout)
            Future<@Nullable Void> writeTask = executor.submit(() -> {
                secureSession.send(request);
                return null;
            });
            // now wait for both write and read to complete
            writeTask.get(TIMEOUT_MILLI_SECONDS, TimeUnit.MILLISECONDS);
            response = readHttpResponseFuture.get(TIMEOUT_MILLI_SECONDS, TimeUnit.MILLISECONDS);
        } else {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            // create Future to write the request (with a timeout)
            Future<@Nullable Void> writeTask = executor.submit(() -> {
                out.write(request);
                out.flush();
                return null;
            });
            // wait for write to complete
            writeTask.get(TIMEOUT_MILLI_SECONDS, TimeUnit.MILLISECONDS);
            // create Future to read the response (with a timeout)
            Future<byte[][]> readTask = executor.submit(() -> readPlainResponse(in, trace));
            // wait for read to complete
            response = readTask.get(TIMEOUT_MILLI_SECONDS, TimeUnit.MILLISECONDS);
        }
        earliestNextRequestTime = Instant.now().plus(MINIMUM_REQUEST_INTERVAL); // allow actual processing time

        if (response.length != 3) {
            throw new IOException("Response must contain 3 arrays");
        }

        if (trace) {
            logger.trace("HTTP response:\n{}", new String(response[2], StandardCharsets.ISO_8859_1));
        }

        checkHeaders(response[0]);
        return response[1];
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
        sb.append("Host: ").append(hostName).append("\r\n");
        if (!contentIsEmpty(method)) {
            sb.append("Content-Length: ").append(content.length).append("\r\n");
            sb.append("Content-Type: ").append(contentType).append("\r\n");
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

    /**
     * Reads a plain (non-secure) HTTP response from the input stream.
     *
     * @param trace if true, captures the raw data for debugging purposes.
     *
     * @return a 3D byte array where the first element is the HTTP headers, the second element is the content,
     *         and the third is the raw trace (if enabled).
     *
     * @throws IOException if an I/O error occurs or if the response is invalid.
     * @throws IllegalStateException if the response is invalid.
     */
    private byte[][] readPlainResponse(InputStream in, boolean trace) throws IOException, IllegalStateException {
        HttpPayloadParser httpParser = new HttpPayloadParser();
        ByteArrayOutputStream raw = trace ? new ByteArrayOutputStream() : null;
        byte[] buf = new byte[4096];
        do {
            int read = in.read(buf, 0, buf.length);
            if (read > 0) {
                byte[] frame = Arrays.copyOf(buf, read);
                if (raw != null) {
                    raw.write(frame);
                }
                httpParser.accept(frame);
            }
        } while (!httpParser.isComplete());
        return new byte[][] { httpParser.getHeaders(), httpParser.getContent(),
                raw != null ? raw.toByteArray() : new byte[0] };
    }

    /**
     * Checks the HTTP headers for a successful response (status code < 300).
     *
     * @throws IOException if the response indicates an error.
     * @throws IllegalStateException if the headers are invalid.
     */
    private void checkHeaders(byte[] headers) throws IOException, IllegalStateException {
        int httpStatusCode = HttpPayloadParser.getHttpStatusCode(headers);
        if (httpStatusCode >= 300) {
            throw new IOException("HTTP " + httpStatusCode);
        }
    }

    @Override
    public void close() {
        closing = true;
        secureSession = null;
        try {
            socket.close();
            if (readThread instanceof Thread thread) {
                thread.interrupt();
                thread.join();
            }
        } catch (IOException | InterruptedException e) {
            // shut down quietly
        }
        readThread = null;
    }

    /**
     * Handles an incoming response message by completing the read future or notifying event listeners.
     *
     * @param response the received response as a 3D byte array
     */
    private void handleResponse(byte[][] response) {
        String headers = new String(response[0], StandardCharsets.ISO_8859_1);
        if (headers.startsWith("HTTP")) {
            if (readHttpResponseFuture instanceof CompletableFuture<byte[][]> future) {
                readHttpResponseFuture = null;
                future.complete(response);
            }
        } else if (headers.startsWith("EVENT")) {
            logger.trace("HTTP event:\n{}", new String(response[2], StandardCharsets.ISO_8859_1));
            String jsonContent = new String(response[1], StandardCharsets.UTF_8);
            eventListener.onEvent(jsonContent);
        } else {
            logger.warn("Unexpected response headers:\n{}", headers);
        }
    }

    /**
     * Listens for incoming response messages and invokes the callback. This method runs in a loop on a
     * thread, receiving responses from the secure session and passing them to the callback until the
     * thread is interrupted, or an error occurs.
     */
    private void readTask() {
        Throwable cause = null;
        do {
            try {
                SecureSession session = secureSession;
                if (session == null) {
                    throw new IllegalStateException("Secure session is null");
                }
                byte[][] response = session.receive(logger.isTraceEnabled());
                handleResponse(response);
            } catch (Exception e) {
                // catch all; capture cause and exit
                cause = e;
                break;
            }
        } while (!Thread.currentThread().isInterrupted());

        if (readHttpResponseFuture instanceof CompletableFuture<byte[][]> future) {
            readHttpResponseFuture = null;
            future.completeExceptionally(cause != null ? cause : new InterruptedException("Listener interrupted"));
        }

        if (cause != null && !closing) {
            logger.debug("Error '{}' while listening for HTTP responses", cause.getMessage(), cause);
        }
    }
}
