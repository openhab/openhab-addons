/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.internal.TopicSubscribers;
import org.eclipse.smarthome.io.transport.mqtt.internal.client.MqttAsyncClientWrapper;

import com.hivemq.client.mqtt.MqttClientState;

/**
 * We need an extended MqttBrokerConnection to overwrite the protected `connectionCallbacks` with
 * an instance that takes the mocked version of `MqttBrokerConnection` and overwrite the connection state.
 *
 * We also replace the internal MqttAsyncClient with a spied one, that in respect to the success flags
 * immediately succeed or fail with publish, subscribe, unsubscribe, connect, disconnect.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MqttBrokerConnectionEx extends MqttBrokerConnection {
    public MqttConnectionState connectionStateOverwrite = MqttConnectionState.DISCONNECTED;
    public boolean publishSuccess = true;
    public boolean subscribeSuccess = true;
    public boolean unsubscribeSuccess = true;
    public boolean disconnectSuccess = true;
    public boolean connectSuccess = true;
    public boolean connectTimeout = false;

    public MqttBrokerConnectionEx(String host, @Nullable Integer port, boolean secure, String clientId) {
        super(host, port, secure, clientId);
    }

    public void setConnectionCallback(MqttBrokerConnectionEx o) {
        connectionCallback = spy(new ConnectionCallback(o));
    }

    public Map<String, TopicSubscribers> getSubscribers() {
        return subscribers;
    }

    @Override
    protected MqttAsyncClientWrapper createClient() {
        MqttAsyncClientWrapper mockedClient = mock(MqttAsyncClientWrapper.class);
        // connect
        doAnswer(i -> {
            if (!connectTimeout) {
                connectionCallback.onConnected(null);
                connectionStateOverwrite = MqttConnectionState.CONNECTED;
                return CompletableFuture.completedFuture(null);
            }
            return new CompletableFuture<>();
        }).when(mockedClient).connect(any(), anyInt(), any(), any());
        doAnswer(i -> {
            if (disconnectSuccess) {
                connectionCallback.onDisconnected(new Throwable("disconnect called"));
                connectionStateOverwrite = MqttConnectionState.DISCONNECTED;
                return CompletableFuture.completedFuture(null);
            }
            return new CompletableFuture<>();
        }).when(mockedClient).disconnect();
        // subscribe
        doAnswer(i -> {
            if (subscribeSuccess) {
                return CompletableFuture.completedFuture(null);
            } else {
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(new Throwable("subscription failed"));
                return future;
            }
        }).when(mockedClient).subscribe(any(), anyInt(), any());
        // unsubscribe
        doAnswer(i -> {
            if (unsubscribeSuccess) {
                return CompletableFuture.completedFuture(null);
            } else {
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(new Throwable("unsubscription failed"));
                return future;
            }
        }).when(mockedClient).unsubscribe(any());
        // state
        doAnswer(i -> {
            return MqttClientState.CONNECTED;
        }).when(mockedClient).getState();
        return mockedClient;
    }

    @Override
    public @NonNull MqttConnectionState connectionState() {
        return connectionStateOverwrite;
    }
}
