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
package org.openhab.binding.atv.internal.client.support.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionFailedError;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionLostError;
import org.openhab.binding.atv.internal.client.exceptions.HttpError;
import org.openhab.binding.atv.internal.client.exceptions.OperationTimeoutError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A persistent HTTP/RTSP connection over a raw TCP socket.
 *
 * <p>
 * Incoming data is read on a dedicated virtual thread; public APIs return
 * {@link CompletableFuture} and never block the caller.
 *
 * <p>
 * Raw wire bytes pass through pluggable processors: everything written goes through the
 * {@code send processor} and every received chunk through the {@code receive processor}. These
 * are swappable mid-connection (the send processor is replaced after HAP pair-verify to
 * transparently enable channel encryption) via {@link #setSendProcessor(UnaryOperator)} and
 * {@link #setReceiveProcessor(UnaryOperator)}.
 *
 * <p>
 * Request/response correlation is a FIFO queue: HTTP responses on a single connection arrive
 * in request order. RTSP responses may arrive out of order; {@link RtspSession} adds CSeq-based
 * correlation on top of this class.
 *
 * <p>
 * The remote end may also send us unsolicited <em>requests</em> ("reverse HTTP", used by the
 * AirPlay event channel). Install a handler with {@link #setRequestHandler(Function)}; its
 * non-null return value is written back through the send processor. The handler runs on the
 * reader thread and must not block.
 *
 * <p>
 * Error mapping lives in {@link #sendAndReceive}: 403 always raises {@link AuthenticationError};
 * 401 raises {@link AuthenticationError} unless {@code allowError}; other non-2xx codes raise
 * {@link HttpError} unless {@code allowError}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class HttpConnection implements AutoCloseable {

    /**
     * Default response timeout. Rather long on purpose: a sleeping device automatically wakes up
     * when a service is requested from it, which can take up to 20 seconds or so.
     */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(25);

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnection.class);

    private static final UnaryOperator<byte[]> NULL_PROCESSOR = UnaryOperator.identity();

    private final Socket socket;
    private final OutputStream out;
    private final String localIp;
    private final String remoteIp;

    /**
     * Guards the pending-request FIFO, the send-processor application and the socket write as
     * one atomic unit. With concurrent Java callers the HAP send processor (stateful ChaCha nonce
     * counter) and FIFO registration must be serialized together with the write, otherwise nonces
     * are reused/reordered and responses are mis-correlated.
     */
    private final Object lock = new Object();
    private final Deque<PendingRequest> requests = new ArrayDeque<>();

    private volatile UnaryOperator<byte[]> sendProcessor = NULL_PROCESSOR;
    private volatile UnaryOperator<byte[]> receiveProcessor = NULL_PROCESSOR;
    private volatile @Nullable Function<HttpRequest, HttpResponse> requestHandler;
    private volatile @Nullable Runnable connectionLostListener;
    private volatile boolean closed;

    private static final class PendingRequest {
        final CompletableFuture<HttpResponse> future = new CompletableFuture<>();
    }

    private HttpConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = socket.getOutputStream();
        this.localIp = socket.getLocalAddress().getHostAddress();
        this.remoteIp = socket.getInetAddress().getHostAddress();
    }

    /**
     * Opens a connection to a remote host. The returned future
     * completes once the TCP connection is established and the reader thread is running, or
     * fails with {@link ConnectionFailedError}.
     *
     * @param address remote host name or IP address
     * @param port remote TCP port
     * @param timeout maximum time to wait for the TCP connect
     * @return future completing with the connected instance
     */
    public static CompletableFuture<HttpConnection> connect(String address, int port, Duration timeout) {
        CompletableFuture<HttpConnection> result = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            Socket socket = new Socket();
            try {
                socket.setTcpNoDelay(true);
                socket.connect(new InetSocketAddress(address, port), (int) timeout.toMillis());
                HttpConnection connection = new HttpConnection(socket);
                connection.startReader();
                LOGGER.debug("Connected to {}", connection.remoteIp);
                result.complete(connection);
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    // Best effort cleanup
                }
                result.completeExceptionally(
                        new ConnectionFailedError("failed to connect to " + address + ":" + port, e));
            }
        });
        return result;
    }

    /**
     * Opens a connection with the default timeout ({@link #DEFAULT_TIMEOUT}).
     *
     * @param address remote host name or IP address
     * @param port remote TCP port
     * @return future completing with the connected instance
     */
    public static CompletableFuture<HttpConnection> connect(String address, int port) {
        return connect(address, port, DEFAULT_TIMEOUT);
    }

    private void startReader() {
        Thread.startVirtualThread(this::readerLoop);
    }

    /** Returns the IP address of the local interface. */
    public String localIp() {
        return localIp;
    }

    /** Returns the IP address of the remote instance. */
    public String remoteIp() {
        return remoteIp;
    }

    /**
     * Replaces the processor applied to all outgoing wire bytes. Swappable mid-connection; used
     * to enable transparent HAP channel encryption after pair-verify.
     *
     * @param processor new processor ({@code null} resets to pass-through)
     */
    public void setSendProcessor(UnaryOperator<byte[]> processor) {
        this.sendProcessor = processor == null ? NULL_PROCESSOR : processor;
    }

    /**
     * Replaces the processor applied to every received chunk of wire bytes. Swappable
     * mid-connection; used to enable transparent HAP channel decryption after pair-verify.
     *
     * @param processor new processor ({@code null} resets to pass-through)
     */
    public void setReceiveProcessor(UnaryOperator<byte[]> processor) {
        this.receiveProcessor = processor == null ? NULL_PROCESSOR : processor;
    }

    /**
     * Installs a handler for unsolicited requests sent by the remote end (reverse HTTP, e.g. the
     * AirPlay event channel). The handler runs on the reader thread and must not block; a
     * non-null response is written back to the remote end.
     *
     * @param handler request handler ({@code null} to remove)
     */
    public void setRequestHandler(Function<HttpRequest, HttpResponse> handler) {
        this.requestHandler = handler;
    }

    /**
     * Installs a listener invoked once when the connection is lost by the remote end (not when
     * {@link #close()} is called locally). Runs on the reader thread.
     *
     * @param listener the listener ({@code null} to remove)
     */
    public void setConnectionLostListener(Runnable listener) {
        this.connectionLostListener = listener;
    }

    /** Returns true while the connection is open. */
    public boolean isConnected() {
        return !closed && !socket.isClosed();
    }

    /**
     * Closes the connection. Pending requests are failed with {@link ConnectionLostError}; the
     * connection-lost listener is not invoked for a local close.
     */
    @Override
    public void close() {
        // Deliberately does not take `lock`: a sender may hold it while blocked in a socket
        // write, and close() must be able to abort that write by closing the socket.
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.debug("Error closing socket", e);
        }
    }

    /**
     * Makes a GET request and returns the (future) response.
     *
     * @param path request path
     * @param allowError if true, error codes other than 403 complete normally
     * @return future response
     */
    public CompletableFuture<HttpResponse> get(String path, boolean allowError) {
        return sendAndReceive("GET", path, "HTTP/1.1", HttpParser.USER_AGENT, null, null, null, allowError,
                DEFAULT_TIMEOUT);
    }

    /**
     * Makes a POST request and returns the (future) response.
     *
     * @param path request path
     * @param headers extra headers (may be null)
     * @param body request body ({@link String}, {@code byte[]} or null)
     * @param allowError if true, error codes other than 403 complete normally
     * @return future response
     */
    public CompletableFuture<HttpResponse> post(String path, @Nullable Map<String, ?> headers, @Nullable Object body,
            boolean allowError) {
        return sendAndReceive("POST", path, "HTTP/1.1", HttpParser.USER_AGENT, null, headers, body, allowError,
                DEFAULT_TIMEOUT);
    }

    /**
     * Sends an HTTP message and returns the (future) response.
     *
     * <p>
     * Completion is exceptional with:
     * <ul>
     * <li>{@link AuthenticationError} on 403 (always) and on 401 (unless {@code allowError})</li>
     * <li>{@link HttpError} on other non-2xx codes (unless {@code allowError})</li>
     * <li>{@link OperationTimeoutError} when no response arrives within {@code timeout}</li>
     * <li>{@link ConnectionLostError} when the connection goes away</li>
     * </ul>
     *
     * @param method request method
     * @param uri request URI
     * @param protocol protocol string, e.g. {@code "HTTP/1.1"} or {@code "RTSP/1.0"}
     * @param userAgent user agent to insert when {@code headers} has none
     * @param contentType optional Content-Type value
     * @param headers extra headers (may be null)
     * @param body request body ({@link String}, {@code byte[]} or null)
     * @param allowError if true, error codes other than 403 complete normally
     * @param timeout maximum time to wait for the response
     * @return future response
     */
    public CompletableFuture<HttpResponse> sendAndReceive(String method, String uri, String protocol, String userAgent,
            @Nullable String contentType, @Nullable Map<String, ?> headers, @Nullable Object body, boolean allowError,
            Duration timeout) {
        byte[] output = HttpParser.formatMessage(method, uri, protocol, userAgent, contentType, headers, body);

        PendingRequest pending = new PendingRequest();
        LOGGER.trace("Sending {} message: {} {}", protocol, method, uri);
        // FIFO registration, send-processor application (stateful when HAP encryption is
        // enabled) and socket write form one atomic unit — see the `lock` javadoc.
        synchronized (lock) {
            if (closed) {
                return CompletableFuture.failedFuture(new ConnectionLostError("not connected to remote"));
            }
            // Register before writing so the reader thread cannot see the response first
            requests.addLast(pending);
            boolean sent = false;
            try {
                write(output);
                sent = true;
            } catch (IOException e) {
                return CompletableFuture.failedFuture(new ConnectionLostError("failed to send request", e));
            } finally {
                if (!sent) {
                    requests.remove(pending);
                }
            }
        }

        return pending.future.orTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS).handle((response, error) -> {
            if (error != null) {
                // If request failed and is still in the request queue, remove it
                removePending(pending);
                Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause()
                        : error;
                if (cause instanceof TimeoutException) {
                    throw new CompletionException(new OperationTimeoutError(
                            "no response to " + method + " " + uri + " (" + protocol + ")", cause));
                }
                throw cause instanceof CompletionException ce ? ce : new CompletionException(cause);
            }

            LOGGER.trace("Got {} response: {} {}", response.protocol(), response.code(), response.message());

            if (response.code() == 403) {
                throw new CompletionException(new AuthenticationError("not authenticated"));
            }

            // Password required
            if (response.code() == 401) {
                if (allowError) {
                    return response;
                }
                throw new CompletionException(new AuthenticationError("not authenticated"));
            }

            // Positive response
            if ((response.code() >= 200 && response.code() < 300) || allowError) {
                return response;
            }

            throw new CompletionException(new HttpError(
                    protocol + " method " + method + " failed with code " + response.code() + ": " + response.message(),
                    response.code()));
        });
    }

    /**
     * Applies the send processor and writes the result to the socket, all under {@code lock}
     * so the (stateful) processor and the write order can never diverge.
     *
     * @param plaintext unprocessed wire bytes
     * @throws IOException on write failure
     */
    private void write(byte[] plaintext) throws IOException {
        synchronized (lock) {
            out.write(sendProcessor.apply(plaintext));
            out.flush();
        }
    }

    private void removePending(PendingRequest pending) {
        synchronized (lock) {
            requests.remove(pending);
        }
    }

    private void readerLoop() {
        byte[] buffer = new byte[0];
        byte[] chunk = new byte[8192];
        boolean lostByRemote = false;
        try {
            InputStream in = socket.getInputStream();
            while (true) {
                int read = in.read(chunk);
                if (read < 0) {
                    lostByRemote = !closed;
                    break;
                }
                byte[] data = receiveProcessor.apply(Arrays.copyOf(chunk, read));
                buffer = concat(buffer, data);
                buffer = processBuffer(buffer);
            }
        } catch (IOException e) {
            lostByRemote = !closed;
            if (!closed) {
                LOGGER.debug("Connection error", e);
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to process incoming data, closing connection", e);
            lostByRemote = !closed;
        }

        LOGGER.debug("Connection closed");
        close();

        List<PendingRequest> outstanding;
        synchronized (lock) {
            outstanding = new ArrayList<>(requests);
            requests.clear();
        }
        for (PendingRequest pending : outstanding) {
            pending.future.completeExceptionally(new ConnectionLostError("connection was lost"));
        }

        if (lostByRemote) {
            Runnable listener = connectionLostListener;
            if (listener != null) {
                listener.run();
            }
        }
    }

    private byte[] processBuffer(byte[] buffer) {
        while (buffer.length > 0) {
            if (HttpParser.startsWithResponseLine(buffer)) {
                HttpParser.ParseResult<HttpResponse> parsed = HttpParser.parseResponse(buffer);
                HttpResponse message = parsed.message();
                if (message == null) {
                    LOGGER.debug("Not enough data to decode message");
                    break;
                }
                buffer = parsed.remainder();
                dispatchResponse(message);
            } else {
                HttpParser.ParseResult<HttpRequest> parsed = HttpParser.parseRequest(buffer);
                HttpRequest message = parsed.message();
                if (message == null) {
                    LOGGER.debug("Not enough data to decode message");
                    break;
                }
                buffer = parsed.remainder();
                dispatchRequest(message);
            }
        }
        return buffer;
    }

    private void dispatchResponse(HttpResponse response) {
        PendingRequest pending;
        synchronized (lock) {
            pending = requests.pollFirst();
        }
        if (pending != null) {
            pending.future.complete(response);
        } else {
            LOGGER.warn("Got response without having a request: {}", response);
        }
    }

    private void dispatchRequest(HttpRequest request) {
        Function<HttpRequest, HttpResponse> handler = requestHandler;
        if (handler == null) {
            LOGGER.warn("Got request without a request handler: {}", request);
            return;
        }
        HttpResponse response;
        try {
            response = handler.apply(request);
        } catch (RuntimeException e) {
            LOGGER.warn("Request handler failed for {}", request, e);
            return;
        }
        if (response != null) {
            try {
                write(HttpParser.formatResponse(response));
            } catch (IOException e) {
                LOGGER.debug("Failed to write response to unsolicited request", e);
            }
        }
    }

    private static byte[] concat(byte[] first, byte[] second) {
        if (first.length == 0) {
            return second;
        }
        if (second.length == 0) {
            return first;
        }
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
