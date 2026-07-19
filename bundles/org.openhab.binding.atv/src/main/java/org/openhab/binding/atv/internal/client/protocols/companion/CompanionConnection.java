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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionFailedError;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.support.Chacha20Cipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote connection to a Companion device.
 *
 * <p>
 * Framing is a 1-byte {@link FrameType} plus a 3-byte big-endian payload length followed by
 * the payload (this is <em>not</em> HAP block framing). Once encryption is enabled with
 * {@link #enableEncryption(byte[], byte[])}, payloads are ChaCha20-Poly1305 encrypted with a
 * 12-byte little-endian counter nonce and the 4-byte frame header as AAD; the length field
 * then includes the 16-byte authentication tag.
 *
 * <p>
 * Reading happens on a dedicated virtual thread which invokes the frame listener for each
 * complete frame; exceptions thrown while handling one frame are logged and do not kill the
 * reader.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionConnection {

    /** Listener interface for received frames. */
    public interface FrameListener {

        /**
         * A frame was received (and decrypted) from the remote device.
         *
         * @param frameType frame type
         * @param payload decrypted frame payload (without the 4-byte header)
         */
        void frameReceived(FrameType frameType, byte[] payload);
    }

    /** Listener for connection teardown notifications. */
    public interface ConnectionListener {

        /**
         * The connection was lost due to an error.
         *
         * @param exception the causing error
         */
        default void connectionLost(Exception exception) {
        }

        /** The connection was closed without an error. */
        default void connectionClosed() {
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanionConnection.class);

    /** Poly1305 tag length appended to encrypted payloads. */
    public static final int AUTH_TAG_LENGTH = 16;
    /** Frame header length (frame type byte + 3-byte big-endian length). */
    public static final int HEADER_LENGTH = 4;

    private final String host;
    private final int port;
    private final ConnectionListener connectionListener;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Object sendLock = new Object();

    private volatile @Nullable FrameListener listener;
    private volatile @Nullable Chacha20Cipher chacha;
    private volatile @Nullable Socket socket;
    private @Nullable OutputStream out;
    private @Nullable Thread readerThread;

    /**
     * Creates a connection (not yet connected).
     *
     * @param host device address
     * @param port Companion service port
     * @param connectionListener teardown listener, or {@code null} for none
     */
    public CompanionConnection(String host, int port, @Nullable ConnectionListener connectionListener) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
        this.connectionListener = connectionListener == null ? new ConnectionListener() {
        } : connectionListener;
    }

    /**
     * Sets the frame listener.
     *
     * @param listener listener receiving decrypted frames
     */
    public void setListener(FrameListener listener) {
        this.listener = listener;
    }

    /**
     * Whether a connection is open.
     */
    public boolean isConnected() {
        Socket current = socket;
        return current != null && !current.isClosed();
    }

    /**
     * Connects to the device and starts the reader thread.
     *
     * @throws ConnectionFailedError if the TCP connection cannot be established
     */
    public synchronized void connect() {
        if (socket != null) {
            return;
        }
        try {
            Socket newSocket = new Socket();
            newSocket.setTcpNoDelay(true);
            newSocket.connect(new InetSocketAddress(host, port));
            this.socket = newSocket;
            this.out = newSocket.getOutputStream();
        } catch (IOException e) {
            throw new ConnectionFailedError("failed to connect to " + host + ":" + port, e);
        }
        LOGGER.debug("Connected to companion device {}:{}", host, port);
        readerThread = Thread.ofVirtual().name("companion-reader-" + host + ":" + port).start(this::readLoop);
    }

    /**
     * Closes the connection to the device.
     */
    public void close() {
        LOGGER.debug("Closing connection");
        closed.set(true);
        Socket current = socket;
        socket = null;
        if (current != null) {
            try {
                current.close();
            } catch (IOException ignored) {
                // best effort
            }
        }
    }

    /**
     * Enables transparent encryption with the specified keys (12-byte nonce mode).
     *
     * @param outputKey key used to encrypt outgoing frames
     * @param inputKey key used to decrypt incoming frames
     */
    public void enableEncryption(byte[] outputKey, byte[] inputKey) {
        this.chacha = new Chacha20Cipher(outputKey, inputKey, 12);
    }

    /**
     * Sends a frame without waiting for a response.
     *
     * @param frameType frame type
     * @param data frame payload (plaintext; encrypted here when encryption is enabled)
     * @throws InvalidStateError if not connected
     */
    public void send(FrameType frameType, byte[] data) {
        // Encryption and write must happen atomically: the cipher nonce is a message
        // counter, so the encryption order must match the on-wire frame order.
        synchronized (sendLock) {
            OutputStream currentOut;
            synchronized (this) {
                OutputStream currentOutField = out;
                if (socket == null || currentOutField == null) {
                    throw new InvalidStateError("not connected");
                }
                currentOut = currentOutField;
            }

            Chacha20Cipher cipher = chacha;
            int payloadLength = data.length;
            if (cipher != null && payloadLength > 0) {
                payloadLength += AUTH_TAG_LENGTH;
            }
            byte[] header = new byte[] { (byte) frameType.value(), (byte) ((payloadLength >> 16) & 0xFF),
                    (byte) ((payloadLength >> 8) & 0xFF), (byte) (payloadLength & 0xFF) };

            byte[] payload = data;
            if (cipher != null && data.length > 0) {
                payload = cipher.encrypt(data, null, header);
            }

            try {
                currentOut.write(header);
                currentOut.write(payload);
                currentOut.flush();
            } catch (IOException e) {
                throw new ConnectionFailedError("failed to send frame", e);
            }
        }
    }

    private void readLoop() {
        Exception error = null;
        Socket currentSocket = socket;
        try {
            if (currentSocket == null) {
                return;
            }
            InputStream in = currentSocket.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
                byte[] data = buffer.toByteArray();
                int offset = 0;
                while (data.length - offset >= HEADER_LENGTH) {
                    int payloadLength = ((data[offset + 1] & 0xFF) << 16) | ((data[offset + 2] & 0xFF) << 8)
                            | (data[offset + 3] & 0xFF);
                    if (data.length - offset < HEADER_LENGTH + payloadLength) {
                        LOGGER.debug("Require {} bytes but only {} in buffer", HEADER_LENGTH + payloadLength,
                                data.length - offset);
                        break;
                    }
                    byte[] header = Arrays.copyOfRange(data, offset, offset + HEADER_LENGTH);
                    byte[] payload = Arrays.copyOfRange(data, offset + HEADER_LENGTH,
                            offset + HEADER_LENGTH + payloadLength);
                    offset += HEADER_LENGTH + payloadLength;
                    handleFrame(header, payload);
                }
                if (offset > 0) {
                    byte[] rest = Arrays.copyOfRange(data, offset, data.length);
                    buffer.reset();
                    buffer.write(rest, 0, rest.length);
                }
            }
        } catch (IOException e) {
            if (!closed.get()) {
                error = e;
            }
        }

        socket = null;
        if (closed.getAndSet(true)) {
            return; // close() was requested locally; no notification (transport already nulled)
        }
        if (error != null) {
            LOGGER.debug("Connection lost to remote device", error);
            connectionListener.connectionLost(error);
        } else {
            LOGGER.debug("Connection closed by remote device");
            connectionListener.connectionClosed();
        }
    }

    private void handleFrame(byte[] header, byte[] payload) {
        try {
            Chacha20Cipher cipher = chacha;
            byte[] data = payload;
            if (cipher != null && payload.length > 0) {
                data = cipher.decrypt(payload, null, header);
            }
            FrameListener currentListener = listener;
            if (currentListener != null) {
                currentListener.frameReceived(FrameType.fromValue(header[0] & 0xFF), data);
            }
        } catch (Exception e) {
            LOGGER.warn("failed to handle frame", e);
        }
    }
}
