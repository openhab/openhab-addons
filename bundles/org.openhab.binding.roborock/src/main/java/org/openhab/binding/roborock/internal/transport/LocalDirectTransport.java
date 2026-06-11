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
package org.openhab.binding.roborock.internal.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.RoborockException;
import org.openhab.binding.roborock.internal.util.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Direct/local transport foundation for Roborock communication.
 *
 * This class performs local direct handshaking and command exchange over a short-lived socket.
 *
 * @author Maciej Pham - Initial contribution
 */
@NonNullByDefault
public class LocalDirectTransport implements RoborockCommandTransport {

    private static final int CRC_LENGTH = 4;
    private static final int HELLO_FRAME_LENGTH_WITHOUT_CRC = 17;
    private static final int HELLO_FRAME_LENGTH = HELLO_FRAME_LENGTH_WITHOUT_CRC + CRC_LENGTH;
    private static final int COMMON_HEADER_LENGTH_WITHOUT_CRC = 19;
    private static final int SOCKET_CONNECT_TIMEOUT_MS = 3000;
    private static final int SOCKET_BASE_READ_TIMEOUT_MS = 600;
    private static final int COMMAND_RESPONSE_TIMEOUT_MS = 600;
    private static final int FRAME_READ_SLICE_TIMEOUT_MS = 200;
    private static final int KEEPALIVE_INTERVAL_SECONDS = 15;
    private static final int KEEPALIVE_RESPONSE_TIMEOUT_MS = 1500;
    private static final int DIRECT_INTER_COMMAND_GAP_MS = 20;
    private static final int READ_TIMEOUT_SENTINEL = -1;
    private static final int READ_EOF_SENTINEL = -2;
    private static final int MAX_PREFIXED_FRAME_LENGTH = 1024 * 1024;
    private static final int MAX_NON_PREFIXED_PAYLOAD_LENGTH = 0xFFFF;
    private static final byte[] EMPTY_NONCE = new byte[16];

    private final Logger logger = LoggerFactory.getLogger(LocalDirectTransport.class);
    private LocalDirectProtocolSession protocolSession = new LocalDirectProtocolSession();
    private final Object commandLock = new Object();
    private final ScheduledExecutorService keepaliveScheduler;
    private final boolean ownsKeepaliveScheduler;
    private final long interCommandGapMillis;
    private final LongSupplier nanoTimeSupplier;
    private final Sleeper sleeper;
    private final Supplier<Socket> socketSupplier;
    private @Nullable ScheduledFuture<?> keepaliveFuture;
    private long lastDirectCommandFinishedNanos;

    private String localKey = "";
    private String localHost = "";
    private int localPort = 58867;
    private String endpointPrefix = "";
    private @Nullable Consumer<byte[]> messageConsumer;
    private @Nullable Socket sessionSocket;
    private boolean preferredHelloL01;
    private boolean preferredHelloPrefixed = true;
    private final AtomicLong readSliceTimeoutCount = new AtomicLong();
    private final AtomicLong readSliceEofCount = new AtomicLong();

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    public LocalDirectTransport() {
        this(createKeepaliveScheduler(), true, DIRECT_INTER_COMMAND_GAP_MS, System::nanoTime, Thread::sleep,
                Socket::new);
    }

    LocalDirectTransport(ScheduledExecutorService keepaliveScheduler, long interCommandGapMillis,
            LongSupplier nanoTimeSupplier, Sleeper sleeper) {
        this(keepaliveScheduler, false, interCommandGapMillis, nanoTimeSupplier, sleeper, Socket::new);
    }

    LocalDirectTransport(ScheduledExecutorService keepaliveScheduler, long interCommandGapMillis,
            LongSupplier nanoTimeSupplier, Sleeper sleeper, Supplier<Socket> socketSupplier) {
        this(keepaliveScheduler, false, interCommandGapMillis, nanoTimeSupplier, sleeper, socketSupplier);
    }

