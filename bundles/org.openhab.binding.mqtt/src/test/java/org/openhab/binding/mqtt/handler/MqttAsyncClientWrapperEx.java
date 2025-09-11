/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.handler;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttWillAndTestament;
import org.openhab.core.io.transport.mqtt.internal.Subscription;
import org.openhab.core.io.transport.mqtt.internal.client.MqttAsyncClientWrapper;

import com.hivemq.client.mqtt.MqttClientState;

/**
 * We need an extended MqttAsyncClientWrapper, that will, in respect to the success flags of the connection, immediately
 * succeed or fail with publish, subscribe, unsubscribe, connect, disconnect.
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public class MqttAsyncClientWrapperEx extends MqttAsyncClientWrapper {

    private final MqttBrokerConnectionEx connection;

    public MqttAsyncClientWrapperEx(MqttBrokerConnectionEx connection) {
        this.connection = connection;
    }

    @Override
    public CompletableFuture<?> connect(@Nullable MqttWillAndTestament lwt, int keepAliveInterval,
            @Nullable String username, @Nullable String password) {
        if (!connection.connectTimeout) {
            connection.getCallback().onConnected(null);
            connection.connectionStateOverwrite = MqttConnectionState.CONNECTED;
            return CompletableFuture.completedFuture(null);
        }
        return new CompletableFuture<>();
    }

    @Override
    public CompletableFuture<@Nullable Void> disconnect() {
        if (connection.disconnectSuccess) {
            connection.getCallback().onDisconnected(new Throwable("disconnect called"));
            connection.connectionStateOverwrite = MqttConnectionState.DISCONNECTED;
            return CompletableFuture.completedFuture(null);
        }
        return new CompletableFuture<>();
    }

    @Override
    public MqttClientState getState() {
        return MqttClientState.CONNECTED;
    }

    @Override
    public CompletableFuture<?> publish(String topic, byte[] payload, boolean retain, int qos) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> subscribe(String topic, int qos, Subscription subscription) {
        if (connection.subscribeSuccess) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.failedFuture(new Throwable("subscription failed"));
    }

    @Override
    public CompletableFuture<?> unsubscribe(String topic) {
        if (connection.unsubscribeSuccess) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.failedFuture(new Throwable("unsubscription failed"));
    }
}
