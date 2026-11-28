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
package org.openhab.binding.atv.internal.client.protocols.airplay;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.DeviceListener;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.protocols.mrp.AbstractMrpConnection;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MRP connection implemented as a channel/stream over AirPlay.
 *
 * <p>
 * A transparent connection bridging MRP protobuf messages onto the {@link DataStreamChannel}
 * of an {@link Ap2Session}. Handed to {@code Mrp.createWithConnection(...,
 * requiresHeartbeat=false)} since the control channel already runs its own keep-alive.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayMrpConnection implements AbstractMrpConnection, DataStreamChannel.Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirPlayMrpConnection.class);

    private final Ap2Session session;
    private final @Nullable DeviceListener deviceListener;

    private volatile @Nullable Listener listener;
    private volatile @Nullable DataStreamChannel dataChannel;

    /**
     * Creates a new connection over an AirPlay 2 session.
     *
     * @param session the session whose data channel carries the MRP messages
     * @param deviceListener listener notified when the channel connection drops, may be
     *            {@code null}
     */
    public AirPlayMrpConnection(Ap2Session session, @Nullable DeviceListener deviceListener) {
        this.session = session;
        this.deviceListener = deviceListener;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public CompletableFuture<Void> connect() {
        DataStreamChannel channel = session.dataChannel();
        if (channel == null) {
            return CompletableFuture.failedFuture(new InvalidStateError("remote control channel not connected"));
        }
        this.dataChannel = channel;
        channel.setListener(this);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void enableEncryption(byte[] outputKey, byte[] inputKey) {
        // Channel is already encrypted (DataStream-Salt keys); nothing to do
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void close() {
        DataStreamChannel channel = dataChannel;
        if (channel != null) {
            LOGGER.debug("Closing connection");
            channel.close();
            dataChannel = null;
        }
    }

    @Override
    public void send(ProtocolMessage message) {
        DataStreamChannel channel = dataChannel;
        if (channel != null) {
            channel.sendProtobuf(message);
            LOGGER.debug(">> Send: Protobuf type={}", message.getType());
        }
    }

    @Override
    public void handleProtobuf(ProtocolMessage message) {
        LOGGER.debug("<< Receive: Protobuf type={}", message.getType());
        Listener current = listener;
        if (current != null) {
            current.messageReceived(message);
        }
    }

    @Override
    public void handleConnectionLost(Exception exception) {
        LOGGER.debug("Disconnected from device: {}", exception == null ? "closed" : exception.toString());
        if (deviceListener != null) {
            if (exception == null) {
                deviceListener.connectionClosed();
            } else {
                deviceListener.connectionLost(exception);
            }
        }
    }
}