    private LocalDirectTransport(ScheduledExecutorService keepaliveScheduler, boolean ownsKeepaliveScheduler,
            long interCommandGapMillis, LongSupplier nanoTimeSupplier, Sleeper sleeper,
            Supplier<Socket> socketSupplier) {
        this.keepaliveScheduler = keepaliveScheduler;
        this.ownsKeepaliveScheduler = ownsKeepaliveScheduler;
        this.interCommandGapMillis = Math.max(0, interCommandGapMillis);
        this.nanoTimeSupplier = nanoTimeSupplier;
        this.sleeper = sleeper;
        this.socketSupplier = socketSupplier;
    }

    private @Nullable Socket getSocketFromSupplier() {
        return socketSupplier.get();
    }

    @Override
    public int sendCommand(String method, String params, int requestId) throws UnsupportedEncodingException {
        long waitStartedNanos = System.nanoTime();
        synchronized (commandLock) {
            long queueWaitMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - waitStartedNanos);
            if (queueWaitMillis > 0 && logger.isTraceEnabled()) {
                logger.trace("Queued direct command '{}' for {} ms while another command was in-flight (requestId={}).",
                        method, queueWaitMillis, requestId);
            }
            return sendCommandSerialized(method, params, requestId);
        }
    }

    private int sendCommandSerialized(String method, String params, int requestId) {
        if (localHost.isBlank()) {
            logger.debug("Direct mode requested, but local host is not available. Skipping method {}", method);
            return -1;
        }
        if (localKey.isBlank()) {
            logger.debug("Direct mode requested, but local key is not available. Skipping method {}", method);
            return -1;
        }

        enforceInterCommandGap(method, requestId);

        try {
            return sendCommandWithReconnect(method, params, requestId);
        } catch (RoborockException | IOException e) {
            logger.debug("Direct command send failed for method '{}' (requestId={}): {}", method, requestId,
                    e.getMessage());
            return -1;
        } finally {
            lastDirectCommandFinishedNanos = nanoTimeSupplier.getAsLong();
        }
    }

    @Override
    public void updateContext(@Nullable String localKey, @Nullable String localHost, int localPort,
            @Nullable String endpointPrefix) {
        boolean contextChanged;
        synchronized (commandLock) {
            String updatedLocalKey = localKey == null ? "" : localKey;
            String updatedLocalHost = localHost == null ? "" : localHost;
            String updatedEndpointPrefix = endpointPrefix == null ? "" : endpointPrefix;

            contextChanged = !this.localKey.equals(updatedLocalKey) || !this.localHost.equals(updatedLocalHost)
                    || this.localPort != localPort || !this.endpointPrefix.equals(updatedEndpointPrefix);

            this.localKey = updatedLocalKey;
            this.localHost = updatedLocalHost;
            this.localPort = localPort;
            this.endpointPrefix = updatedEndpointPrefix;
        }
        if (contextChanged) {
            invalidateSession("Direct transport context changed. Reconnect required.");
        }
    }

    @Override
    public void setMessageConsumer(@Nullable Consumer<byte[]> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void dispose() {
        invalidateSession("Disposing direct transport session.");
        if (ownsKeepaliveScheduler) {
            keepaliveScheduler.shutdownNow();
        }
    }

    public int getConnectNonce() {
        return protocolSession.getConnectNonce();
    }

    public int getAckNonce() {
        return protocolSession.getAckNonce();
    }

    private int sendCommandWithReconnect(String method, String params, int requestId)
            throws RoborockException, IOException {
        try {
            return sendCommandOnSession(method, params, requestId, false);
        } catch (IOException firstFailure) {
            invalidateSession("Direct session failure detected. Reconnecting and retrying command.", firstFailure);
            return sendCommandOnSession(method, params, requestId, true);
        }
    }

    private int sendCommandOnSession(String method, String params, int requestId, boolean reconnectAttempt)
            throws RoborockException, IOException {
        Socket socket = ensureConnectedSession(method, requestId, reconnectAttempt);
        OutputStream outputStream = socket.getOutputStream();

        byte[] frame = protocolSession.buildCommandFrame(method, params, requestId, localKey);
        outputStream.write(frame);
        outputStream.flush();

        byte[] response = readMatchingResponseFrame(socket, requestId, method, COMMAND_RESPONSE_TIMEOUT_MS);
        if (response.length > 0) {
            @Nullable
            Consumer<byte[]> consumer = messageConsumer;
            if (consumer != null) {
                consumer.accept(response);
            }
            logger.debug(
                    "Direct transport sent method '{}' and received matching response ({} bytes) from {}:{} (requestId={}).",
                    method, response.length, localHost, localPort, requestId);
        } else {
            logger.debug(
                    "Direct transport sent method '{}' to {}:{} (requestId={}) but received no matching response before timeout.",
                    method, localHost, localPort, requestId);
            // A full no-match timeout is treated as session failure immediately so we reconnect and retry once.
            invalidateSession("No matching direct response frame received before timeout.");
            if (logger.isTraceEnabled()) {
                logger.trace("Direct no-match timeout counters: timeoutSlices={}, eofEvents={}",
                        readSliceTimeoutCount.get(), readSliceEofCount.get());
            }
            throw new SocketTimeoutException("Direct command did not receive matching response before timeout.");
        }
        return requestId;
    }

    private Socket ensureConnectedSession(String method, int requestId, boolean reconnectAttempt) throws IOException {
        @Nullable
        Socket currentSocket = sessionSocket;
        if (currentSocket != null && isSessionHealthy(currentSocket)) {
            return currentSocket;
        }
        if (currentSocket != null && logger.isTraceEnabled()) {
            logger.trace(
                    "Direct session socket is not reusable (connected={}, closed={}, inputShutdown={}, outputShutdown={}). Opening replacement session.",
                    currentSocket.isConnected(), currentSocket.isClosed(), currentSocket.isInputShutdown(),
                    currentSocket.isOutputShutdown());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Opening {} direct session for {}:{} before method '{}' (requestId={}).",
                    reconnectAttempt ? "replacement" : "new", localHost, localPort, method, requestId);
        }

        @Nullable
        Socket socket = getSocketFromSupplier();
        if (socket == null) {
            throw new IOException("Direct socket supplier returned null socket instance.");
        }
        try {
            socket.connect(new InetSocketAddress(localHost, localPort), SOCKET_CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(SOCKET_BASE_READ_TIMEOUT_MS);

            protocolSession = new LocalDirectProtocolSession();
            OutputStream outputStream = socket.getOutputStream();
            if (!performHelloHandshake(socket, outputStream, method, requestId)) {
                throw new IOException("Direct local handshake failed while establishing reusable session.");
            }

            sessionSocket = socket;
            scheduleKeepalive();
            if (logger.isTraceEnabled()) {
                logger.trace("Direct session established for {}:{} (requestId={}); keepalive active every {}s.",
                        localHost, localPort, requestId, KEEPALIVE_INTERVAL_SECONDS);
            }
            return socket;
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException closeException) {
                logger.trace("Failed to close partially initialized direct socket cleanly: {}",
                        closeException.getMessage());
            }
            throw e;
        }
    }

    private boolean isSessionHealthy(@Nullable Socket socket) {
        return socket != null && socket.isConnected() && !socket.isClosed() && !socket.isInputShutdown()
                && !socket.isOutputShutdown();
    }

    private void invalidateSession(String reason) {
        invalidateSession(reason, null);
    }

    private void invalidateSession(String reason, @Nullable Throwable cause) {
        synchronized (commandLock) {
            cancelKeepalive();

            @Nullable
            Socket socket = sessionSocket;
            sessionSocket = null;

            if (cause != null) {
                logger.debug("{} Cause: {}", reason, cause.getMessage());
            } else if (logger.isTraceEnabled()) {
                logger.trace("{}", reason);
            }

            if (socket == null) {
                return;
            }

            try {
                socket.close();
            } catch (IOException closeException) {
                logger.trace("Failed to close direct session socket cleanly: {}", closeException.getMessage());
            }
        }
    }

    private void enforceInterCommandGap(String method, int requestId) {
        if (interCommandGapMillis <= 0) {
            return;
        }
        long lastFinishedNanos = lastDirectCommandFinishedNanos;
        if (lastFinishedNanos <= 0) {
            return;
        }
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(nanoTimeSupplier.getAsLong() - lastFinishedNanos);
        long remainingMillis = interCommandGapMillis - elapsedMillis;
        if (remainingMillis <= 0) {
            return;
        }
        try {
            sleeper.sleep(remainingMillis);
            if (logger.isTraceEnabled()) {
                logger.trace("Applied direct inter-command pacing gap of {} ms before method '{}' (requestId={}).",
                        remainingMillis, method, requestId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.trace("Interrupted while applying direct inter-command pacing gap before '{}' (requestId={}).",
                    method, requestId);
        }
    }

    private void scheduleKeepalive() {
        @Nullable
        ScheduledFuture<?> currentKeepaliveFuture = keepaliveFuture;
        if (currentKeepaliveFuture != null && !currentKeepaliveFuture.isDone()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Direct keepalive scheduler already active for {}:{}.", localHost, localPort);
            }
            return;
        }
        keepaliveFuture = keepaliveScheduler.scheduleWithFixedDelay(this::runKeepalive, KEEPALIVE_INTERVAL_SECONDS,
                KEEPALIVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
        if (logger.isTraceEnabled()) {
            logger.trace("Started direct keepalive scheduler for {}:{} with interval={}s.", localHost, localPort,
                    KEEPALIVE_INTERVAL_SECONDS);
        }
    }

    private void cancelKeepalive() {
        @Nullable
        ScheduledFuture<?> task = keepaliveFuture;
        keepaliveFuture = null;
        if (task != null) {
            task.cancel(true);
            if (logger.isTraceEnabled()) {
                logger.trace("Cancelled direct keepalive scheduler for {}:{}.", localHost, localPort);
            }
        }
    }

    private void runKeepalive() {
        synchronized (commandLock) {
            @Nullable
            Socket socket = sessionSocket;
            if (socket == null || !isSessionHealthy(socket)) {
                return;
            }
            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Running direct keepalive ping for {}:{}.", localHost, localPort);
                }
                OutputStream outputStream = socket.getOutputStream();
                byte[] pingFrame = protocolSession.buildPingRequestFrame();
                outputStream.write(pingFrame);
                outputStream.flush();

                if (!readPingResponseFrame(socket, KEEPALIVE_RESPONSE_TIMEOUT_MS)) {
                    throw new IOException("Direct keepalive ping timed out waiting for ping response.");
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Direct keepalive ping succeeded for {}:{}.", localHost, localPort);
                }
            } catch (IOException e) {
                invalidateSession("Direct keepalive ping failed. Marking session invalid.", e);
            }
        }
    }

    private boolean readPingResponseFrame(Socket socket, int timeoutMs) throws IOException {
        long deadlineNanos = nanoTimeSupplier.getAsLong() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        while (nanoTimeSupplier.getAsLong() < deadlineNanos) {
            long remainingMillis = Math.max(1,
                    TimeUnit.NANOSECONDS.toMillis(deadlineNanos - nanoTimeSupplier.getAsLong()));
            socket.setSoTimeout((int) Math.min(remainingMillis, FRAME_READ_SLICE_TIMEOUT_MS));

            byte[] candidate = normalizeFrame(readSingleFrame(socket));
            if (candidate.length == 0) {
                continue;
            }
            if (protocolSession.isPingResponseFrame(candidate)) {
                return true;
            }
            if (!isControlOrAckFrame(candidate)) {
                @Nullable
                Consumer<byte[]> consumer = messageConsumer;
                if (consumer != null) {
                    try {
                        consumer.accept(candidate);
                    } catch (RuntimeException e) {
                        logger.debug(
                                "Failed to dispatch background direct frame while waiting for keepalive response: {}",
                                e.getMessage());
                    }
                }
            }
        }
        return false;
    }

    private static ScheduledExecutorService createKeepaliveScheduler() {
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "roborock-direct-keepalive");
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    private byte[] readMatchingResponseFrame(Socket socket, int requestId, String method, int timeoutMs)
            throws IOException {
        long timeoutCountAtStart = readSliceTimeoutCount.get();
        long eofCountAtStart = readSliceEofCount.get();
        long deadlineNanos = nanoTimeSupplier.getAsLong() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        while (nanoTimeSupplier.getAsLong() < deadlineNanos) {
            long remainingMillis = Math.max(1,
                    TimeUnit.NANOSECONDS.toMillis(deadlineNanos - nanoTimeSupplier.getAsLong()));
            socket.setSoTimeout((int) Math.min(remainingMillis, FRAME_READ_SLICE_TIMEOUT_MS));

            byte[] candidate = normalizeFrame(readSingleFrame(socket));
            if (candidate.length == 0) {
                continue;
            }

            int candidateProtocol = readFrameProtocol(candidate);
            int candidatePayloadLength = readFramePayloadLength(candidate);

            if (isControlOrAckFrame(candidate)) {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Ignoring direct non-payload frame ({} bytes, protocol={}, payloadLen={}) while waiting for method '{}' (requestId={}).",
                            candidate.length, candidateProtocol, candidatePayloadLength, method, requestId);
                }
                continue;
            }

            @Nullable
            Integer candidateRequestId = extractResponseRequestId(candidate);
            if (candidateRequestId == null) {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Ignoring direct payload frame without correlatable request id while waiting for method '{}' requestId {} (protocol={}, payloadLen={}, firstBytes={}).",
                            method, requestId, candidateProtocol, candidatePayloadLength, toHexPreview(candidate, 18));
                }
                continue;
            }

            if (candidateRequestId.intValue() != requestId) {
                dispatchOutOfOrderFrame(candidate, candidateRequestId.intValue(), requestId, method, candidateProtocol,
                        candidatePayloadLength);
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Ignoring direct payload frame for different requestId {} while waiting for method '{}' requestId {} (protocol={}, payloadLen={}).",
                            candidateRequestId, method, requestId, candidateProtocol, candidatePayloadLength);
                }
                continue;
            }
            return candidate;
        }
        if (logger.isTraceEnabled()) {
            long timeoutDelta = readSliceTimeoutCount.get() - timeoutCountAtStart;
            long eofDelta = readSliceEofCount.get() - eofCountAtStart;
            logger.trace(
                    "Direct response wait timed out for method '{}' (requestId={}) after {}ms with sliceTimeouts={} and eofEvents={}",
                    method, requestId, timeoutMs, timeoutDelta, eofDelta);
        }
        return new byte[0];
    }

    private void dispatchOutOfOrderFrame(byte[] frame, int candidateRequestId, int awaitedRequestId,
            String awaitedMethod, int candidateProtocol, int candidatePayloadLength) {
        @Nullable
        Consumer<byte[]> consumer = messageConsumer;
        if (consumer == null) {
            return;
        }
        try {
            consumer.accept(frame);
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Dispatched out-of-order direct payload for requestId {} while awaiting method '{}' requestId {} (protocol={}, payloadLen={}).",
                        candidateRequestId, awaitedMethod, awaitedRequestId, candidateProtocol, candidatePayloadLength);
            }
        } catch (RuntimeException e) {
            logger.debug(
                    "Failed to dispatch out-of-order direct payload for requestId {} while awaiting method '{}' requestId {}: {}",
                    candidateRequestId, awaitedMethod, awaitedRequestId, e.getMessage());
        }
    }

    private boolean isControlOrAckFrame(byte[] frame) {
        if (frame.length <= HELLO_FRAME_LENGTH_WITHOUT_CRC) {
            return true;
        }

        if (frame.length > HELLO_FRAME_LENGTH_WITHOUT_CRC) {
            int protocol = ProtocolUtils.readInt16BE(frame, 15);
            if (protocol >= 0 && protocol <= 3) {
                return true;
            }
        }

        if (frame.length >= COMMON_HEADER_LENGTH_WITHOUT_CRC) {
            int payloadLength = ProtocolUtils.readInt16BE(frame, 17);
            return payloadLength <= 0;
        }

        return false;
    }

    private @Nullable Integer extractResponseRequestId(byte[] responseFrame) {
        try {
            ProtocolUtils.DecodedMessage decodedMessage = ProtocolUtils.decodeMessage(responseFrame, localKey,
                    EMPTY_NONCE, endpointPrefix, protocolSession.getConnectNonce(), protocolSession.getAckNonce(),
                    protocolSession.hasHandshakeContext());
            if (decodedMessage instanceof ProtocolUtils.MapPayloadResponse mapPayloadResponse) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Correlated direct map payload to requestId={} (protocol={}).",
                            mapPayloadResponse.requestId(), readFrameProtocol(responseFrame));
                }
                return Integer.valueOf(mapPayloadResponse.requestId());
            }

            if (decodedMessage instanceof ProtocolUtils.JsonPayloadResponse jsonPayloadResponse) {
                @Nullable
                Integer requestId = extractRequestIdFromJsonPayload(jsonPayloadResponse.payload());
                if (logger.isTraceEnabled()) {
                    logger.trace("Decoded direct JSON payload correlation result requestId={} (protocol={}).",
                            requestId, readFrameProtocol(responseFrame));
                }
                return requestId;
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Decoded direct frame did not produce correlatable payload (protocol={}).",
                        readFrameProtocol(responseFrame));
            }
            return null;
        } catch (RuntimeException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Failed to decode direct frame for request correlation (protocol={}): {}",
                        readFrameProtocol(responseFrame), e.getMessage());
            }
            return null;
        }
    }

    private @Nullable Integer extractRequestIdFromJsonPayload(String decodedPayload) {
        try {
            JsonElement parsedPayload = JsonParser.parseString(decodedPayload);
            if (!parsedPayload.isJsonObject()) {
                return null;
            }

            JsonObject decodedPayloadJson = parsedPayload.getAsJsonObject();
            if (decodedPayloadJson.has("id") && decodedPayloadJson.get("id").isJsonPrimitive()) {
                return Integer.valueOf(decodedPayloadJson.get("id").getAsInt());
            }

            @Nullable
            JsonElement rpcPayload = extractRpcPayload(decodedPayloadJson);
            if (rpcPayload == null) {
                return null;
            }

            if (rpcPayload.isJsonPrimitive()) {
                JsonElement rpcPayloadJson = JsonParser.parseString(rpcPayload.getAsString());
                if (rpcPayloadJson.isJsonObject() && rpcPayloadJson.getAsJsonObject().has("id")) {
                    return Integer.valueOf(rpcPayloadJson.getAsJsonObject().get("id").getAsInt());
                }
            } else if (rpcPayload.isJsonObject() && rpcPayload.getAsJsonObject().has("id")) {
                return Integer.valueOf(rpcPayload.getAsJsonObject().get("id").getAsInt());
            }

            return null;
        } catch (JsonSyntaxException | NumberFormatException | IllegalStateException e) {
            return null;
        }
    }

    private @Nullable JsonElement extractRpcPayload(JsonObject responseJson) {
        JsonElement dpsElement = responseJson.get("dps");
        if (dpsElement == null || !dpsElement.isJsonObject()) {
            return null;
        }
        JsonObject dpsJson = dpsElement.getAsJsonObject();
        if (dpsJson.has("102")) {
            return dpsJson.get("102");
        }
        if (dpsJson.has("101")) {
            return dpsJson.get("101");
        }
        return null;
    }

    private byte[] readSingleFrame(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        int first = readByte(inputStream);
        if (first == READ_TIMEOUT_SENTINEL) {
            return new byte[0];
        }
        if (first == READ_EOF_SENTINEL) {
            if (logger.isTraceEnabled() && shouldLogReadCounter(readSliceEofCount.get())) {
                logger.trace(
                        "Direct frame read reached stream EOF while awaiting next frame byte (timeoutCount={}, eofCount={}).",
                        readSliceTimeoutCount.get(), readSliceEofCount.get());
            }
            throw new IOException("Direct transport stream reached EOF while reading frame.");
        }

        if ((byte) first != '1' && (byte) first != 'L') {
            byte[] prefix = new byte[4];
            prefix[0] = (byte) first;
            readFully(inputStream, prefix, 1, 3);
            int frameLength = ProtocolUtils.readInt32BE(prefix, 0);
            validatePrefixedFrameLength(frameLength);
            byte[] frame = new byte[frameLength];
            readFully(inputStream, frame, 0, frameLength);
            byte[] combined = new byte[prefix.length + frame.length];
            System.arraycopy(prefix, 0, combined, 0, prefix.length);
            System.arraycopy(frame, 0, combined, prefix.length, frame.length);
            return combined;
        }

        byte[] firstPart = new byte[HELLO_FRAME_LENGTH_WITHOUT_CRC];
        firstPart[0] = (byte) first;
        readFully(inputStream, firstPart, 1, HELLO_FRAME_LENGTH_WITHOUT_CRC - 1);
        int protocol = ProtocolUtils.readInt16BE(firstPart, 15);
        if (protocol <= 3) {
            byte[] helloFrame = new byte[HELLO_FRAME_LENGTH];
            System.arraycopy(firstPart, 0, helloFrame, 0, HELLO_FRAME_LENGTH_WITHOUT_CRC);
            readFully(inputStream, helloFrame, HELLO_FRAME_LENGTH_WITHOUT_CRC, CRC_LENGTH);
            return helloFrame;
        }

        byte[] headerAndPayload = new byte[COMMON_HEADER_LENGTH_WITHOUT_CRC];
        System.arraycopy(firstPart, 0, headerAndPayload, 0, firstPart.length);
        readFully(inputStream, headerAndPayload, HELLO_FRAME_LENGTH_WITHOUT_CRC,
                COMMON_HEADER_LENGTH_WITHOUT_CRC - HELLO_FRAME_LENGTH_WITHOUT_CRC);
        int payloadLength = ProtocolUtils.readInt16BE(headerAndPayload, 17);
        validatePayloadLength(payloadLength);
        byte[] remaining = new byte[payloadLength + CRC_LENGTH];
        readFully(inputStream, remaining, 0, remaining.length);

        ByteArrayOutputStream output = new ByteArrayOutputStream(headerAndPayload.length + remaining.length);
        output.write(headerAndPayload);
        output.write(remaining);
        return output.toByteArray();
    }

    private void validatePrefixedFrameLength(int frameLength) throws IOException {
        if (frameLength <= 0) {
            logger.warn("Rejecting direct frame with invalid prefixed length {} (must be > 0).", frameLength);
            throw new IOException("Invalid direct frame prefix length: " + frameLength);
        }
        if (frameLength > MAX_PREFIXED_FRAME_LENGTH) {
            logger.warn("Rejecting direct frame with implausible prefixed length {} (max {}).", frameLength,
                    MAX_PREFIXED_FRAME_LENGTH);
            throw new IOException("Direct frame prefix length exceeds maximum: " + frameLength);
        }
    }

    private void validatePayloadLength(int payloadLength) throws IOException {
        if (payloadLength <= 0) {
            logger.warn("Rejecting direct frame with invalid payload length {} (must be > 0).", payloadLength);
            throw new IOException("Invalid direct frame payload length: " + payloadLength);
        }
        if (payloadLength > MAX_NON_PREFIXED_PAYLOAD_LENGTH) {
            logger.warn("Rejecting direct frame with implausible payload length {} (max {}).", payloadLength,
                    MAX_NON_PREFIXED_PAYLOAD_LENGTH);
            throw new IOException("Direct frame payload length exceeds maximum: " + payloadLength);
        }
    }

    private int readByte(InputStream inputStream) throws IOException {
        try {
            int value = inputStream.read();
            if (value < 0) {
                readSliceEofCount.incrementAndGet();
                return READ_EOF_SENTINEL;
            }
            return value;
        } catch (SocketTimeoutException e) {
            readSliceTimeoutCount.incrementAndGet();
            return READ_TIMEOUT_SENTINEL;
        }
    }

    private boolean shouldLogReadCounter(long counter) {
        return counter <= 3 || counter % 100 == 0;
    }

    private void readFully(InputStream inputStream, byte[] buffer, int offset, int length) throws IOException {
        int total = 0;
        while (total < length) {
            int read = inputStream.read(buffer, offset + total, length - total);
            if (read < 0) {
                throw new IOException("Socket closed while reading direct transport frame.");
            }
            total += read;
        }
    }

    private byte[] normalizeFrame(byte[] wireFrame) {
        if (wireFrame.length >= 7 && wireFrame[4] == '1' && wireFrame[5] == '.' && wireFrame[6] == '0') {
            int prefixedLength = ProtocolUtils.readInt32BE(wireFrame, 0);
            if (prefixedLength > 0 && wireFrame.length >= 4 + prefixedLength) {
                byte[] stripped = new byte[prefixedLength];
                System.arraycopy(wireFrame, 4, stripped, 0, prefixedLength);
                return stripped;
            }
        }
        if (wireFrame.length >= 7 && wireFrame[4] == 'L' && wireFrame[5] == '0' && wireFrame[6] == '1') {
            int prefixedLength = ProtocolUtils.readInt32BE(wireFrame, 0);
            if (prefixedLength > 0 && wireFrame.length >= 4 + prefixedLength) {
                byte[] stripped = new byte[prefixedLength];
                System.arraycopy(wireFrame, 4, stripped, 0, prefixedLength);
                return stripped;
            }
        }
        return wireFrame;
    }

    private int readFrameProtocol(byte[] frame) {
        if (frame.length >= HELLO_FRAME_LENGTH_WITHOUT_CRC) {
            return ProtocolUtils.readInt16BE(frame, 15);
        }
        return -1;
    }

    private int readFramePayloadLength(byte[] frame) {
        if (frame.length >= COMMON_HEADER_LENGTH_WITHOUT_CRC) {
            return ProtocolUtils.readInt16BE(frame, 17);
        }
        return -1;
    }

    private boolean performHelloHandshake(Socket socket, OutputStream outputStream, String method, int requestId)
            throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("Starting direct HELLO negotiation for {}:{} before method '{}' (requestId={})", localHost,
                    localPort, method, requestId);
        }
        boolean[] l01Modes = preferredHelloL01 ? new boolean[] { true, false } : new boolean[] { false, true };
        boolean[] prefixedModes = preferredHelloPrefixed ? new boolean[] { true, false }
                : new boolean[] { false, true };
        for (boolean prefixedMode : prefixedModes) {
            for (boolean l01Mode : l01Modes) {
                byte[] helloRequest = protocolSession.buildHelloRequestFrame(l01Mode, prefixedMode);
                if (logger.isTraceEnabled()) {
                    logger.trace("Direct HELLO send using {} (prefixed={}) bytes={} firstBytes={}",
                            l01Mode ? "L01" : "1.0", prefixedMode, helloRequest.length, toHexPreview(helloRequest, 24));
                }
                outputStream.write(helloRequest);
                outputStream.flush();

                byte[] helloResponse = normalizeFrame(readSingleFrame(socket));
                if (logger.isTraceEnabled()) {
                    logger.trace("Direct HELLO recv using {} (prefixed={}) bytes={} firstBytes={}",
                            l01Mode ? "L01" : "1.0", prefixedMode, helloResponse.length,
                            toHexPreview(helloResponse, 24));
                }
                if (protocolSession.applyHelloResponse(helloResponse)) {
                    rememberHelloPreference(l01Mode, prefixedMode);
                    logger.debug(
                            "Direct local handshake succeeded for {}:{} using {} (prefixed={}) with connectNonce={} and ackNonce={} before method '{}' (requestId={}).",
                            localHost, localPort, l01Mode ? "L01" : "1.0", prefixedMode,
                            protocolSession.getConnectNonce(), protocolSession.getAckNonce(), method, requestId);
                    return true;
                }

                logger.debug(
                        "Direct local handshake attempt using {} (prefixed={}) produced no valid HELLO response for {}:{} before method '{}' (requestId={}). responseBytes={}",
                        l01Mode ? "L01" : "1.0", prefixedMode, localHost, localPort, method, requestId,
                        helloResponse.length);
            }
        }
        return false;
    }

    private void rememberHelloPreference(boolean l01Mode, boolean prefixedMode) {
        boolean changed = preferredHelloL01 != l01Mode || preferredHelloPrefixed != prefixedMode;
        preferredHelloL01 = l01Mode;
        preferredHelloPrefixed = prefixedMode;
        if (changed && logger.isTraceEnabled()) {
            logger.trace("Updated direct HELLO preference to {} (prefixed={}) for subsequent sessions.",
                    l01Mode ? "L01" : "1.0", prefixedMode);
        }
    }

    private String toHexPreview(byte[] bytes, int maxLen) {
        int length = Math.min(bytes.length, maxLen);
        if (length == 0) {
            return "<empty>";
        }
        StringBuilder sb = new StringBuilder(length * 3 + 8);
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", bytes[i] & 0xFF));
            if (i + 1 < length) {
                sb.append(' ');
            }
        }
        if (bytes.length > maxLen) {
            sb.append(" ...");
        }
        return sb.toString();
    }
}
