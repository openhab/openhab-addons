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
package org.openhab.binding.atv.internal.client.protocols.mrp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.DeviceListener;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionFailedError;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.openhab.binding.atv.internal.client.support.Chacha20Cipher;
import org.openhab.binding.atv.internal.client.support.ReadBuffer;
import org.openhab.binding.atv.internal.client.support.Sockets;
import org.openhab.binding.atv.internal.client.support.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network layer for MRP: varint-length-prefixed {@code ProtocolMessage} frames over TCP,
 * optionally encrypted with ChaCha20-Poly1305 (8-byte little-endian nonce counters).
 *
 * <p>
 * The length prefix is computed <em>after</em> encryption. A dedicated reader
 * thread reassembles frames from the stream using {@link Variant#tryReadVariant}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpConnection implements AbstractMrpConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(MrpConnection.class);

    private final String host;
    private final int port;
    private final @Nullable DeviceListener deviceListener;

    private final Object sendLock = new Object();
    private final Object stateLock = new Object();
    private final AtomicBoolean lostReported = new AtomicBoolean();

    /**
     * Maximum accepted frame payload length. Real MRP messages are tiny (artwork responses are
     * the largest, well below 1 MiB); anything bigger indicates a corrupt/hostile length prefix
     * and is treated as a protocol error instead of allocating gigabytes.
     */
    static final long MAX_FRAME_SIZE = 1 << 24; // 16 MiB

    private volatile @Nullable Listener listener;
    private volatile @Nullable Socket socket;
    private volatile @Nullable OutputStream out;
    private volatile @Nullable Chacha20Cipher chacha;
    private volatile boolean closedLocally;
    private volatile @Nullable Exception writeError;
    /** Bumped by every close() so a connect that was in flight at the time can tell it lost. */
    private int closeGeneration;

    /**
     * Creates a connection without a device listener.
     *
     * @param host device address
     * @param port MRP service port
     */
    public MrpConnection(String host, int port) {
        this(host, port, null);
    }

    /**
     * Creates a connection.
     *
     * @param host device address
     * @param port MRP service port
     * @param deviceListener optional listener notified about connection closed/lost, or
     *            {@code null}
     */
    public MrpConnection(String host, int port, @Nullable DeviceListener deviceListener) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
        this.deviceListener = deviceListener;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> result = new CompletableFuture<>();
        int generation;
        synchronized (stateLock) {
            generation = closeGeneration;
        }
        Thread.ofPlatform().daemon().name("mrp-connect-" + host).start(() -> {
            try {
                Socket newSocket = Sockets.connect(host, port);
                try {
                    newSocket.setKeepAlive(true);
                } catch (IOException e) {
                    LOGGER.debug("Keep-alive not supported: {}", e.toString());
                }
                synchronized (stateLock) {
                    if (generation != closeGeneration) {
                        newSocket.close();
                        result.completeExceptionally(new ConnectionFailedError("closed while connecting"));
                        return;
                    }
                    this.out = newSocket.getOutputStream();
                    this.socket = newSocket;
                    this.closedLocally = false;
                    this.writeError = null;
                    this.lostReported.set(false);
                }
                LOGGER.debug("Connection established to {}:{}", host, port);
                Thread.ofPlatform().daemon().name("mrp-reader-" + host).start(() -> readLoop(newSocket));
                MrpFutures.completeVoid(result);
            } catch (IOException e) {
                result.completeExceptionally(new ConnectionFailedError("failed to connect to " + host + ":" + port, e));
            }
        });
        return result;
    }

    @Override
    public void enableEncryption(byte[] outputKey, byte[] inputKey) {
        this.chacha = new Chacha20Cipher(outputKey, inputKey);
    }

    @Override
    public boolean isConnected() {
        @Nullable
        Socket current = socket;
        return current != null && !current.isClosed();
    }

    @Override
    public void close() {
        LOGGER.debug("Closing connection to {}:{}", host, port);
        @Nullable
        Socket current;
        synchronized (stateLock) {
            closeGeneration++;
            closedLocally = true;
            current = socket;
            socket = null;
            chacha = null;
        }
        if (current != null) {
            try {
                current.close();
            } catch (IOException ignored) {
                // best effort
            }
        }
    }

    @Override
    public void send(ProtocolMessage message) {
        byte[] serialized = message.toByteArray();
        sendRaw(serialized);
        LOGGER.trace(">> Send: Protobuf type={}", message.getType());
    }

    /**
     * Sends already serialized message bytes to the device. The bytes are encrypted
     * (when encryption is enabled) and length-prefixed.
     *
     * @param data serialized protobuf message
     */
    public void sendRaw(byte[] data) {
        @Nullable
        Chacha20Cipher cipher = chacha;
        @Nullable
        OutputStream stream = out;
        if (stream == null) {
            throw new IllegalStateException("not connected");
        }
        synchronized (sendLock) {
            byte[] payload = cipher != null ? cipher.encrypt(data) : data;
            try {
                stream.write(Variant.writeVariant(payload.length));
                stream.write(payload);
                stream.flush();
            } catch (IOException e) {
                LOGGER.debug("Failed to write to device", e);
                // Record the failure before closing: a failed transport write must be
                // reported as connection *lost* with the cause, not as a clean local close.
                writeError = e;
                close();
            }
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void readLoop(Socket boundSocket) {
        @Nullable
        Exception error = null;
        try {
            InputStream in = boundSocket.getInputStream();
            byte[] chunk = new byte[8192];
            ReadBuffer buffer = new ReadBuffer();
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.append(chunk, 0, read);
                drainFrames(buffer);
            }
            LOGGER.debug("Received EOF from server");
        } catch (IOException e) {
            if (!closedLocally) {
                error = e;
            }
        } catch (RuntimeException e) {
            // A decode crash (e.g. corrupt length prefix) must be reported as a lost
            // connection; never let it escape as a silent clean close.
            error = e;
        } finally {
            boolean wasLocal = closedLocally;
            try {
                boundSocket.close();
            } catch (IOException ignored) {
                // best effort
            }
            if (socket == boundSocket) {
                socket = null;
            }
            // A recorded write failure means the disconnect was not a clean local close,
            // even though close() was invoked from sendRaw.
            @Nullable
            Exception cause = error != null ? error : writeError;
            notifyLost(wasLocal && cause == null ? null : cause);
        }
    }

    private void drainFrames(ReadBuffer buffer) throws IOException {
        while (true) {
            Optional<Variant.Result> length;
            try {
                length = Variant.tryReadVariant(buffer.view());
            } catch (IllegalArgumentException e) {
                throw new IOException("invalid frame length prefix", e);
            }
            if (length.isEmpty()) {
                break;
            }
            long rawLength = length.get().value();
            if (rawLength < 0 || rawLength > MAX_FRAME_SIZE) {
                // Corrupt/hostile length prefix: report as a protocol error (connection lost
                // with cause) instead of crashing the reader with an unchecked exception
                throw new IOException("invalid frame length " + rawLength);
            }
            int prefixLength = length.get().consumed();
            int payloadLength = (int) rawLength;
            if (buffer.size() - prefixLength < payloadLength) {
                LOGGER.trace("Require {} bytes but only {} in buffer", payloadLength, buffer.size() - prefixLength);
                break;
            }
            buffer.skip(prefixLength);
            byte[] data = decrypt(buffer.take(payloadLength));
            try {
                handleMessage(data);
            } catch (Exception e) {
                LOGGER.debug("Failed to handle message", e);
            }
        }
    }

    /**
     * Decrypts a frame. The cipher counter has already moved on when this fails, so every
     * later frame would fail too; the connection is treated as lost instead.
     */
    private byte[] decrypt(byte[] data) throws IOException {
        @Nullable
        Chacha20Cipher cipher = chacha;
        if (cipher == null) {
            return data;
        }
        try {
            return cipher.decrypt(data);
        } catch (AuthenticationError e) {
            throw new IOException("failed to decrypt frame", e);
        }
    }

    private void handleMessage(byte[] plain) throws IOException {
        ProtocolMessage parsed = ProtocolMessage.parseFrom(plain, MrpExtensions.EXTENSION_REGISTRY);
        LOGGER.trace("<< Receive: Protobuf type={}", parsed.getType());
        @Nullable
        Listener current = listener;
        if (current != null) {
            current.messageReceived(parsed);
        }
    }

    private void notifyLost(@Nullable Exception exception) {
        if (!lostReported.compareAndSet(false, true)) {
            return;
        }
        LOGGER.debug("Disconnected from device: {}", exception == null ? "closed" : exception.toString());
        @Nullable
        Listener current = listener;
        if (current != null) {
            current.connectionLost(exception);
        }
        if (deviceListener != null) {
            if (exception == null) {
                deviceListener.connectionClosed();
            } else {
                deviceListener.connectionLost(exception);
            }
        }
    }

    @Override
    public String toString() {
        return "MRP:" + host;
    }
}
