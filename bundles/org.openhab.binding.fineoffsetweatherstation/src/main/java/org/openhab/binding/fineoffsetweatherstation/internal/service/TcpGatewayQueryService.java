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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.Utils;
import org.openhab.binding.fineoffsetweatherstation.internal.handler.ThingStatusListener;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Base class for the gateway query services that communicate via the proprietary TCP binary protocol.
 * <p>
 * It owns the socket lifecycle and the command/response exchange; the concrete subclasses
 * ({@link FineOffsetGatewayQueryService}, {@link ELVGatewayQueryService}) only differ in the
 * commands they send and how they parse the responses.
 *
 * @author Andreas Berger - Initial contribution
 * @author Christopher Smith - Read framed responses completely, per-gateway request lock, reconnect on stale
 *         connections
 */
@NonNullByDefault
public abstract class TcpGatewayQueryService extends GatewayQueryService {

    /** Marks a response whose length is not encoded in a size header; it is read with a single read call. */
    protected static final int UNFRAMED = 0;

    static final String NO_VALID_RESPONSE = "@text/offline.comm-error.no-valid-response";

    private static final int UNFRAMED_BUFFER_LENGTH = 2048;
    /** How long a host name that did not resolve is not looked up again for the lock key. */
    private static final long LOCK_KEY_RETRY_NANOS = TimeUnit.MINUTES.toNanos(1);

    /**
     * The gateways answer only one request at a time and some accept only a single client connection. Requests to
     * the same gateway - from its handler and from discovery probes - are serialized through a lock shared per
     * resolved address; different gateways do not block each other.
     */
    private static final Map<String, Lock> REQUEST_LOCKS = new ConcurrentHashMap<>();

    private volatile @Nullable Socket socket;
    /** The socket currently being connected, so {@link #close()} can abort the connect without waiting for it. */
    private volatile @Nullable Socket connecting;
    private volatile boolean closed;
    private volatile @Nullable String lockKey;
    /** When the host name may be looked up again for the lock key, after it did not resolve. */
    private volatile long lockKeyRetryAt = System.nanoTime();

    public TcpGatewayQueryService(FineOffsetGatewayConfiguration config,
            @Nullable ThingStatusListener thingStatusListener) {
        super(config, thingStatusListener);
    }

    /**
     * Sends a request and reads the response.
     * <p>
     * If a reused connection turns out to be closed by the gateway (it drops idle clients), the request is sent once
     * more on a new connection.
     *
     * @param sizeBytes the number of bytes (1 or 2) of the size field following the 3-byte header of the response, or
     *            {@link #UNFRAMED} if the response carries no such header
     * @return the response, or {@code null} if no valid response was received
     */
    protected byte @Nullable [] executeCommand(String command, byte[] request, int sizeBytes,
            Function<byte[], Boolean> validateResponse) {
        Lock lock = Objects.requireNonNull(REQUEST_LOCKS.computeIfAbsent(lockKey(), key -> new ReentrantLock()));
        // a request holds the lock for up to two connects (initial and after a stale connection) and their reads
        long lockWaitSeconds = 4L * config.timeout + 5;
        try {
            if (!lock.tryLock(lockWaitSeconds, TimeUnit.SECONDS)) {
                logger.debug("executeCommand({}): timed out while getting the lock", command);
                updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, NO_VALID_RESPONSE);
                return null;
            }
        } catch (InterruptedException e) {
            logger.debug("executeCommand({}): was interrupted while getting the lock", command);
            Thread.currentThread().interrupt();
            return null;
        }

