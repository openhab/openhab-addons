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

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;

/**
 * Abstract base for an MRP connection.
 *
 * <p>
 * {@link MrpConnection} is the plain TCP implementation; the AirPlay MRP tunnel (Wave 5)
 * substitutes its own transport by implementing this interface and handing it to
 * {@link Mrp#createWithConnection}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface AbstractMrpConnection {

    /** Receiver of messages and connection state changes. */
    interface Listener {

        /**
         * Called for every decoded protocol message.
         *
         * @param message the decoded message
         */
        void messageReceived(ProtocolMessage message);

        /**
         * Called once when the connection has gone away.
         *
         * @param exception the error that tore the connection down, or {@code null} for a
         *            clean close initiated by the peer or by {@link #close()}
         */
        void connectionLost(@Nullable Exception exception);
    }

    /**
     * Sets the listener receiving messages and connection state changes. Must be set
     * before {@link #connect()}.
     *
     * @param listener the listener
     */
    void setListener(Listener listener);

    /**
     * Connects to the device.
     *
     * @return future completing when the connection is established
     */
    CompletableFuture<Void> connect();

    /**
     * Enables encryption with the specified keys (ChaCha20-Poly1305, 8-byte nonce).
     *
     * @param outputKey key used to encrypt outgoing messages
     * @param inputKey key used to decrypt incoming messages
     */
    void enableEncryption(byte[] outputKey, byte[] inputKey);

    /**
     * Returns whether a connection is open.
     *
     * @return {@code true} if connected
     */
    boolean isConnected();

    /**
     * Closes the connection to the device.
     */
    void close();

    /**
     * Sends a protobuf message to the device.
     *
     * @param message the message to send
     */
    void send(ProtocolMessage message);
}
