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
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.HomekitBindingConstants;
import org.openhab.binding.homekit.internal.session.AsymmetricSessionKeys;
import org.openhab.binding.homekit.internal.session.EventListener;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser.HttpPayload;
import org.openhab.binding.homekit.internal.session.HttpReaderListener;
import org.openhab.binding.homekit.internal.session.SecureSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provides the IP transport layer for HomeKit communication.
 * It provides methods for sending GET, POST, and PUT requests with appropriate headers and content types.
 * It supports both plain and secure (encrypted) communication based on whether session keys have been set.
 * It handles building HTTP requests, sending them over a socket, and parsing HTTP responses. It uses a
 * single thread executor for outputting HTTP requests to the socket in plain or encrypted format.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class IpTransport implements AutoCloseable, HttpReaderListener {

    private static final int TIMEOUT_MILLI_SECONDS = 15000; // HomeKit spec expects "around 10 seconds" so be safe
    private static final Duration MINIMUM_REQUEST_INTERVAL = Duration.ofMillis(250);
    private static final int SOCKET_READ_TIMEOUT_MILLI_SECONDS = 500;

    private final Logger logger = LoggerFactory.getLogger(IpTransport.class);
    private final Socket socket;
    private final String hostName;
    private final String ipAddress;
    private final EventListener eventListener;
    private final ExecutorService outputThreadExecutor;
    private final AtomicReference<@Nullable CompletableFuture<HttpPayload>> currentResponseFuture = new AtomicReference<>();

    private volatile HttpPayloadParser httpPayloadParser;
    private volatile @Nullable SecureSession secureSession = null;
    private volatile OutputStream outputStream;

    private Instant earliestNextRequestTime = Instant.MIN;
    private boolean closing;

    /**
     * Creates a new IpTransport instance on the given host.
     *
     * @param ipAddress the IP address and port of the HomeKit accessory
     * @param hostName the fully qualified host name (e.g. 'foobar._hap._tcp.local') of the HomeKit accessory
     * @throws IOException
     */
    public IpTransport(String ipAddress, String hostName, EventListener eventListener) throws IOException {
        logger.debug("Connecting to {} alias {}", ipAddress, hostName);
        this.hostName = hostName;
        this.ipAddress = ipAddress;
        this.eventListener = eventListener;

        outputThreadExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "OH-binding-" + HomekitBindingConstants.BINDING_ID + "-ip-transport-output");
            t.setDaemon(true);
            return t;
        });

        String[] parts = ipAddress.split(":");
        socket = new Socket();
        socket.setKeepAlive(true); // keep-alive forbidden for accessories but client should use it
        socket.setTcpNoDelay(true); // disable Nagle algorithm to force immediate flushing of packets
        socket.setSoTimeout(SOCKET_READ_TIMEOUT_MILLI_SECONDS); // allow socket to be interruptible
        socket.connect(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])), TIMEOUT_MILLI_SECONDS);

        outputStream = socket.getOutputStream();

        httpPayloadParser = new HttpPayloadParser(socket.getInputStream(), this);
        httpPayloadParser.start();
        logger.debug("Connected to {} alias {}", ipAddress, hostName);
    }

    /**
     * Sets the session keys for secure communication.
     * This switches the parser to read from the encrypted input stream.
     *
     * @param keys the asymmetric session keys for encryption/decryption
     * @throws IOException
     * @throws IllegalStateException if the secure session is already set
     */
    public void setSessionKeys(AsymmetricSessionKeys keys) throws IOException {
        logger.trace("setSessionKeys()");
        if (secureSession != null) {
            throw new IllegalStateException("Secure session already set");
        }

        HttpPayloadParser oldParser = httpPayloadParser;
        try {
            oldParser.close(); // blocks until oldParser input thread is really finished
        } catch (IOException ignored) {
        }

        flushSocketAvailableBytes();

        SecureSession newSession = new SecureSession(socket, keys);
        secureSession = newSession;

        outputStream = newSession.getOutputStream();

        HttpPayloadParser newParser = new HttpPayloadParser(newSession.getInputStream(), this);
        httpPayloadParser = newParser;
        newParser.start(); // blocks until input thread is fully running

        logger.trace("setSessionKeys() {}", newSession);
    }

    /**
     * Sends a GET request to the specified end-point with the given content type.
     */
    public byte[] get(String endpoint, String contentType)
            throws IOException, InterruptedException, ExecutionException, IllegalStateException, TimeoutException {
        return execute("GET", endpoint, contentType, new byte[0]);
    }

    /**
     * Sends a POST request to the specified end-point with the given content type and content.
     */
    public byte[] post(String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, ExecutionException, IllegalStateException, TimeoutException {
        return execute("POST", endpoint, contentType, content);
    }

    /**
     * Sends a PUT request to the specified end-point with the given content type and content.
     */
    public byte[] put(String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, ExecutionException, IllegalStateException, TimeoutException {
        return execute("PUT", endpoint, contentType, content);
    }

    /**
     * Executes an HTTP request with the specified method, end-point, content type, and content.
     * Note: for thread safety only one request may be in flight at a time
     */
    private synchronized byte[] execute(String method, String endpoint, String contentType, byte[] content)
            throws IOException, InterruptedException, ExecutionException, IllegalStateException, TimeoutException {
        CompletableFuture<HttpPayload> responseFuture = new CompletableFuture<>();
        if (!currentResponseFuture.compareAndSet(null, responseFuture)) {
            throw new IllegalStateException("Another HTTP request is already in flight");
        }

        try {
            byte[] request = buildRequest(method, endpoint, contentType, content);

            Duration delay = Duration.between(Instant.now(), earliestNextRequestTime);
            if (delay.isPositive()) {
                Thread.sleep(delay.toMillis()); // rate limit the HTTP requests
            }

            Future<@Nullable Exception> writeFuture = outputThreadExecutor.submit(() -> {
                try {
                    outputStream.write(request);
                    outputStream.flush();
                    return null;
                } catch (Exception e) {
                    return e; // returned by get() below
                }
            });
            if (writeFuture.get(TIMEOUT_MILLI_SECONDS, TimeUnit.MILLISECONDS) instanceof Exception e) {
                throw new IOException("HTTP write error", e); // exception cause logging gets deferred to the caller
            }
            if (logger.isTraceEnabled()) {
                logger.trace("{} sent:\n{}", ipAddress, new String(request, StandardCharsets.ISO_8859_1));
            }

            HttpPayload response = responseFuture.get(TIMEOUT_MILLI_SECONDS, TimeUnit.MILLISECONDS);
            earliestNextRequestTime = Instant.now().plus(MINIMUM_REQUEST_INTERVAL); // allow actual processing time

            checkHeaders(response.headers());
            return response.content();
        } catch (InterruptedException e) { // note: for all exceptions the cause logging gets deferred to the caller
            if (!closing) {
                logger.debug("{} Interrupted exception", ipAddress, e);
            }
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            currentResponseFuture.set(null);
        }
    }

    /**
     * Builds an HTTP request with the given method, end-point, content type, and content.
     */
    private byte[] buildRequest(String method, String endpoint, String contentType, byte[] content) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(endpoint).append(" HTTP/1.1\r\n");
        if (!hostName.isBlank()) {
            sb.append("Host: ").append(hostName).append("\r\n");
        }
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

    /**
     * Checks if the HTTP method implies an empty content.
     */
    private boolean contentIsEmpty(String method) {
        return "GET".equals(method) || "DELETE".equals(method);
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
    public synchronized void close() {
        closing = true;
        secureSession = null;
        try {
            socket.close();
        } catch (IOException e) {
            // shut down quietly
        }
        try {
            httpPayloadParser.close();
        } catch (IOException e) {
            // shut down quietly
        }
        outputThreadExecutor.shutdownNow();
        try {
            if (!outputThreadExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                logger.debug("Executor did not terminate promptly");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Forwards the received HTTP payload to the appropriate handler based on its type.
     *
     * @param httpPayload the received HTTP payload
     */
    @Override
    public void onHttpPayload(HttpPayload httpPayload) {
        String headers = new String(httpPayload.headers(), StandardCharsets.ISO_8859_1);
        if (logger.isTraceEnabled()) { // don't expand content trace string if not needed
            logger.trace("{} received:\n{}{}", ipAddress, headers,
                    new String(httpPayload.content(), StandardCharsets.ISO_8859_1));
        }
        if (headers.startsWith("HTTP")) { // deliver HTTP responses to execute()
            CompletableFuture<HttpPayload> future = currentResponseFuture.get();
            if (future != null) {
                future.complete(httpPayload);
            } else {
                logger.debug("{} received HTTP response outside an HTTP response window", ipAddress);
            }
        } else if (headers.startsWith("EVENT")) { // deliver EVENT messages directly to listener
            if (currentResponseFuture.get() != null) {
                logger.debug("{} received EVENT within an HTTP response window", ipAddress);
            }
            String jsonContent = new String(httpPayload.content(), StandardCharsets.UTF_8);
            eventListener.onEvent(jsonContent);
        } else {
            logger.warn("Unexpected response:\n{}{}", headers,
                    new String(httpPayload.content(), StandardCharsets.ISO_8859_1));
        }
    }

    @Override
    public void onHttpReaderError(Throwable error) {
        if (!closing) {
            CompletableFuture<HttpPayload> future = currentResponseFuture.get();
            if (future != null && !future.isDone()) {
                future.completeExceptionally(error); // exception cause logging gets deferred to the caller
            } else {
                logger.debug("{} HTTP reader error", ipAddress, error); // otherwise it gets logged here
            }
        }
    }

    @Override
    public void onHttpReaderClose(byte[] remainingData) {
        if (!closing) {
            if (remainingData.length > 0) {
                logger.warn("{} HTTP reader closed with remaining data:\n{}", ipAddress,
                        HexFormat.of().formatHex(remainingData));
            } else {
                logger.debug("{} HTTP reader closed normally", ipAddress);
            }
        }
    }

    /**
     * Flush any remaining unread data from the socket raw input stream. This is used when switching to a
     * secure session to ensure no leftover unencrypted data remains in the stream. It only reads data
     * that is immediately available without blocking.
     * 
     * @throws IOException
     */
    private void flushSocketAvailableBytes() throws IOException {
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[8192];
        int totalFlushed = 0;
        int available;

        while ((available = in.available()) > 0) {
            int toRead = Math.min(available, buffer.length);
            int n = in.read(buffer, 0, toRead);
            if (n <= 0) {
                break; // EOF
            }
            totalFlushed += n;
        }

        if (totalFlushed > 0) {
            logger.debug("Flushed {} bytes of available unread data", totalFlushed);
        }
    }
}
