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
package org.openhab.binding.atv.internal.client.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionFailedError;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionLostError;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for HAP based channels (connections): a raw TCP connection using
 * {@link HapSession} encryption and segmenting.
 *
 * <p>
 * A dedicated virtual thread performs the blocking reads and feeds decrypted plaintext to
 * {@link #onReceive(byte[])}; writes are serialized on a single-threaded virtual-thread
 * executor so {@link #send(byte[])} never blocks the caller and the encryption counters
 * advance in send order.
 *
 * <p>
 * Subclasses implement {@link #onReceive(byte[])} (doing their own message
 * framing/buffering) and may register a connection-lost callback via
 * {@link #onConnectionLost(Consumer)}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class HapChannel implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(HapChannel.class);

    private final HapSession session = new HapSession();
    private final ExecutorService writer = Executors
            .newSingleThreadExecutor(Thread.ofVirtual().name("hap-channel-writer").factory());
    private final AtomicBoolean closed = new AtomicBoolean();

    private volatile @Nullable Socket socket;
    private volatile @Nullable InputStream inputStream;
    private volatile @Nullable OutputStream outputStream;
    private volatile @Nullable Consumer<@Nullable Throwable> connectionLostCallback;

    /**
     * Creates a channel with encryption enabled using the given session keys.
     *
     * @param outputKey key encrypting outgoing data
     * @param inputKey key decrypting incoming data
     */
    protected HapChannel(byte[] outputKey, byte[] inputKey) {
        session.enable(outputKey, inputKey);
    }

    /**
     * Connects to the device and starts the reader thread.
     *
     * @param address remote host
     * @param port remote port
     * @return a future completing when the connection is established, failing with
     *         {@link ConnectionFailedError} if it cannot be
     */
    public CompletableFuture<Void> connect(String address, int port) {
        return CompletableFuture.runAsync(() -> {
            if (socket != null) {
                throw new InvalidStateError("already connected");
            }
            try {
                Socket newSocket = new Socket();
                newSocket.connect(new InetSocketAddress(address, port));
                newSocket.setTcpNoDelay(true);
                this.socket = newSocket;
                this.inputStream = newSocket.getInputStream();
                this.outputStream = newSocket.getOutputStream();
            } catch (IOException e) {
                throw new ConnectionFailedError("failed to connect to " + address + ":" + port, e);
            }
            logger.debug("Connected to {}:{}", address, port);
            startReader();
        }, writer);
    }

    /**
     * Remote connection port number.
     *
     * @throws InvalidStateError if not connected
     */
    public int port() {
        Socket currentSocket = socket;
        if (currentSocket == null) {
            throw new InvalidStateError("not connected");
        }
        return currentSocket.getPort();
    }

    /**
     * Registers a callback invoked once when the connection is lost (not invoked on a
     * local {@link #close()}).
     *
     * @param callback receives the cause, or {@code null} if the remote end closed the
     *            connection cleanly
     */
    public void onConnectionLost(Consumer<@Nullable Throwable> callback) {
        this.connectionLostCallback = callback;
    }

    /**
     * Handles decrypted data received from the device. Called on the reader thread.
     *
     * @param plaintext decrypted bytes (framing/buffering is up to the subclass)
     */
    protected abstract void onReceive(byte[] plaintext);

    /**
     * Encrypts and sends a message to the device.
     *
     * @param data plaintext to send
     * @return a future completing once the data has been written to the socket
     */
    public CompletableFuture<Void> send(byte[] data) {
        byte[] copy = data.clone();
        return CompletableFuture.runAsync(() -> {
            OutputStream out = outputStream;
            if (out == null) {
                throw new InvalidStateError("not connected");
            }
            byte[] encrypted = session.encrypt(copy);
            try {
                out.write(encrypted);
                out.flush();
            } catch (IOException e) {
                throw new ConnectionLostError("failed to send data", e);
            }
        }, writer);
    }

    /**
     * Closes the channel; the connection-lost callback is not invoked.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        closeSocket();
        writer.shutdown();
    }

    private void closeSocket() {
        Socket currentSocket = socket;
        if (currentSocket != null) {
            try {
                currentSocket.close();
            } catch (IOException e) {
                logger.debug("Error closing socket", e);
            }
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void startReader() {
        Thread.ofVirtual().name("hap-channel-reader").start(() -> {
            @Nullable
            Throwable cause = null;
            try {
                InputStream in = inputStream;
                if (in == null) {
                    throw new InvalidStateError("not connected");
                }
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    if (read == 0) {
                        continue;
                    }
                    byte[] decrypted = session.decrypt(Arrays.copyOf(buffer, read));
                    if (decrypted.length > 0) {
                        onReceive(decrypted);
                    }
                }
            } catch (Throwable t) {
                cause = t;
            }
            boolean locallyClosed = closed.get();
            logger.debug("Connection was lost to remote (locally closed: {})", locallyClosed);
            if (!locallyClosed) {
                Consumer<@Nullable Throwable> callback = connectionLostCallback;
                if (callback != null) {
                    callback.accept(cause);
                }
            }
        });
    }

    /**
     * Sets up a new HAP channel with encryption enabled: derives the channel keys from
     * the verifier and connects the channel produced by the factory.
     *
     * @param <T> concrete channel type
     * @param factory creates the channel from (outputKey, inputKey)
     * @param verifier a completed pair-verify (or transient pair-setup) procedure
     * @param address remote host
     * @param port remote port
     * @param salt HKDF salt for key derivation
     * @param outputInfo HKDF info for the output key
     * @param inputInfo HKDF info for the input key
     * @return a future completing with the connected channel
     */
    public static <T extends HapChannel> CompletableFuture<T> setupChannel(BiFunction<byte[], byte[], T> factory,
            PairVerifyProcedure verifier, String address, int port, String salt, String outputInfo, String inputInfo) {
        EncryptionKeys keys = verifier.encryptionKeys(salt, outputInfo, inputInfo);
        T channel = factory.apply(keys.outputKey(), keys.inputKey());
        return channel.connect(address, port).thenApply(v -> channel);
    }
}