        try {
            for (int attempt = 0;; attempt++) {
                boolean reused = this.socket != null;
                Socket socket = getConnection();
                if (socket == null) {
                    return null;
                }
                boolean mayRetry = reused && attempt == 0 && !closed;
                byte[] data;
                try {
                    logger.trace("executeCommand({}): send request: {}", command,
                            Utils.toHexString(request, request.length, ""));
                    socket.getOutputStream().write(request);
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(config.timeout);
                    data = sizeBytes == UNFRAMED ? readUnframed(socket, deadline)
                            : readFramed(socket, sizeBytes, deadline);
                } catch (SocketTimeoutException ex) {
                    // the gateway is slow, not gone: retrying would only double the wait
                    disconnect();
                    updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                    return null;
                } catch (IOException ex) {
                    disconnect();
                    if (mayRetry) {
                        logger.debug("executeCommand({}): connection lost ({}), reconnecting", command,
                                ex.getMessage());
                        continue;
                    }
                    updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                    return null;
                }
                if (data == null) {
                    disconnect();
                    if (mayRetry) {
                        logger.debug("executeCommand({}): connection closed by the gateway, reconnecting", command);
                        continue;
                    }
                    logger.debug("executeCommand({}): no or incomplete response", command);
                    updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, NO_VALID_RESPONSE);
                    return null;
                }
                if (!validateResponse.apply(data)) {
                    logger.debug("executeCommand({}), invalid response: {}", command,
                            Utils.toHexString(data, data.length, ""));
                    // discard the connection, so remaining bytes of this response cannot be mistaken for the next one
                    disconnect();
                    updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, NO_VALID_RESPONSE);
                    return null;
                }
                logger.trace("executeCommand({}): received: {}", command, Utils.toHexString(data, data.length, ""));
                return data;
            }
        } catch (RuntimeException ex) {
            logger.warn("executeCommand({})", command, ex);
            disconnect();
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The same gateway may be configured by host name in its Thing and reached by IP address from discovery, so the
     * lock is keyed by the resolved address.
     */
    private String lockKey() {
        String key = this.lockKey;
        if (key != null) {
            return key;
        }
        String host = config.ip;
        if (host == null || host.isBlank() || System.nanoTime() - lockKeyRetryAt < 0) {
            return host + ":" + config.port;
        }
        try {
            // resolved once: a name lookup on every request could wait for an unreachable DNS server each time
            key = InetAddress.getByName(host).getHostAddress() + ":" + config.port;
            this.lockKey = key;
            return key;
        } catch (UnknownHostException e) {
            // Not cached, so the key matches discovery's once the name resolves again; connecting reports the error.
            // Until then the name is not looked up for the key, which would make every request wait for DNS twice.
            lockKeyRetryAt = System.nanoTime() + LOCK_KEY_RETRY_NANOS;
            return host + ":" + config.port;
        }
    }

    private byte @Nullable [] readUnframed(Socket socket, long deadline) throws IOException {
        byte[] buffer = new byte[UNFRAMED_BUFFER_LENGTH];
        int bytesRead = read(socket, buffer, 0, buffer.length, deadline);
        return bytesRead <= 0 ? null : Arrays.copyOf(buffer, bytesRead);
    }

    /**
     * Reads a response of the form {@code 0xff 0xff <command> <size (1 or 2 bytes)> <data> <checksum>}, where size
     * counts all bytes except the two leading {@code 0xff}. TCP may deliver the response in several segments, so this
     * reads until the announced length has been received, or the reply timeout has passed.
     */
    private byte @Nullable [] readFramed(Socket socket, int sizeBytes, long deadline) throws IOException {
        int headerLength = 3 + sizeBytes;
        byte[] header = new byte[headerLength];
        if (readFully(socket, header, 0, headerLength, deadline) < headerLength || header[0] != (byte) 0xff
                || header[1] != (byte) 0xff) {
            return null;
        }
        int size = sizeBytes == 1 ? Utils.toUInt8(header[3]) : Utils.toUInt16(header, 3);
        int totalLength = size + 2;
        if (totalLength <= headerLength) {
            return null;
        }
        byte[] data = Arrays.copyOf(header, totalLength);
        int remaining = totalLength - headerLength;
        if (readFully(socket, data, headerLength, remaining, deadline) < remaining) {
            return null;
        }
        return data;
    }

    /** Reads until {@code length} bytes are read or the stream ends, and returns the number of bytes read. */
    private int readFully(Socket socket, byte[] buffer, int offset, int length, long deadline) throws IOException {
        int total = 0;
        while (total < length) {
            int bytesRead = read(socket, buffer, offset + total, length - total, deadline);
            if (bytesRead < 0) {
                break;
            }
            total += bytesRead;
        }
        return total;
    }

    /**
     * A single read that ends by the deadline: {@code SO_TIMEOUT} limits each read on its own, so a response that
     * arrives slowly in many segments would otherwise hold the gateway's lock well beyond the reply timeout.
     */
    private int read(Socket socket, byte[] buffer, int offset, int length, long deadline) throws IOException {
        long remainingMillis = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
        if (remainingMillis <= 0) {
            throw new SocketTimeoutException("Read timed out");
        }
        socket.setSoTimeout((int) Math.min(remainingMillis, Integer.MAX_VALUE));
        return socket.getInputStream().read(buffer, offset, length);
    }

    protected synchronized @Nullable Socket getConnection() {
        if (closed) {
            return null;
        }
        Socket socket = this.socket;
        if (socket != null) {
            return socket;
        }
        String ip = config.ip;
        if (ip == null || ip.isBlank()) {
            return null;
        }
        Socket newSocket = new Socket();
        connecting = newSocket;
        try {
            int timeoutMs = (int) TimeUnit.SECONDS.toMillis(config.timeout);
            newSocket.connect(new InetSocketAddress(ip, config.port), timeoutMs);
            newSocket.setSoTimeout(timeoutMs);
            if (closed) {
                closeQuietly(newSocket);
                return null;
            }
            this.socket = newSocket;
            if (closed) {
                // close() ran between the check above and the assignment and did not see this socket
                disconnect();
                return null;
            }
            updateThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
            return newSocket;
        } catch (IOException e) {
            closeQuietly(newSocket);
            if (!closed) {
                updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            return null;
        } finally {
            connecting = null;
        }
    }

    /** Drops the current connection; the next request opens a new one. */
    private void disconnect() {
        Socket socket = this.socket;
        this.socket = null;
        closeQuietly(socket);
    }

    private void closeQuietly(@Nullable Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException e) {
            logger.trace("failed to close the socket: {}", e.getMessage());
        }
    }

    /**
     * Closes the connection for good. This does not wait for a request in progress: a connect in progress is aborted,
     * so disposing the handler returns promptly.
     */
    @Override
    public void close() throws IOException {
        closed = true;
        closeQuietly(connecting);
        disconnect();
    }
}
