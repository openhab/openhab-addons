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
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.session.AsymmetricSessionKeys;
import org.openhab.binding.homekit.internal.session.EventListener;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser.HttpPayload;
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

    private static final int TIMEOUT_MILLI_SECONDS = 15000; // HomeKit spec expects "around 10 seconds" so be safe
    private static final Duration MINIMUM_REQUEST_INTERVAL = Duration.ofMillis(250);

    private final Logger logger = LoggerFactory.getLogger(IpTransport.class);
    private final Socket socket;
    private final String hostName;
    private final String ipAddress;
    private final EventListener eventListener;
    private final AtomicBoolean awaitingHttpResponse = new AtomicBoolean(false);
    private final SynchronousQueue<HttpPayload> responseQueue = new SynchronousQueue<>();
    private final Thread inputThread;
    private final ExecutorService outputThreadExecutor;

    /**
     * SpotBugs incorrectly assumes that {@link SynchronousQueue#poll(long, TimeUnit)} never returns {@code null}.
     * According to the JavaDoc, the method returns {@code null} if the specified waiting time elapses before an
     * element becomes available. So this wrapper method correctly annotates the return value as nullable.
     */
    private static @Nullable HttpPayload synchronousQueuePollNullable(SynchronousQueue<HttpPayload> queue, long timeout,
            TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    private volatile HttpPayloadParser httpPayloadParser;
    private volatile @Nullable SecureSession secureSession = null;

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

        String[] parts = ipAddress.split(":");
        socket = new Socket();
        socket.setKeepAlive(true); // keep-alive forbidden for accessories but client should use it
        socket.setTcpNoDelay(true); // disable Nagle algorithm to force immediate flushing of packets
        socket.connect(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])), TIMEOUT_MILLI_SECONDS);

        httpPayloadParser = new HttpPayloadParser(socket.getInputStream());

        inputThread = new Thread(this::inputTask, "ip-transport-input");
        inputThread.setDaemon(true);
        inputThread.start();

        outputThreadExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ip-transport-output");
            t.setDaemon(true);
            return t;
        });

        logger.debug("Connected to {} alias {}", ipAddress, hostName);
    }

    /**
     * Sets the session keys for secure communication.
     * This starts a read thread to listen for incoming responses.
     *
     * @param keys the asymmetric session keys for encryption/decryption
     * @throws IOException
     * @throws IllegalStateException if the secure session is already set or the read thread is already running
     */
    public void setSessionKeys(AsymmetricSessionKeys keys) throws IOException, IllegalStateException {
        logger.trace("setSessionKeys()");
        if (secureSession != null) {
            throw new IllegalStateException("Secure session already set");
        }
        SecureSession secureSession = new SecureSession(socket, keys);
        httpPayloadParser = new HttpPayloadParser(secureSession.getInputStream()); // switch encrypted input stream
        this.secureSession = secureSession;
        logger.trace("setSessionKeys() {}", secureSession);
    }

    /**
     * Sends a GET request to the specified end-point with the given content type.
     *
     * @param end-point the end-point to which the request is sent
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
     * Sends a POST request to the specified end-point with the given content type and content.
     *
     * @param end-point the end-point to which the request is sent
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
     * Sends a PUT request to the specified end-point with the given content type and content.
     *
     * @param end-point the end-point to which the request is sent
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
     * Note: for thread safety only one request may be in flight at a time
     *
     * @param method the HTTP method (e.g., "GET", "POST", "PUT")
     * @param end-point the end-point to which the request is sent
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
        try {
            awaitingHttpResponse.set(true);
            byte[] request = buildRequest(method, endpoint, contentType, content);

            Duration delay = Duration.between(Instant.now(), earliestNextRequestTime);
            if (delay.isPositive()) {
                Thread.sleep(delay.toMillis()); // rate limit the HTTP requests
            }

            boolean trace = logger.isTraceEnabled();
            if (trace) {
                logger.trace("{} sending:\n{}", ipAddress, new String(request, StandardCharsets.ISO_8859_1));
            }

            HttpPayload response;
            Future<@Nullable Void> writeFuture;
            if (secureSession instanceof SecureSession secureSession) {
                writeFuture = outputThreadExecutor.submit(() -> {
                    secureSession.send(request);
                    return null;
                });
            } else {
                writeFuture = outputThreadExecutor.submit(() -> {
                    OutputStream out = socket.getOutputStream();
                    out.write(request);
                    out.flush();
                    return null;
                });
            }
            writeFuture.get(TIMEOUT_MILLI_SECONDS, TimeUnit.MILLISECONDS);
            response = synchronousQueuePollNullable(responseQueue, TIMEOUT_MILLI_SECONDS, TimeUnit.MILLISECONDS);
            if (response == null) {
                throw new TimeoutException("Timed out waiting for HTTP response");
            }
            earliestNextRequestTime = Instant.now().plus(MINIMUM_REQUEST_INTERVAL); // allow actual processing time

            checkHeaders(response.headers());
            return response.content();
        } finally {
            awaitingHttpResponse.set(false);
        }
    }

    /**
     * Builds an HTTP request with the given method, end-point, content type, and content.
     *
     * @param method the HTTP method (e.g., "GET", "POST", "PUT")
     * @param end-point the end-point to which the request is sent
     * @param contentType the content type of the request
     * @param content the content of the request
     * @return the complete HTTP request as a byte array
     * @throws IOException if an I/O error occurs
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
        if (inputThread instanceof Thread thread) {
            try {
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore interrupt flag, and shut down quietly
            }
        }
        responseQueue.offer(new HttpPayload()); // unblock any waiting execute() call
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
     * The input task that continuously listens for incoming HTTP pay-loads and forwards them to the
     * appropriate handler.
     */
    private void inputTask() {
        while (!Thread.currentThread().isInterrupted() && !closing) {
            httpPayloadParser.awaitHttpPayload().thenAccept(this::forwardHttpPayload);
        }
    }

    /**
     * Forwards the received HTTP pay-load to the appropriate handler based on its type.
     *
     * @param httpPayload the received HTTP pay-load
     */
    private void forwardHttpPayload(HttpPayload httpPayload) {
        String headers = new String(httpPayload.headers(), StandardCharsets.ISO_8859_1);
        if (logger.isTraceEnabled()) { // don't expand content trace string if not needed
            logger.trace("{} received:\n{}{}", ipAddress, headers,
                    new String(httpPayload.content(), StandardCharsets.ISO_8859_1));
        }
        if (headers.startsWith("HTTP")) { // deliver HTTP responses to execute()
            if (!responseQueue.offer(httpPayload)) {
                logger.warn("{} received HTTP response but no thread was waiting", ipAddress);
            }
        } else if (headers.startsWith("EVENT")) { // deliver EVENT messages directly to listener
            if (awaitingHttpResponse.get()) {
                logger.warn("{} received EVENT while waiting for HTTP response", ipAddress);
            }
            String jsonContent = new String(httpPayload.content(), StandardCharsets.UTF_8);
            eventListener.onEvent(jsonContent);
        } else {
            logger.warn("Unexpected response headers:\n{}", headers);
            responseQueue.offer(new HttpPayload()); // unblock any waiting execute() call
        }
    }
}
